package com.acgist.snail.protocol.hls;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.net.http.HttpClient;
import com.acgist.snail.pojo.bean.M3u8;
import com.acgist.snail.pojo.bean.M3u8.Type;
import com.acgist.snail.pojo.wrapper.KeyValueWrapper;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.UrlUtils;

/**
 * <p>M3U8解析器</p>
 * 
 * <p>M3U8列表：</p>
 * <pre>
 * #EXTM3U
 * #EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=800000,RESOLUTION=1080x608
 * 1000k/hls/index.m3u8
 * </pre>
 * <p>M3U8列表：</p>
 * <pre>
 * #EXTM3U
 * #EXT-X-VERSION:3
 * #EXT-X-KEY:METHOD=AES-128,URI="https://www.acgist.com/key",IV=0x00000000
 * #EXT-X-TARGETDURATION:8
 * #EXT-X-MEDIA-SEQUENCE:0
 * #EXTINF:7.200000,
 * 7a9cb1ab891000000.ts
 * #EXTINF:0.920000,
 * 7a9cb1ab891000001.ts
 * ...
 * #EXTINF:4.000000,
 * 7a9cb1ab891000616.ts
 * #EXTINF:2.320000,
 * 7a9cb1ab891000617.ts
 * #EXT-X-ENDLIST
 * </pre>
 * 
 * @author acgist
 */
public final class M3u8Builder {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(M3u8Builder.class);

	/**
	 * <p>IV长度：{@value}</p>
	 */
	private static final int IV_LEHGTH = 32;
	/**
	 * <p>IV长度（16进制开头）：{@value}</p>
	 */
	private static final int IV_PREFIX_LENGTH = 34;
	/**
	 * <p>类型标签</p>
	 */
	private static final String LABEL_EXTM3U = "EXTM3U";
	/**
	 * <p>视频分片</p>
	 */
	private static final String LABEL_EXTINF = "EXTINF";
	/**
	 * <p>视频分片（码率）</p>
	 */
	private static final String LABEL_EXT_X_BITRATE = "EXT-X-BITRATE";
	/**
	 * <p>数据加密</p>
	 * <p>#EXT-X-KEY:METHOD=AES-128,URI="https://www.acgist.com/key",IV=0x00000000</p>
	 */
	private static final String LABEL_EXT_X_KEY = "EXT-X-KEY";
	/**
	 * <p>视频结束标记</p>
	 * <p>如果没有这个标记表示为直播流媒体</p>
	 */
	private static final String LABEL_EXT_X_ENDLIST = "EXT-X-ENDLIST";
	/**
	 * <p>多级M3U8列表</p>
	 */
	private static final String LABEL_EXT_X_STREAM_INF = "EXT-X-STREAM-INF";
	/**
	 * <p>序列号</p>
	 * <p>如果没有数据加密IV使用序列号</p>
	 */
	private static final String LABEL_EXT_X_MEDIA_SEQUENCE = "EXT-X-MEDIA-SEQUENCE";
	/**
	 * <p>码率</p>
	 */
	private static final String ATTR_BANDWIDTH = "BANDWIDTH";
	
	/**
	 * <p>原始链接地址</p>
	 */
	private final String source;
	/**
	 * <p>M3U8描述信息</p>
	 * <p>每行一条</p>
	 */
	private final List<String> lines;
	/**
	 * <p>标签</p>
	 */
	private final List<Label> labels;
	
	/**
	 * @param source 原始链接地址
	 * @param lines M3U8描述信息
	 */
	private M3u8Builder(String source, List<String> lines) {
		this.source = source;
		this.lines = lines;
		this.labels = new ArrayList<>();
	}
	
	/**
	 * <p>新建一个M3U8解析器</p>
	 * 
	 * @param file 描述文件
	 * @param source 原始链接地址
	 * 
	 * @return M3U8解析器
	 * 
	 * @throws DownloadException 下载异常
	 */
	public static final M3u8Builder newInstance(File file, String source) throws DownloadException {
		List<String> lines;
		try {
			lines = Files.readAllLines(file.toPath());
		} catch (IOException e) {
			throw new DownloadException("M3U8文件解析失败", e);
		}
		return new M3u8Builder(source, lines);
	}
	
	/**
	 * <p>新建一个M3U8解析器</p>
	 * 
	 * @param content 描述信息
	 * @param source 原始链接地址
	 * 
	 * @return M3U8解析器
	 */
	public static final M3u8Builder newInstance(String content, String source) {
		final List<String> lines = StringUtils.readLines(content);
		return new M3u8Builder(source, lines);
	}
	
	/**
	 * <p>解析M3U8</p>
	 * 
	 * @return M3U8
	 * 
	 * @throws NetException 网络异常
	 * @throws DownloadException 下载异常
	 */
	public M3u8 build() throws NetException, DownloadException {
		if(CollectionUtils.isEmpty(this.lines)) {
			throw new DownloadException("M3U8文件解析失败（没有描述信息）");
		}
		this.buildLabels();
		this.checkLabels();
		return this.buildM3u8();
	}
	
