package com.acgist.snail.net.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.wrapper.HttpHeaderWrapper;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.NetException;
import com.acgist.snail.utils.IoUtils;
import com.acgist.snail.utils.MapUtils;
import com.acgist.snail.utils.NumberUtils;

/**
 * HTTP客户端
 * 配置参考：https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html
 * 
 * @author acgist
 */
public final class HttpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClient.class);
    
    /**
     * 请求方式
     * 
     * @author acgist
     */
    public enum Method {
        
        /**
         * GET请求
         */
        GET,
        /**
         * HEAD请求
         */
        HEAD,
        /**
         * POST请求
         */
        POST;
        
    }
    
    /**
     * HTTP客户端信息（User-Agent）
     */
    private static final String USER_AGENT;
    
    static {
        final StringBuilder userAgentBuilder = new StringBuilder();
        userAgentBuilder
            .append(SystemConfig.getNameEn())
            .append("/")
            .append(SystemConfig.getVersion())
            .append(" (+")
            .append(SystemConfig.getSupport())
            .append(")");
        USER_AGENT = userAgentBuilder.toString();
        LOGGER.debug("HTTP客户端信息（User-Agent）：{}", USER_AGENT);
        // 配置HTTPS
        final SSLContext sslContext = HttpClient.buildSSLContext();
        if(sslContext != null) {
            HttpsURLConnection.setDefaultHostnameVerifier(SnailHostnameVerifier.INSTANCE);
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        }
    }
    
    /**
     * 请求地址
     */
    private final String url;
    /**
     * 请求连接
     */
    private final HttpURLConnection httpURLConnection;
    /**
     * 状态码
     */
    private int code;
    /**
     * 响应头
     */
    private HttpHeaderWrapper httpHeaderWrapper;
    
    /**
     * @param url            请求地址
     * @param connectTimeout 连接超时时间（毫秒）
     * @param receiveTimeout 响应超时时间（毫秒）
     * 
     * @throws NetException 网络异常
     */
    private HttpClient(String url, int connectTimeout, int receiveTimeout) throws NetException {
        this.url               = url;
        this.httpURLConnection = this.buildHttpURLConnection(connectTimeout, receiveTimeout);
        this.setDefaultHeader();
    }
    
    /**
     * 新建下载HTTP客户端
     * 
     * @param url 请求地址
     * 
     * @return {@link HttpClient}
     * 
     * @throws NetException 网络异常
     */
    public static final HttpClient newDownloader(String url) throws NetException {
        return HttpClient.newInstance(url, SystemConfig.CONNECT_TIMEOUT_MILLIS, SystemConfig.DOWNLOAD_TIMEOUT_MILLIS);
    }
    
    /**
     * 新建HTTP客户端
     * 
     * @param url 请求地址
     * 
     * @return {@link HttpClient}
     * 
     * @throws NetException 网络异常
     */
    public static final HttpClient newInstance(String url) throws NetException {
        return HttpClient.newInstance(url, SystemConfig.CONNECT_TIMEOUT_MILLIS, SystemConfig.RECEIVE_TIMEOUT_MILLIS);
    }
    
    /**
     * 新建HTTP客户端
     * 
     * @param url            请求地址
     * @param connectTimeout 连接超时时间（毫秒）
     * @param receiveTimeout 响应超时时间（毫秒）
     * 
     * @return {@link HttpClient}
     * 
     * @throws NetException 网络异常
     */
    public static final HttpClient newInstance(String url, int connectTimeout, int receiveTimeout) throws NetException {
        return new HttpClient(url, connectTimeout, receiveTimeout);
    }
    
    /**
     * 使用缓存
     * 
     * @return {@link HttpClient}
     */
    public HttpClient useCache() {
        this.httpURLConnection.setUseCaches(true);
        return this;
    }
    
    /**
     * 设置请求头
     * 
     * @param key   请求头名称
     * @param value 请求头值
     * 
     * @return {@link HttpClient}
     */
    public HttpClient header(String key, String value) {
        this.httpURLConnection.setRequestProperty(key, value);
        return this;
    }
    
    /**
     * 启用长连接
     * 
     * @return {@link HttpClient}
     */
    public HttpClient keepAlive() {
        return this.header("Connection", "keep-alive");
    }
    
    /**
     * 禁用长连接
     * 
     * @return {@link HttpClient}
     */
    public HttpClient disableKeepAlive() {
        return this.header("Connection", "close");
    }
    
    /**
     * 设置请求范围
     * 
     * @param pos 开始位置
     * 
     * @return {@link HttpClient}
     */
    public HttpClient range(long pos) {
        return this.header(HttpHeaderWrapper.HEADER_RANGE, "bytes=" + pos + "-");
    }
    
    /**
     * 执行GET请求
     * 
     * @return {@link HttpClient}
     * 
     * @throws NetException 网络异常
     */
    public HttpClient get() throws NetException {
        return this.execute(Method.GET, null);
    }
    
    /**
     * 执行HEAD请求
     * 
     * @return {@link HttpClient}
     * 
     * @throws NetException 网络异常
     */
    public HttpClient head() throws NetException {
        return this.execute(Method.HEAD, null);
    }
    
    /**
     * 执行POST请求
     * 
     * @param data 请求数据
     * 
     * @return {@link HttpClient}
     * 
     * @throws NetException 网络异常
     */
    public HttpClient post(String data) throws NetException {
        this.header(HttpHeaderWrapper.HEADER_CONTENT_TYPE, "application/json");
        return this.execute(Method.POST, data);
    }
    
    /**
     * 执行POST表单请求
     * 
     * @param data 请求数据
     * 
     * @return {@link HttpClient}
     * 
     * @throws NetException 网络异常
     */
    public HttpClient post(Map<String, String> data) throws NetException {
        // 设置表单请求
        this.header(HttpHeaderWrapper.HEADER_CONTENT_TYPE, "application/x-www-form-urlencoded;charset=" + SystemConfig.DEFAULT_CHARSET);
        if(MapUtils.isEmpty(data)) {
            return this.execute(Method.POST, null);
        } else {
            return this.execute(Method.POST, MapUtils.toUrlQuery(data));
        }
    }
    
    /**
     * 执行请求
     * 
     * @param method 请求方法
     * @param body   请求数据
     * 
     * @return {@link HttpClient}
     * 
     * @throws NetException 网络异常
     */
    public HttpClient execute(Method method, String body) throws NetException {
        OutputStream output = null;
        try {
            // 设置请求方式
            this.httpURLConnection.setRequestMethod(method.name());
            if(method == Method.GET) {
                // 是否写出：GET不要写出
                this.httpURLConnection.setDoOutput(false);
            } else if(method == Method.HEAD) {
                // 是否写出：HEAD不要写出
                this.httpURLConnection.setDoOutput(false);
            } else if(method == Method.POST) {
                // 是否写出：POST需要写出
                this.httpURLConnection.setDoOutput(true);
            } else {
                throw new NetException("不支持的请求方式：" + method);
            }
            // 发起连接
            this.httpURLConnection.connect();
            // 发送请求参数
            if(body != null) {
                output = this.httpURLConnection.getOutputStream();
                output.write(body.getBytes());
            }
            // 设置状态码
            this.code = this.httpURLConnection.getResponseCode();
        } catch (IOException e) {
            throw new NetException(e);
        } finally {
            IoUtils.close(output);
        }
        return this;
    }
    
    /**
     * @return 状态码
     */
    public int getCode() {
        return this.code;
    }
    
    /**
     * @return 是否成功
     * 
     * @see HttpURLConnection#HTTP_OK
     */
    public boolean ok() {
        return HttpURLConnection.HTTP_OK == this.code;
    }
    
    /**
     * @return 是否部分内容
     * 
     * @see HttpURLConnection#HTTP_PARTIAL
     */
    public boolean partial() {
        return HttpURLConnection.HTTP_PARTIAL == this.code;
    }
    
    /**
     * @return 是否服务器错误
     * 
     * @see HttpURLConnection#HTTP_INTERNAL_ERROR
     */
    public boolean internalError() {
        return HttpURLConnection.HTTP_INTERNAL_ERROR == this.code;
    }
    
    /**
     * @return 是否可以下载
     * 
     * @see #ok()
     * @see #partial()
     */
    public boolean downloadable() {
        return this.ok() || this.partial();
    }
    
    /**
     * 使用完成需要关闭（归还连接）：下次相同地址端口继续使用（复用底层Socket）
     * 
     * @return 响应数据流
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
     * @return 响应字节数组
     * 
     * @throws NetException 网络异常
     */
    public byte[] responseToBytes() throws NetException {
        try (
            final InputStream input = this.response()
        ) {
            final int size = input.available();
            if(size == 0) {
                return input.readAllBytes();
            }
            final byte[] bytes = new byte[size];
            final int length   = input.read(bytes);
            if(length == size) {
                return bytes;
            } else {
                return Arrays.copyOf(bytes, length);
            }
        } catch (IOException e) {
            throw new NetException(e);
        }
    }
    
    /**
     * @return 响应文本
     * 
     * @throws NetException 网络异常
     */
    public String responseToString() throws NetException {
        final byte[] bytes          = new byte[SystemConfig.DEFAULT_EXCHANGE_LENGTH];
        final StringBuilder builder = new StringBuilder();
        try (
            final InputStream input = this.response()
        ) {
            int length;
            while((length = input.read(bytes)) >= 0) {
                builder.append(new String(bytes, 0, length));
            }
        } catch (IOException e) {
            throw new NetException(e);
        }
        return builder.toString();
    }
    
    /**
     * @return 响应头
     */
    public HttpHeaderWrapper responseHeader() {
        if(this.httpHeaderWrapper == null) {
            this.httpHeaderWrapper = HttpHeaderWrapper.newInstance(this.httpURLConnection.getHeaderFields());
        }
        return this.httpHeaderWrapper;
    }
    
    /**
     * 关闭连接
     * 关闭连接和底层Socket：不能保持长连接
     * 
     * @return {@link HttpClient}
     */
    public HttpClient shutdown() {
        this.httpURLConnection.disconnect();
        return this;
    }
    
    /**
     * 设置默认请求头
     */
    private void setDefaultHeader() {
        // 接收所有类型参数
        this.header("Accept", "*/*");
        // 设置客户端信息
        this.header(HttpHeaderWrapper.HEADER_USER_AGENT, USER_AGENT);
    }
    
    /**
     * 新建请求连接
     * 
     * @param connectTimeout 连接超时时间（毫秒）
     * @param receiveTimeout 响应超时时间（毫秒）
     * 
     * @return 请求连接
     * 
     * @throws NetException 网络异常
     */
    private HttpURLConnection buildHttpURLConnection(int connectTimeout, int receiveTimeout) throws NetException {
        try {
            final URL requestUrl               = new URL(this.url);
            final HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
            // 是否读取
            connection.setDoInput(true);
            // 是否缓存
            connection.setUseCaches(false);
            // 响应超时时间
            connection.setReadTimeout(receiveTimeout);
            // 连接超时时间
            connection.setConnectTimeout(connectTimeout);
            // 是否自动重定向
            connection.setInstanceFollowRedirects(true);
            return connection;
        } catch (IOException e) {
            throw new NetException(e);
        }
    }
    
    /**
     * 新建SSLContext
     * 
     * @return {@link SSLContext}
     */
    private static final SSLContext buildSSLContext() {
        try {
            // SSL协议：SSL、SSLv2、SSLv3、TLS、TLSv1、TLSv1.1、TLSv1.2、TLSv1.3
            final SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(
                null,
                new X509TrustManager[] {
                    SnailTrustManager.INSTANCE
                },
                NumberUtils.random()
            );
            return sslContext;
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            LOGGER.error("新建SSLContext异常", e);
        }
        try {
            return SSLContext.getDefault();
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("新建SSLContext异常", e);
        }
        return null;
    }

    /**
     * 域名验证
     * 
     * @author acgist
     */
    public static class SnailHostnameVerifier implements HostnameVerifier {

        private static final SnailHostnameVerifier INSTANCE = new SnailHostnameVerifier();
        
        private SnailHostnameVerifier() {
        }
        
        @Override
        public boolean verify(String requestHost, SSLSession remoteSslSession) {
            // 证书域名必须匹配
            return requestHost.equalsIgnoreCase(remoteSslSession.getPeerHost());
        }
        
    }
    
    /**
     * 证书验证
     * 
     * @author acgist
     */
    public static class SnailTrustManager implements X509TrustManager {

        private static final SnailTrustManager INSTANCE = new SnailTrustManager();

        private SnailTrustManager() {
        }
        
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            if(chain == null) {
                throw new CertificateException("证书验证失败");
            }
        }
        
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            if(chain == null) {
                throw new CertificateException("证书验证失败");
            }
        }
        
    }
    
}
