package com.acgist.snail.protocol.magnet;

import java.io.File;
import java.net.URI;
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
import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.acgist.snail.system.config.DownloadConfig;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.FileUtils;

/**
 * 磁力链接解析器：磁力链接转种子文件
 */
public abstract class MagnetCoder {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MagnetCoder.class);
	
	private static final  List<MagnetCoder> DECODERS = new ArrayList<>();
	
	private static final String HASH_KEY = "xt";
	private static final String HASH_PREFIX = "urn:btih:";
	
	public static final String MAGNET_PREFIX = "magnet:?xt=urn:btih:"; // 磁力链接前缀
	
	public static final String MAGNET_REGEX = "magnet:\\?xt=urn:btih:.+"; // 磁力链接正则表达式
	public static final String MAGNET_HASH_32_REGEX = "[a-zA-Z0-9]{32}"; // 32位磁力链接HASH正则表达式
	public static final String MAGNET_HASH_40_REGEX = "[a-zA-Z0-9]{40}"; // 40位磁力链接HASH正则表达式
	
	protected String hash; // HASH
	protected String magnet; // 完整磁力链接
	
	static {
		MagnetCoder.register(BtbttvMagnetCoder.newInstance());
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
			this.hash = buildHash(url);
		} else if(verifyMagnetHash32(url)) { // 32位转为正常40位
			InfoHash infoHash = InfoHash.newInstance(url);
			this.hash = infoHash.hash();
		} else if(verifyMagnetHash40(url)) {
			this.hash = url;
		}
		if(this.hash == null) {
			throw new DownloadException("不支持的磁力链接：" + url);
		}
		this.magnet = buildMagnet(this.hash);
	}
	
	/**
	 * 解析磁力链接获取hash
	 */
	private String buildHash(String url) throws DownloadException {
		URI uri = URI.create(url);
		String[] datas = uri.getSchemeSpecificPart().substring(1).split("&");
		int index;
		String key, value;
		for (String data : datas) {
			index = data.indexOf("=");
			if(index >= 0) {
				key = data.substring(0, index);
				if(HASH_KEY.equals(key)) {
					value = data.substring(index + 1);
					String hash = value.substring(HASH_PREFIX.length());
					if(verifyMagnetHash32(hash)) {
						InfoHash infoHash = InfoHash.newInstance(hash);
						hash = infoHash.hash();
					}
					return hash;
				}
			}
		}
		return null;
	}
	
	/**
	 * 设置解码器排序
	 */
	private static final void register(MagnetCoder decoder) {
		LOGGER.info("注册磁力链接解码器：{}", decoder.name());
		DECODERS.add(decoder);
	}

	/**
	 * 下载种子文件
	 */
	public static final File download(String url) throws DownloadException {
		synchronized (DECODERS) {
			File file = null;
			for (MagnetCoder coder : DECODERS) {
				file = coder.execute(url);
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
		return
			verifyMagnet(url) ||
			verifyMagnetHash32(url) ||
			verifyMagnetHash40(url);
	}

	/**
	 * 验证磁力链接
	 */
	public static final boolean verifyMagnet(String url) {
		return url != null && url.matches(MAGNET_REGEX);
	}
	
	/**
	 * 验证32位磁力链接HASH
	 */
	public static final boolean verifyMagnetHash32(String url) {
		return url != null && url.matches(MAGNET_HASH_32_REGEX);
	}
	
	/**
	 * 验证40位磁力链接HASH
	 */
	public static final boolean verifyMagnetHash40(String url) {
		return url != null && url.matches(MAGNET_HASH_40_REGEX);
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
