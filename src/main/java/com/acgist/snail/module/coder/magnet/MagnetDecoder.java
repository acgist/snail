package com.acgist.snail.module.coder.magnet;

import java.io.File;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.module.coder.magnet.impl.BtbttvMagnetDecoder;
import com.acgist.snail.module.config.DownloadConfig;
import com.acgist.snail.module.exception.DownloadException;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.HttpUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * 磁力链接解码
 */
public abstract class MagnetDecoder {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MagnetDecoder.class);
	
	private static final  List<MagnetDecoder> DECODERS = new ArrayList<>();
	
	private static final String HASH_REGEX = "[a-zA-Z0-9]{40}";
	private static final String MAGNET_PREFIX = "magnet:?xt=urn:btih:";
	
	protected String hash;
	protected String magnet;
	
	static {
		MagnetDecoder.putDecoder(new BtbttvMagnetDecoder());
		DECODERS.sort((a, b) -> {
			return a.order().compareTo(b.order());
		});
	}
	
	/**
	 * 名称
	 */
	public abstract String name();
	
	/**
	 * 排序
	 */
	public abstract Integer order();
	
	/**
	 * 下载请求
	 */
	public abstract HttpRequest request();

	/**
	 * 下载种子
	 */
	public File execute(String url) throws DownloadException {
		this.init(url);
		HttpClient client = HttpUtils.newClient();
		HttpRequest request = request();
		HttpResponse<byte[]> response = HttpUtils.request(client, request, BodyHandlers.ofByteArray());
		if(HttpUtils.ok(response)) {
			byte[] bytes = response.body();
			String path = DownloadConfig.getDownloadPath(this.hash + ".torrent");
			FileUtils.write(path, bytes);
			return new File(path);
		}
		return null;
	}
	
	/**
	 * 初始化
	 */
	private void init(String url) throws DownloadException {
		if(verifyMagnet(url)) {
			this.magnet = url;
			String hash = url.substring(MAGNET_PREFIX.length());
			int index = hash.indexOf("?");
			if(index != -1) {
				hash = hash.substring(0, index);
			}
			this.hash = hash;
		} else if(verifyHash(url)) {
			this.hash = url;
			this.magnet = MAGNET_PREFIX + this.hash;
		} else {
			throw new DownloadException("不支持的磁力链接：" + url);
		}
	}
	
	/**
	 * 设置解码器排序
	 */
	private static final void putDecoder(MagnetDecoder decoder) {
		LOGGER.info("添加磁力链接解码器：{}", decoder.name());
		DECODERS.add(decoder);
	}

	/**
	 * 下载
	 */
	public static final File download(String url) throws DownloadException {
		synchronized (DECODERS) {
			File file = null;
			for (MagnetDecoder decoder : DECODERS) {
				file = decoder.execute(url);
				if(file != null) {
					break;
				}
			}
			return file;
		}
	}
	
	/**
	 * 验证
	 */
	public static final boolean verify(String url) {
		return verifyMagnet(url) && verifyHash(url);
	}
	
	public static final boolean verifyMagnet(String url) {
		return StringUtils.startsWith(url, MAGNET_PREFIX);
	}
	
	public static final boolean verifyHash(String url) {
		return url != null && url.matches(HASH_REGEX);
	}
	
	public static final String buildMagnet(String hash) {
		return MAGNET_PREFIX + hash;
	}
	
}
