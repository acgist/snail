package com.acgist.snail.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.stream.Collectors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.pojo.wrapper.HttpHeaderWrapper;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.IoUtils;
import com.acgist.snail.utils.MapUtils;
import com.acgist.snail.utils.UrlUtils;

/**
 * <p>HTTP客户端</p>
 * <p>配置参考：https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html</p>
 * <p>推荐直接使用HTTP协议下载：HTTPS下载CPU占用较高</p>
 * 
 * @author acgist
 */
public final class HttpClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpClient.class);
	
	/**
	 * <p>HTTP状态码</p>
	 * <p>协议链接：https://www.ietf.org/rfc/rfc2616</p>
	 * 
	 * @author acgist
	 */
	public enum StatusCode {
		
		/**
		 * <p>成功</p>
		 */
		OK(200),
		/**
		 * <p>断点续传</p>
		 */
		PARTIAL_CONTENT(206),
		/**
		 * <p>永久重定向</p>
		 */
		MOVED_PERMANENTLY(301),
		/**
		 * <p>临时重定向</p>
		 * 
		 * @see #SEE_OTHER
		 * @see #TEMPORARY_REDIRECT
		 */
		FOUND(302),
		/**
		 * <p>临时重定向</p>
		 * <p>请求已被处理：POST不能获取参数</p>
		 */
		SEE_OTHER(303),
		/**
		 * <p>临时重定向</p>
		 * <p>请求没有处理：POST可以获取参数</p>
		 */
		TEMPORARY_REDIRECT(307),
		/**
		 * <p>请求文件不存在</p>
		 */
		NOT_FOUND(404),
		/**
		 * <p>无法满足请求范围</p>
		 */
		REQUESTED_RANGE_NOT_SATISFIABLE(416),
		/**
		 * <p>服务器错误</p>
		 */
		INTERNAL_SERVER_ERROR(500);
		
		/**
		 * <p>状态码</p>
		 */
		private final int code;
		
		/**
		 * @param code 状态码
		 */
		private StatusCode(int code) {
			this.code = code;
		}
		
		/**
		 * <p>获取状态码</p>
		 * 
		 * @return 状态码
		 */
		public final int code() {
			return this.code;
		}
		
		/**
		 * <p>判断状态码是否相等</p>
		 * 
		 * @param code 状态码
		 * 
		 * @return 是否相等
		 */
		public final boolean equalsCode(int code) {
			return this.code == code;
		}
		
		/**
		 * <p>判断响应状态码是否匹配</p>
		 * 
		 * @param <T> 响应体泛型
		 * 
		 * @param response 响应
		 * 
		 * @return 是否匹配
		 */
		public final <T> boolean verifyCode(HttpResponse<T> response) {
			return response != null && this.equalsCode(response.statusCode());
		}
		
	}
	
	/**
	 * <p>请求方式</p>
	 * 
	 * @author acgist
	 */
	public enum Method {
		
		/**
		 * <p>GET请求</p>
		 */
		GET,
		/**
		 * <p>HEAD请求</p>
		 */
		HEAD,
		/**
		 * <p>POST请求</p>
		 */
		POST;
		
	}
	
	/**
	 * <p>HTTP客户端信息（User-Agent）</p>
	 */
	private static final String USER_AGENT;
	
	static {
		final StringBuilder userAgentBuilder = new StringBuilder();
		userAgentBuilder
			.append("Mozilla/5.0")
			.append(" ")
			.append("(compatible; ")
			.append(SystemConfig.getNameEn())
			.append("/")
			.append(SystemConfig.getVersion())
			.append("; +")
			.append(SystemConfig.getSupport())
			.append(")");
		USER_AGENT = userAgentBuilder.toString();
		LOGGER.debug("HTTP客户端信息（User-Agent）：{}", USER_AGENT);
		HttpsURLConnection.setDefaultHostnameVerifier(allowedAllHostname());
		HttpsURLConnection.setDefaultSSLSocketFactory(newSSLContext().getSocketFactory());
	}
	
	/**
	 * <p>请求地址</p>
	 */
	private final String url;
	/**
	 * <p>请求连接</p>
	 */
	private final HttpURLConnection httpURLConnection;
	/**
	 * <p>响应状态码</p>
	 */
	private int code;
	
	/**
	 * @param url 请求地址
	 * 
	 * @throws NetException 网络异常
	 */
	private HttpClient(String url, int connectTimeout) throws NetException {
		this.url = url;
		this.httpURLConnection = this.buildHttpURLConnection(connectTimeout);
		this.buildDefaultHeader();
	}
	
	public static final HttpClient newInstance(String url) throws NetException {
		return newInstance(url, SystemConfig.CONNECT_TIMEOUT_MILLIS);
	}
	
	public static final HttpClient newInstance(String url, int connectTimeout) throws NetException {
		return new HttpClient(url, connectTimeout);
	}
	
	/**
	 * <p>设置请求头</p>
	 * 
	 * @param key 请求头名称
	 * @param value 请求头值
	 */
	public void header(String key, String value) {
		this.httpURLConnection.setRequestProperty(key, value);
	}
	
	public HttpClient get() throws NetException {
		return this.execute(Method.GET, null);
	}
	
	public HttpClient head() throws NetException {
		return this.execute(Method.HEAD, null);
	}
	
	public HttpClient post(String data) throws NetException {
		return this.execute(Method.POST, null);
	}
	
	public HttpClient post(Map<String, String> data) throws NetException {
		this.header(HttpHeaderWrapper.HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded;charset=" + SystemConfig.DEFAULT_CHARSET);
		if(MapUtils.isEmpty(data)) {
			return this.execute(Method.POST, null);
		} else {
			final String body = data.entrySet().stream()
				.map(entry -> entry.getKey() + "=" + UrlUtils.encode(entry.getValue()))
				.collect(Collectors.joining("&"));
			return this.execute(Method.POST, body);
		}
	}
	
	/**
	 * <p>执行请求</p>
	 * 
	 * @param method 请求方法
	 * @param body 请求数据
	 * 
	 * @return HttpClient
	 * 
	 * @throws NetException 网络异常
	 */
	public HttpClient execute(Method method, String body) throws NetException {
		try {
			// 设置请求方式
			this.httpURLConnection.setRequestMethod(method.name());
			if(method == Method.GET) {
				// 是否写出
				this.httpURLConnection.setDoOutput(false);
			} else if(method == Method.HEAD) {
				// 是否写出
				this.httpURLConnection.setDoOutput(false);
			} else if(method == Method.POST) {
				// 是否写出
				this.httpURLConnection.setDoOutput(true);
			} else {
				throw new NetException("不支持的请求方式：" + method);
			}
			// 连接
			this.httpURLConnection.connect();
			// 发送请求参数
			if(body != null) {
				final OutputStream output = this.httpURLConnection.getOutputStream();
				output.write(body.getBytes());
				IoUtils.close(output);
			}
			// 设置响应状态码
			this.code = this.httpURLConnection.getResponseCode();
		} catch (IOException e) {
			throw new NetException(e);
		}
		return this;
	}
	
	/**
	 * <p>获取响应信息</p>
	 * <p>使用完成需要关闭</p>
	 * 
	 * @return 响应信息
	 * 
	 * @throws NetException 网络异常
	 */
	public InputStream response() throws NetException {
		try {
			return this.httpURLConnection.getInputStream();
		} catch (IOException e) {
			throw new NetException(e);
		}
	}

	/**
	 * <p>获取响应信息</p>
	 * 
	 * @return 响应信息
	 * 
	 * @throws NetException 网络异常
	 */
	public byte[] responseToBytes() throws NetException {
		int length;
		final var input = this.response();
		final var bytes = new byte[8 * SystemConfig.ONE_KB];
		try {
			final var output = new ByteArrayOutputStream(input.available());
			while((length = input.read(bytes)) >= 0) {
				output.write(bytes, 0, length);
			}
			return output.toByteArray();
		} catch (IOException e) {
			throw new NetException(e);
		} finally {
			IoUtils.close(input);
		}
	}
	
	/**
	 * <p>获取响应信息</p>
	 * 
	 * @return 响应信息
	 * 
	 * @throws NetException 网络异常
	 */
	public String responseToString() throws NetException {
		int length;
		final var input = this.response();
		final var bytes = new byte[8 * SystemConfig.ONE_KB];
		final var builder = new StringBuilder();
		try {
			while((length = input.read(bytes)) >= 0) {
				builder.append(new String(bytes, 0, length));
			}
		} catch (IOException e) {
			throw new NetException(e);
		} finally {
			IoUtils.close(input);
		}
		return builder.toString();
	}
	
	/**
	 * <p>创建请求连接</p>
	 * 
	 * @param method 请求方法
	 * @param connectTimeout 连接超时时间（单位：毫秒）
	 * 
	 * @return 请求连接
	 * 
	 * @throws NetException 网络异常
	 */
	private HttpURLConnection buildHttpURLConnection(int connectTimeout) throws NetException {
		try {
			final var url = new URL(this.url);
			final var httpURLConnection = (HttpURLConnection) url.openConnection();
			// 是否读取
			httpURLConnection.setDoInput(true);
			// 是否缓存
			httpURLConnection.setUseCaches(false);
			// 请求超时时间
			httpURLConnection.setConnectTimeout(connectTimeout);
			// 是否自动重定向
			httpURLConnection.setInstanceFollowRedirects(true);
			return httpURLConnection;
		} catch (IOException e) {
			throw new NetException(e);
		}
	}

	private static final HostnameVerifier allowedAllHostname() {
		final var verifier = new HostnameVerifier() {
			public boolean verify(String host, SSLSession sslSession) {
				return true;
			}
		};
		return verifier;
	}
	
	private void buildDefaultHeader() {
		// 设置请求头
		this.header(HttpHeaderWrapper.HEADER_USER_AGENT, USER_AGENT);
	}
	
	/**
	 * <p>新建SSLContext</p>
	 * 
	 * @return SSLContext
	 */
	private static final SSLContext newSSLContext() {
		SSLContext sslContext = null;
		try {
			// SSL协议：SSL、SSLv2、SSLv3、TLS、TLSv1、TLSv1.1、TLSv1.2、TLSv1.3
			sslContext = SSLContext.getInstance("TLSv1.2");
			sslContext.init(null, ALLOWED_ALL_TRUST_MANAGER, SecureRandom.getInstanceStrong());
		} catch (KeyManagementException | NoSuchAlgorithmException e) {
			LOGGER.error("新建SSLContext异常", e);
			try {
				sslContext = SSLContext.getDefault();
			} catch (NoSuchAlgorithmException ex) {
				LOGGER.error("新建默认SSLContext异常", ex);
			}
		}
		return sslContext;
	}
	
	/**
	 * <p>信任所有证书</p>
	 */
	private static final TrustManager[] ALLOWED_ALL_TRUST_MANAGER = new TrustManager[] {
		new X509TrustManager() {
			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				if(ArrayUtils.isEmpty(chain)) {
					throw new CertificateException("证书加载失败");
				}
			}
			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				if(ArrayUtils.isEmpty(chain)) {
					throw new CertificateException("证书加载失败");
				}
			}
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}
		}
	};
	
}
