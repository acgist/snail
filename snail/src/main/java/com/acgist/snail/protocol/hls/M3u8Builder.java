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

import com.acgist.snail.config.SymbolConfig;
import com.acgist.snail.context.wrapper.KeyValueWrapper;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.DownloadException;
import com.acgist.snail.net.NetException;
import com.acgist.snail.net.hls.M3u8;
import com.acgist.snail.net.hls.M3u8.Type;
import com.acgist.snail.net.http.HttpClient;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.UrlUtils;

/**
 * M3U8 Builder
 * 
 * 多级M3U8列表：
 * 
 * #EXTM3U
 * #EXT-X-STREAM-INF:PROGRAM-ID=1,BANDWIDTH=800000,RESOLUTION=1080x608
 * 1000k/hls/index.m3u8
 * 
 * M3U8文件列表：
 * 
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
 * 
 * @author acgist
 */
public final class M3u8Builder {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(M3u8Builder.class);

	/**
	 * IV长度：{@value}
	 */
	private static final int IV_LENGTH = 32;
	/**
	 * 16进制IV开头：{@value}
	 */
	private static final String IV_HEX_PREFIX = "0x";
	/**
	 * 类型标签：{@value}
	 */
	private static final String LABEL_EXTM3U = "EXTM3U";
	/**
	 * 视频分片：{@value}
	 */
	private static final String LABEL_EXTINF = "EXTINF";
	/**
	 * 视频分片（码率）：{@value}
	 */
	private static final String LABEL_EXT_X_BITRATE = "EXT-X-BITRATE";
	/**
	 * 数据加密信息：{@value}
	 * 
	 * #EXT-X-KEY:METHOD=AES-128,URI="https://www.acgist.com/key",IV=0x00000000
	 */
	private static final String LABEL_EXT_X_KEY = "EXT-X-KEY";
	/**
	 * 视频结束标记：{@value}
	 * 没有结束标记：直播流媒体
	 */
	private static final String LABEL_EXT_X_ENDLIST = "EXT-X-ENDLIST";
	/**
	 * 多级M3U8列表：{@value}
	 */
	private static final String LABEL_EXT_X_STREAM_INF = "EXT-X-STREAM-INF";
	/**
	 * 序列号：{@value}
	 * 没有数据加密IV使用序列号
	 * 
	 * @see #LABEL_EXT_X_KEY
	 */
	private static final String LABEL_EXT_X_MEDIA_SEQUENCE = "EXT-X-MEDIA-SEQUENCE";
	/**
	 * 码率：{@value}
	 */
	private static final String ATTR_BANDWIDTH = "BANDWIDTH";
	/**
	 * 加密信息：{@value}
	 * 
	 * @see #LABEL_EXT_X_KEY
	 */
	private static final String ATTR_IV = "IV";
	/**
	 * 加密信息：{@value}
	 * 
	 * @see #LABEL_EXT_X_KEY
	 */
	private static final String ATTR_URI = "URI";
	/**
	 * 加密信息：{@value}
	 * 
	 * @see #LABEL_EXT_X_KEY
	 */
	private static final String ATTR_METHOD = "METHOD";
	
	/**
	 * 原始链接地址
	 */
	private final String source;
	/**
	 * M3U8描述信息
	 */
	private final List<String> lines;
	/**
	 * 标签
	 */
	private final List<Label> labels;
	
	/**
	 * @param source 原始链接地址
	 * @param lines  M3U8描述信息
	 */
	private M3u8Builder(String source, List<String> lines) {
		this.source = source;
		this.lines = lines;
		this.labels = new ArrayList<>();
	}
	
	/**
	 * 新建M3U8解析器
	 * 
	 * @param file   描述文件
	 * @param source 原始链接地址
	 * 
	 * @return {@link M3u8Builder}
	 * 
	 * @throws DownloadException 下载异常
	 */
	public static final M3u8Builder newInstance(File file, String source) throws DownloadException {
		try {
			return new M3u8Builder(source, Files.readAllLines(file.toPath()));
		} catch (IOException e) {
			throw new DownloadException("M3U8文件解析失败", e);
		}
	}
	
	/**
	 * 新建M3U8解析器
	 * 
	 * @param content 描述信息
	 * @param source  原始链接地址
	 * 
	 * @return {@link M3u8Builder}
	 */
	public static final M3u8Builder newInstance(String content, String source) {
		return new M3u8Builder(source, StringUtils.readLines(content));
	}
	