	/**
	 * <p>解析标签</p>
	 * <p>空行：忽略</p>
	 * <p>URL：文件地址、M3U8地址</p>
	 * <p>注释、标签：#开头</p>
	 * <p>属性：跟在标签后面</p>
	 * 
	 * @throws DownloadException 下载异常
	 */
	private void buildLabels() throws DownloadException {
		int jndex;
		String line;
		Label label = null;
		// 解析标签
		for (int index = 0; index < this.lines.size(); index++) {
			line = this.lines.get(index).trim();
			// 空行跳过
			if(StringUtils.isEmpty(line)) {
				continue;
			}
			if(line.indexOf('#') == 0) {
				// 标签
				label = new Label();
				this.labels.add(label);
				jndex = line.indexOf(':');
				if(jndex < 0) {
					// 没有属性
					label.setName(line.substring(1).trim());
				} else {
					// 含有属性
					label.setName(line.substring(1, jndex).trim());
					label.setValue(line.substring(jndex + 1).trim());
				}
			} else {
				// URL
				if(label == null) {
					throw new DownloadException("M3U8文件解析失败（URL格式错误）");
				}
				label.setUrl(line);
			}
		}
	}
	
	/**
	 * <p>文件校验</p>
	 * 
	 * @throws DownloadException 下载异常
	 */
	private void checkLabels() throws DownloadException {
		if(this.labels.isEmpty()) {
			throw new DownloadException("M3U8格式错误（没有标签）");
		}
		// 验证类型
		final String type = this.labels.get(0).getName();
		if(!LABEL_EXTM3U.equalsIgnoreCase(type)) {
			throw new DownloadException("M3U8格式错误（头部错误）");
		}
	}
	
	/**
	 * <p>创建M3U8信息</p>
	 * 
	 * @return M3U8
	 * 
	 * @throws NetException 网络异常
	 */
	private M3u8 buildM3u8() throws NetException {
		// 判断是否是流媒体
		final boolean stream = this.labels.stream().noneMatch(label -> LABEL_EXT_X_ENDLIST.equalsIgnoreCase(label.getName()));
		// 判断多级M3U8列表
		final boolean multiM3u8 = this.labels.stream().anyMatch(label -> LABEL_EXT_X_STREAM_INF.equalsIgnoreCase(label.getName()));
		final M3u8.Type type = multiM3u8 ? Type.M3U8 : stream ? Type.STREAM : Type.FILE;
		final Cipher cipher = this.buildCipher();
		// 读取文件列表
		List<String> links;
		if(multiM3u8) {
			links = this.buildLinksM3u8();
		} else {
			// 获取LABEL_EXTINF标签数据
			links = this.buildLinksFile(LABEL_EXTINF);
			// LABEL_EXTINF标签没有数据：获取LABEL_EXT_X_BITRATE标签数据
			if(CollectionUtils.isEmpty(links)) {
				links = this.buildLinksFile(LABEL_EXT_X_BITRATE);
			}
			if(CollectionUtils.isEmpty(links)) {
				throw new NetException("没有下载文件");
			}
		}
		return new M3u8(type, cipher, links);
	}

	/**
	 * <p>创建加密套件</p>
	 * 
	 * @return 加密套件
	 * 
	 * @throws NetException 网络异常
	 */
	private Cipher buildCipher() throws NetException {
		final var optional = this.labels.stream()
			.filter(label -> LABEL_EXT_X_KEY.equalsIgnoreCase(label.getName()))
			.findFirst();
		if(optional.isEmpty()) {
			return null;
		}
		final String value = optional.get().getValue();
		final var wrapper = KeyValueWrapper.newInstance(',', '=', value);
		wrapper.decode();
		// 加密算法
		final String method = wrapper.getIgnoreCase("METHOD");
		final M3u8.Protocol protocol = M3u8.Protocol.of(method);
		LOGGER.debug("HLS加密算法：{}", method);
		if(protocol == null || protocol == M3u8.Protocol.NONE) {
			return null;
		}
		if(protocol == M3u8.Protocol.AES_128) {
			final String iv = wrapper.getIgnoreCase("IV");
			final String uri = wrapper.getIgnoreCase("URI");
			return this.buildCipherAes128(iv, uri);
		} else {
			LOGGER.warn("HLS加密算法未实现：{}", protocol);
		}
		return null;
	}
	
	/**
	 * <p>创建加密套件</p>
	 * 
	 * @param iv IV
	 * @param uri URI
	 * 
	 * @return 加密套件
	 * 
	 * @throws NetException 网络异常
	 */
	private Cipher buildCipherAes128(String iv, String uri) throws NetException {
		final String requestURI = UrlUtils.redirect(this.source, uri);
		final byte[] secret = HttpClient
			.newInstance(requestURI)
			.get()
			.responseToBytes();
		try {
			return this.buildCipher(this.buildIv(iv), secret, "AES", "AES/CBC/NoPadding");
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException e) {
			throw new NetException("获取加密套件失败", e);
		}
	}
	
