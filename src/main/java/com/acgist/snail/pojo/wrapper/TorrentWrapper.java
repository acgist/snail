package com.acgist.snail.pojo.wrapper;

import com.acgist.snail.protocol.torrent.pojo.Torrent;
import com.acgist.snail.protocol.torrent.pojo.TorrentInfo;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.StringUtils;

/**
 * wrapper - 种子
 */
public class TorrentWrapper {

	private Torrent torrent;

	public TorrentWrapper(Torrent torrent) throws DownloadException {
		if(torrent == null) {
			throw new DownloadException("解析种子文件异常");
		}
		this.torrent = torrent;
	}

	/**
	 * 下载名称
	 */
	public String name() {
		TorrentInfo torrentInfo = torrent.getInfo();
		String name = torrentInfo.getNameUtf8();
		if(StringUtils.isEmpty(name)) {
			name = StringUtils.charset(torrentInfo.getName(), torrent.getEncoding());
		}
		return name;
	}
	
	public Torrent torrent() {
		return this.torrent;
	}
	
}