	/**
	 * 解析M3U8
	 * 
	 * @return {@link M3u8}
	 * 
	 * @throws NetException      网络异常
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
	 * 解析标签
	 * 
	 * 空行：忽略
	 * URL：文件地址、M3U8地址
	 * 标签、注释：#开头
	 * 属性：跟在标签后面
	 */
	private void buildLabels() {
		int jndex;
		String line;
		Label label = null;
		// 解析标签
		for (int index = 0; index < this.lines.size(); index++) {
			line = this.lines.get(index).strip();
			if(StringUtils.isEmpty(line)) {
				// 空行跳过
				continue;
			}
			if(line.indexOf(SymbolConfig.Symbol.POUND.toChar()) == 0) {
				// 标签
				label = new Label();
				this.labels.add(label);
				jndex = line.indexOf(SymbolConfig.Symbol.COLON.toChar());
				if(jndex < 0) {
					// 没有属性
					label.setName(line.substring(1).strip());
				} else {
					// 含有属性
					label.setName(line.substring(1, jndex).strip());
					label.setValue(line.substring(jndex + 1).strip());
				}
			} else {
				// URL
				if(label == null) {
					LOGGER.warn("链接没有标签信息：{}-{}", this.source, line);
				} else {
					label.setUrl(line);
				}
			}
		}
	}
	
	/**
	 * 标签校验
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
	 * 新建M3U8信息
	 * 
	 * @return {@link M3u8}
	 * 
	 * @throws NetException 网络异常
	 */
	private M3u8 buildM3u8() throws NetException {
		final M3u8.Type type = this.buildType();
		final Cipher cipher = this.buildCipher();
		List<String> links;
		if(type == Type.M3U8) {
			links = this.buildM3u8Links();
		} else {
			// 获取LABEL_EXTINF标签数据
			links = this.buildFileLinks(LABEL_EXTINF);
			// 没有LABEL_EXTINF标签数据：获取LABEL_EXT_X_BITRATE标签数据
			if(CollectionUtils.isEmpty(links)) {
				links = this.buildFileLinks(LABEL_EXT_X_BITRATE);
			}
			if(CollectionUtils.isEmpty(links)) {
				throw new NetException("没有下载文件");
			}
		}
		return new M3u8(type, cipher, links);
	}

	/**
	 * 新建M3U8类型
	 * 
	 * @return {@link Type}
	 */
	private Type buildType() {
		// 注意顺序：多级M3U8列表、直播流媒体、文件列表
		// 判断是否是多级M3U8列表
		final boolean multiM3u8 = this.labels.stream()
			.anyMatch(label -> LABEL_EXT_X_STREAM_INF.equalsIgnoreCase(label.getName()));
		if(multiM3u8) {
			return Type.M3U8;
		}
		// 判断是否是直播流媒体
		final boolean streamM3u8 = this.labels.stream()
			.noneMatch(label -> LABEL_EXT_X_ENDLIST.equalsIgnoreCase(label.getName()));
		if(streamM3u8) {
			return Type.STREAM;
		}
		return Type.FILE;
	}

	/**
	 * 新建加密套件
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
			// 没有加密标签：不用解密
			return null;
		}
		final KeyValueWrapper wrapper = optional.get().attrs();
		final String method = wrapper.getIgnoreCase(ATTR_METHOD);
		final M3u8.Protocol protocol = M3u8.Protocol.of(method);
		LOGGER.debug("HLS加密算法：{}", method);
		if(protocol == null || protocol == M3u8.Protocol.NONE) {
			throw new NetException("不支持的HLS加密算法：" + method);
		}
		if(protocol == M3u8.Protocol.AES_128) {
			return this.buildCipherAes128(wrapper.getIgnoreCase(ATTR_IV), wrapper.getIgnoreCase(ATTR_URI));
		} else {
			throw new NetException("不支持的HLS加密算法：" + method);
		}
	}
	
	/**
	 * 新建加密套件
	 * 
	 * @param iv  IV
	 * @param uri URI
	 * 
	 * @return 加密套件
	 * 
	 * @throws NetException 网络异常
	 */
	private Cipher buildCipherAes128(String iv, String uri) throws NetException {
		final byte[] secret = HttpClient
			.newInstance(UrlUtils.redirect(this.source, uri))
			.get()
			.responseToBytes();
		try {
			return this.buildCipher(this.buildCipherIv(iv), secret, "AES", "AES/CBC/NoPadding");
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException e) {
			throw new NetException("获取加密套件失败", e);
		}
	}
	