	/**
	 * <p>创建IV</p>
	 * <p>如果IV不存在使用序列号代替</p>
	 * 
	 * @param iv 原始IV
	 * 
	 * @return IV
	 */
	private byte[] buildIv(String iv) {
		if(iv == null) {
			final var optional = this.labels.stream()
				.filter(label -> LABEL_EXT_X_MEDIA_SEQUENCE.equalsIgnoreCase(label.getName()))
				.findFirst();
			if(optional.isEmpty()) {
				LOGGER.error("HLS数据缺少序列号：{}", this.source);
				return null;
			}
			final String sequence = optional.get().getValue();
			final int length = sequence.length();
			if(length > IV_LEHGTH) {
				LOGGER.error("HLS数据序列号错误：{}-{}", this.source, sequence);
				return null;
			} else {
				// 填充：0
				final String padding = "0".repeat(IV_LEHGTH - length);
				return StringUtils.unhex(padding + sequence);
			}
		} else {
			if(iv.length() == IV_LEHGTH) {
				return StringUtils.unhex(iv);
			} else if(iv.length() == IV_PREFIX_LENGTH) {
				// 0x....
				return StringUtils.unhex(iv.substring(IV_PREFIX_LENGTH - IV_LEHGTH));
			} else {
				LOGGER.error("HLS数据IV错误：{}-{}", this.source, iv);
				return null;
			}
		}
	}
	
	/**
	 * <p>创建加密套件</p>
	 * 
	 * @param iv IV
	 * @param secret 密钥
	 * @param algorithm 加密算法名称
	 * @param transformation 算法描述
	 * 
	 * @return 加密套件
	 * 
	 * @throws InvalidKeyException 密钥异常
	 * @throws NoSuchPaddingException 填充异常
	 * @throws NoSuchAlgorithmException 算法异常
	 * @throws InvalidAlgorithmParameterException 参数异常
	 */
	private Cipher buildCipher(byte[] iv, byte[] secret, String algorithm, String transformation) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		final SecretKeySpec secretKeySpec = new SecretKeySpec(secret, algorithm);
		final IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
		final Cipher cipher = Cipher.getInstance(transformation);
		cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
		return cipher;
	}
	
	/**
	 * <p>创建多级M3U8链接</p>
	 * 
	 * @return 多级M3U8链接
	 */
	private List<String> buildLinksM3u8() {
		return this.labels.stream()
			.filter(label -> LABEL_EXT_X_STREAM_INF.equalsIgnoreCase(label.getName()))
			.sorted((source, target) -> {
				// 码率排序
				final String sourceBandwidth = source.attrs().getIgnoreCase(ATTR_BANDWIDTH);
				final String targetBandwidth = target.attrs().getIgnoreCase(ATTR_BANDWIDTH);
				if(sourceBandwidth == null || targetBandwidth == null) {
					return 0;
				} else {
					return Integer.valueOf(sourceBandwidth).compareTo(Integer.valueOf(targetBandwidth));
				}
			})
			.map(label -> UrlUtils.redirect(this.source, label.getUrl()))
			.collect(Collectors.toList());
	}
	
	/**
	 * <p>获取文件链接</p>
	 * 
	 * @return 文件链接
	 */
	private List<String> buildLinksFile(String labelName) {
		return this.labels.stream()
			.filter(label -> labelName.equalsIgnoreCase(label.getName()))
			.filter(label -> StringUtils.isNotEmpty(label.getUrl()))
			.map(label -> UrlUtils.redirect(this.source, label.getUrl()))
			.collect(Collectors.toList());
	}
	
	/**
	 * <p>标签</p>
	 */
	public static final class Label {
		
		/**
		 * <p>标签名称</p>
		 */
		private String name;
		/**
		 * <p>标签值</p>
		 */
		private String value;
		/**
		 * <p>标签链接</p>
		 */
		private String url;
		/**
		 * <p>属性Key-Value包装器</p>
		 */
		private KeyValueWrapper wrapper;

		/**
		 * <p>获取标签名称</p>
		 * 
		 * @return 标签名称
		 */
		public String getName() {
			return name;
		}

		/**
		 * <p>设置标签名称</p>
		 * 
		 * @param name 标签名称
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * <p>获取标签值</p>
		 * 
		 * @return 标签值
		 */
		public String getValue() {
			return value;
		}

		/**
		 * <p>设置标签值</p>
		 * 
		 * @param value 标签值
		 */
		public void setValue(String value) {
			this.value = value;
		}

		/**
		 * <p>获取标签链接</p>
		 * 
		 * @return 标签链接
		 */
		public String getUrl() {
			return url;
		}

		/**
		 * <p>设置标签链接</p>
		 * 
		 * @param url 标签链接
		 */
		public void setUrl(String url) {
			this.url = url;
		}

		/**
		 * <p>获取属性Key-Value包装器</p>
		 * 
		 * @return 属性Key-Value包装器
		 */
		public KeyValueWrapper attrs() {
			if(this.wrapper == null) {
				this.wrapper = KeyValueWrapper.newInstance(',', '=', this.value);
				this.wrapper.decode();
			}
			return this.wrapper;
		}
		
	}
	
}
