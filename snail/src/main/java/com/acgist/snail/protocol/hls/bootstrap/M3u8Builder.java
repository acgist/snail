package com.acgist.snail.protocol.hls.bootstrap;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.net.http.HTTPClient;
import com.acgist.snail.pojo.bean.M3u8;
import com.acgist.snail.pojo.bean.M3u8.Type;
import com.acgist.snail.pojo.wrapper.KeyValueWrapper;
import com.acgist.snail.utils.ArrayUtils;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.ObjectUtils;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.UrlUtils;

/**
 * <p>M3U8解析器</p>
 * 
 * TODO：文件编码
 * TODO：加密解密
 * 
 * @author acgist
 * @since 1.4.1
 */
public final class M3u8Builder {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(M3u8Builder.class);

	/**
	 * <p>类型标签</p>
	 */
	private static final String TAG_EXTM3U = "EXTM3U";
	/**
	 * <p>视频分片</p>
	 */
	private static final String TAG_EXTINF = "EXTINF";
	/**
	 * <p>数据加密</p>
	 * <p>#EXT-X-KEY:METHOD=AES-128,URI="https://www.acgist.com/key",IV=0x00000000</p>
	 */
	private static final String TAG_EXT_X_KEY = "EXT-X-KEY";
	/**
	 * <p>序列号</p>
	 */
	private static final String TAG_EXT_X_MEDIA_SEQUENCE = "EXT-X-MEDIA-SEQUENCE";
	/**
	 * <p>视频结束标记</p>
	 * <p>如果没有这个标记表示为直播流媒体</p>
	 */
	private static final String TAG_EXT_X_ENDLIST = "EXT-X-ENDLIST";
	/**
	 * <p>多级M3U8列表</p>
	 */
	private static final String TAG_EXT_X_STREAM_INF = "EXT-X-STREAM-INF";
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
	private final List<Tag> tags;
	