	/**
	 * 新建IV
	 * 
	 * @param iv IV
	 * 
	 * @return IV
	 * 
	 * @see #LABEL_EXT_X_MEDIA_SEQUENCE
	 */
	private byte[] buildCipherIv(String iv) {
		if(iv == null) {
			iv = this.buildCiperSequenceIv();
		}
		if(
			iv.startsWith(IV_HEX_PREFIX) ||
			iv.startsWith(IV_HEX_PREFIX.toUpperCase())
		) {
			// 去掉十六进制前缀
			iv = iv.substring(IV_HEX_PREFIX.length());
		}
		final int length = iv.length();
		if(length == IV_LENGTH) {
			return StringUtils.unhex(iv);
		} else if(length < IV_LENGTH) {
			final String padding = SymbolConfig.Symbol.ZERO.toString().repeat(IV_LENGTH - length);
			return StringUtils.unhex(padding + iv);
		} else {
			return StringUtils.unhex(iv.substring(length - IV_LENGTH));
		}
	}
	
	/**
	 * 新建序列号IV
	 * 
	 * @return IV
	 */
	private String buildCiperSequenceIv() {
		final var optional = this.labels.stream()
			.filter(label -> LABEL_EXT_X_MEDIA_SEQUENCE.equalsIgnoreCase(label.getName()))
			.findFirst();
		if(optional.isEmpty()) {
			return SymbolConfig.Symbol.ZERO.toString().repeat(IV_LENGTH);
		} else {
			final String sequence = optional.get().getValue();
			final int length = sequence.length();
			if(length < IV_LENGTH) {
				final String padding = SymbolConfig.Symbol.ZERO.toString().repeat(IV_LENGTH - length);
				return padding + sequence;
			} else if(length > IV_LENGTH) {
				return sequence.substring(length - IV_LENGTH);
			} else {
				return sequence;
			}
		}
	}
	
	/**
	 * 新建加密套件
	 * 
	 * @param iv             IV
	 * @param secret         密钥
	 * @param algorithm      加密算法名称
	 * @param transformation 算法描述
	 * 
	 * @return 加密套件
	 * 
	 * @throws InvalidKeyException                密钥异常
	 * @throws NoSuchPaddingException             填充异常
	 * @throws NoSuchAlgorithmException           算法异常
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
	 * 新建多级M3U8链接
	 * 
	 * @return 多级M3U8链接
	 */
	private List<String> buildM3u8Links() {
		return this.labels.stream()
			.filter(label -> LABEL_EXT_X_STREAM_INF.equalsIgnoreCase(label.getName()))
			.sorted((sourceLabel, targetLabel) -> {
				// 码率排序
				final String sourceBandwidth = sourceLabel.attrs().getIgnoreCase(ATTR_BANDWIDTH);
				final String targetBandwidth = targetLabel.attrs().getIgnoreCase(ATTR_BANDWIDTH);
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
	 * @return 文件链接
	 */
	private List<String> buildFileLinks(String labelName) {
		return this.labels.stream()
			.filter(label -> labelName.equalsIgnoreCase(label.getName()))
			.filter(label -> StringUtils.isNotEmpty(label.getUrl()))
			.map(label -> UrlUtils.redirect(this.source, label.getUrl()))
			.collect(Collectors.toList());
	}
	
	/**
	 * 标签
	 * 
	 * @author acgist
	 */
	public static final class Label {
		
		/**
		 * 标签名称
		 */
		private String name;
		/**
		 * 标签值
		 */
		private String value;
		/**
		 * 标签链接
		 */
		private String url;
		/**
		 * 属性Key-Value包装器
		 */
		private KeyValueWrapper wrapper;

		/**
		 * @return 标签名称
		 */
		public String getName() {
			return this.name;
		}

		/**
		 * @param name 标签名称
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * @return 标签值
		 */
		public String getValue() {
			return this.value;
		}

		/**
		 * @param value 标签值
		 */
		public void setValue(String value) {
			this.value = value;
		}

		/**
		 * @return 标签链接
		 */
		public String getUrl() {
			return this.url;
		}

		/**
		 * @param url 标签链接
		 */
		public void setUrl(String url) {
			this.url = url;
		}

		/**
		 * @return 属性Key-Value包装器
		 */
		public KeyValueWrapper attrs() {
			if(this.wrapper == null) {
				this.wrapper = KeyValueWrapper.newInstance(SymbolConfig.Symbol.COMMA.toChar(), SymbolConfig.Symbol.EQUALS.toChar(), this.value);
				this.wrapper.decode();
			}
			return this.wrapper;
		}
		
	}
	
}
