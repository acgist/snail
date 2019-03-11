package com.acgist.snail.pojo.wrapper;

import com.acgist.snail.coder.torrent.TorrentFiles;
import com.acgist.snail.coder.torrent.TorrentInfo;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.StringUtils;

/**
 * wrapper - 种子
 */
public class TorrentWrapper {

	private TorrentInfo torrentInfo;

	public TorrentWrapper(TorrentInfo torrentInfo) throws DownloadException {
		if(torrentInfo == null) {
			throw new DownloadException("解析种子文件异常");
		}
		this.torrentInfo = torrentInfo;
	}

	/**
	 * 下载名称
	 */
	public String name() {
		TorrentFiles files = torrentInfo.getInfo();
		String name = files.getNameUtf8();
		if(StringUtils.isEmpty(name)) {
			name = StringUtils.charset(files.getName(), torrentInfo.getEncoding());
		}
		return name;
	}
	
	public TorrentInfo torrentInfo() {
		return this.torrentInfo;
	}
	
}
