package com.acgist.snail.protocol.magnet;

import java.io.File;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import com.acgist.snail.net.http.HTTPClient;
import com.acgist.snail.protocol.torrent.TorrentProtocol;
import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.acgist.snail.system.config.DownloadConfig;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.FileUtils;

/**
 * 磁力链接解析器：磁力链接转种子文件
 */
public abstract class MagnetResolver {
	
	protected String hash; // HASH
	protected String magnet; // 完整磁力链接
	
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
		HttpClient client = HTTPClient.newClient();
		HttpRequest request = request();
		HttpResponse<byte[]> response = HTTPClient.request(client, request, BodyHandlers.ofByteArray());
		if(HTTPClient.ok(response)) {
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
		if(MagnetProtocol.verifyMagnet(url)) {
			this.hash = MagnetProtocol.buildHash(url);
		} else if(MagnetProtocol.verifyMagnetHash32(url)) { // 32位转为正常40位
			InfoHash infoHash = InfoHash.newInstance(url);
			this.hash = infoHash.infoHashHex();
		} else if(MagnetProtocol.verifyMagnetHash40(url)) {
			this.hash = url;
		}
		if(this.hash == null) {
			throw new DownloadException("不支持的磁力链接：" + url);
		}
		this.magnet = MagnetProtocol.buildMagnet(this.hash);
	}
	
}