	private M3u8Builder(String source, List<String> lines) {
		this.source = source;
		this.lines = lines;
		this.tags = new ArrayList<>();
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
		this.buildTags();
		this.check();
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
	private void buildTags() throws DownloadException {
		int jndex;
		String line;
		Tag tag = null;
		// 解析标签
		for (int index = 0; index < this.lines.size(); index++) {
			line = this.lines.get(index).trim();
			// 空行跳过
			if(StringUtils.isEmpty(line)) {
				continue;
			}
			if(line.indexOf('#') == 0) {
				// 标签
				tag = new Tag();
				this.tags.add(tag);
				jndex = line.indexOf(':');
				if(jndex < 0) {
					tag.setName(line.substring(1));
				} else {
					tag.setName(line.substring(1, jndex).trim());
					tag.setValue(line.substring(jndex + 1).trim());
				}
			} else {
				// URL
				if(tag == null) {
					throw new DownloadException("M3U8文件解析失败（URL格式错误）");
				}
				tag.setUrl(line);
			}
		}
	}
	
	/**
	 * <p>文件校验</p>
	 * 
	 * @throws DownloadException 下载异常
	 */
	private void check() throws DownloadException {
		if(this.tags.isEmpty()) {
			throw new DownloadException("M3U8格式错误（没有标签）");
		}
		// 验证类型
		final String type = this.tags.get(0).getName();
		if(!TAG_EXTM3U.equalsIgnoreCase(type)) {
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
		final boolean stream = this.tags.stream().noneMatch(tag -> TAG_EXT_X_ENDLIST.equalsIgnoreCase(tag.getName()));
		// 判断多级M3U8列表
		final boolean multiM3u8 = this.tags.stream().anyMatch(tag -> TAG_EXT_X_STREAM_INF.equalsIgnoreCase(tag.getName()));
		final M3u8.Type type = multiM3u8 ? Type.M3U8 : stream ? Type.STREAM : Type.FILE;
		final Cipher cipher = this.buildCipher();
		// 读取文件列表
		final List<String> links;
		if(multiM3u8) {
			links = this.buildLinksM3u8();
		} else {
			links = this.buildLinksFile();
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
		final var optional = this.tags.stream()
			.filter(tag -> TAG_EXT_X_KEY.equalsIgnoreCase(tag.getName()))
			.findFirst();
		if(optional.isEmpty()) {
			return null;
		}
		final String value = optional.get().getValue();
		final var wrapper = KeyValueWrapper.newInstance(',', '=', value);
		wrapper.decode();
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
		byte[] ivBytes;
		final String requestUri = UrlUtils.redirect(this.source, uri);
		final byte[] secret = HTTPClient.get(requestUri, BodyHandlers.ofByteArray()).body();
		if(iv == null) {
			// IV不存在时：使用序列化
			final var optional = this.tags.stream()
				.filter(tag -> TAG_EXT_X_MEDIA_SEQUENCE.equalsIgnoreCase(tag.getName()))
				.findFirst();
			if(optional.isEmpty()) {
				LOGGER.error("HLS数据缺少序列号：{}", this.source);
				return null;
			}
			final String sequence = optional.get().getValue();
			final int length = sequence.length();
			if(length > 32) {
				LOGGER.error("HLS数据序列号错误：{}", sequence);
				return null;
			}
			// 填充：0
			final String padding = "0".repeat(32 - length);
			ivBytes = StringUtils.unhex(padding + sequence);
		} else {
			if(iv.length() == 32) {
				ivBytes = StringUtils.unhex(iv);
			} else if(iv.length() == 34) {
				// 0x....
				ivBytes = StringUtils.unhex(iv.substring(2));
			} else {
				LOGGER.error("HLS数据IV错误：{}-{}", this.source, iv);
				return null;
			}
		}
		try {
			return this.buildCipher(ivBytes, secret, "AES", "AES/CBC/NoPadding");
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException e) {
			throw new NetException("获取加密套件失败", e);
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
		Cipher cipher = null;
		final SecretKeySpec secretKeySpec = new SecretKeySpec(secret, algorithm);
		final IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
//		PKCS7Padding：最后一个字节填充大小
		cipher = Cipher.getInstance(transformation);
		cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
		return cipher;
	}
	
	/**
	 * <p>创建多级M3U8链接</p>
	 * 
	 * @return 多级M3U8链接
	 */
	private List<String> buildLinksM3u8() {
		return this.tags.stream()
			.filter(tag -> TAG_EXT_X_STREAM_INF.equalsIgnoreCase(tag.getName()))
			.sorted((source, target) -> {
				// 码率排序
				final String sourceBandwidth = source.attrs().get(ATTR_BANDWIDTH);
				final String targetBandwidth = target.attrs().get(ATTR_BANDWIDTH);
				if(sourceBandwidth == null || targetBandwidth == null) {
					return 0;
				} else {
					return Integer.valueOf(sourceBandwidth).compareTo(Integer.valueOf(targetBandwidth));
				}
			})
			.map(tag -> UrlUtils.redirect(this.source, tag.getUrl()))
			.collect(Collectors.toList());
	}
	
	/**
	 * <p>获取文件链接</p>
	 * 
	 * @return 文件链接
	 */
	private List<String> buildLinksFile() {
		return this.tags.stream()
			.filter(tag -> TAG_EXTINF.equalsIgnoreCase(tag.getName()))
			.map(tag -> UrlUtils.redirect(this.source, tag.getUrl()))
			.collect(Collectors.toList());
	}
	
	/**
	 * <p>标签</p>
	 */
	public static final class Tag {
		
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
		 * <p>获取标签值数组</p>
		 * 
		 * @return 标签值数组
		 */
		public String[] values() {
			if(this.value == null) {
				return new String[] {};
			}
			return this.value.split(",");
		}

		/**
		 * <p>获取标签属性数组</p>
		 * 
		 * @return 标签属性数组
		 */
		public Map<String, String> attrs() {
			final String[] values = this.values();
			if(ArrayUtils.isEmpty(values)) {
				return Map.of();
			}
			int index;
			final Map<String, String> attrs = new HashMap<>(values.length);
			for (String attr : values) {
				index = attr.indexOf('=');
				if(index < 0) {
					attrs.put(attr, null);
				} else {
					attrs.put(attr.substring(0, index), attr.substring(index + 1));
				}
			}
			return attrs;
		}

		@Override
		public String toString() {
			return ObjectUtils.toString(this);
		}
		
	}
	
}
