package com.acgist.snail.protocol.magnet;

import java.io.File;
import java.net.URI;

import com.acgist.snail.pojo.entity.TaskEntity.Type;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.protocol.torrent.TorrentProtocol;
import com.acgist.snail.protocol.torrent.TorrentProtocol.TorrentFileOperation;
import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.manager.MagnetResolverManager;
import com.acgist.snail.system.manager.ProtocolManager;
import com.acgist.snail.utils.StringUtils;

/**
 * 磁力链接协议
 */
public class MagnetProtocol extends Protocol {

	private static final String HASH_KEY = "xt";
	private static final String HASH_PREFIX = "urn:btih:";
	
	public static final String MAGNET_PREFIX = "magnet:?xt=urn:btih:"; // 磁力链接前缀
	
	public static final String MAGNET_REGEX = "magnet:\\?xt=urn:btih:.+"; // 磁力链接正则表达式
	public static final String MAGNET_HASH_32_REGEX = "[a-zA-Z0-9]{32}"; // 32位磁力链接HASH正则表达式
	public static final String MAGNET_HASH_40_REGEX = "[a-zA-Z0-9]{40}"; // 40位磁力链接HASH正则表达式
	
	private static final MagnetProtocol INSTANCE = new MagnetProtocol();
	
	private MagnetProtocol() {
		super(Type.magnet, MAGNET_REGEX, MAGNET_HASH_32_REGEX, MAGNET_HASH_40_REGEX);
	}
	
	public static final MagnetProtocol getInstance() {
		return INSTANCE;
	}
	
	@Override
	public String name() {
		return "磁力链接";
	}
	
	@Override
	public boolean available() {
		return true;
	}
	
	@Override
	protected Protocol convert() throws DownloadException {
		File file = MagnetResolverManager.getInstance().download(url);
		if(file == null) {
			throw new DownloadException("下载种子失败：" + url);
		}
		var protocol = ProtocolManager.getInstance().protocol(file.getPath());
		if(protocol instanceof TorrentProtocol) { // 设置种子文件操作类型：移动
			TorrentProtocol torrentProtocol = (TorrentProtocol) protocol;
			torrentProtocol.operation(TorrentFileOperation.move);
		}
		return protocol;
	}
	
	@Override
	protected boolean buildTaskEntity() throws DownloadException {
		return false;
	}
	
	@Override
	protected void cleanMessage() {
	}
	
	/**
	 * 解析磁力链接获取hash
	 */
	public static final String buildHash(String url) throws DownloadException {
		final URI uri = URI.create(url);
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
						hash = infoHash.hashHex();
					}
					return hash;
				}
			}
		}
		return null;
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
		return StringUtils.regex(url, MAGNET_REGEX, true);
	}
	
	/**
	 * 验证32位磁力链接HASH
	 */
	public static final boolean verifyMagnetHash32(String url) {
		return StringUtils.regex(url, MAGNET_HASH_32_REGEX, true);
	}
	
	/**
	 * 验证40位磁力链接HASH
	 */
	public static final boolean verifyMagnetHash40(String url) {
		return StringUtils.regex(url, MAGNET_HASH_40_REGEX, true);
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
