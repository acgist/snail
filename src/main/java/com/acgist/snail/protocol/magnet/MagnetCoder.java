package com.acgist.snail.protocol.magnet;

import java.io.File;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.http.HttpManager;
import com.acgist.snail.protocol.magnet.impl.BtbttvMagnetCoder;
import com.acgist.snail.protocol.torrent.TorrentProtocol;
import com.acgist.snail.system.config.DownloadConfig;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * 磁力链接解析器：磁力链接转种子文件
 */
public abstract class MagnetCoder {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MagnetCoder.class);
	
	private static final  List<MagnetCoder> DECODERS = new ArrayList<>();
	
	public static final String MAGNET_REGEX = "magnet:\\?xt=urn:btih:.+"; // 磁力链接正则表达式
	public static final String MAGNET_PREFIX = "magnet:?xt=urn:btih:"; // 磁力链接前缀
	public static final String MAGNET_HASH_REGEX = "[a-zA-Z0-9]{40}"; // 磁力链接HASH正则表达式
	
	protected String hash; // HASH
	protected String magnet; // 完整磁力链接
	
	static {
		MagnetCoder.putDecoder(BtbttvMagnetCoder.newInstance());
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
		HttpClient client = HttpManager.newClient();
		HttpRequest request = request();
		HttpResponse<byte[]> response = HttpManager.request(client, request, BodyHandlers.ofByteArray());
		if(HttpManager.ok(response)) {
			byte[] bytes = response.body();
			String path = DownloadConfig.getPath(this.hash + TorrentProtocol.TORRENT_SUFFIX);
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
		} else if(verifyMagnetHash(url)) {
			this.hash = url;
			this.magnet = MAGNET_PREFIX + this.hash;
		} else {
			throw new DownloadException("不支持的磁力链接：" + url);
		}
	}
	
	/**
	 * 设置解码器排序
	 */
	private static final void putDecoder(MagnetCoder decoder) {
		LOGGER.info("添加磁力链接解码器：{}", decoder.name());
		DECODERS.add(decoder);
	}

	/**
	 * 下载种子文件
	 */
	public static final File download(String url) throws DownloadException {
		synchronized (DECODERS) {
			File file = null;
			for (MagnetCoder decoder : DECODERS) {
				file = decoder.execute(url);
				if(file != null) {
					break;
				}
			}
			return file;
		}
	}
	
	/**
	 * 验证磁力链接
	 */
	public static final boolean verify(String url) {
		return verifyMagnet(url) && verifyMagnetHash(url);
	}

	/**
	 * 验证磁力链接
	 */
	public static final boolean verifyMagnet(String url) {
		return StringUtils.startsWith(url.toLowerCase(), MAGNET_PREFIX);
	}
	
	/**
	 * 验证磁力链接HASH
	 */
	public static final boolean verifyMagnetHash(String url) {
		return url != null && url.matches(MAGNET_HASH_REGEX);
	}
	
	/**
	 * 将磁力链接HASH转为磁力链接
	 */
	public static final String buildMagnet(String hash) {
		if(verifyMagnet(hash)) {
			return hash;
		}
		return MAGNET_PREFIX + hash.toLowerCase();
	}
	
}
