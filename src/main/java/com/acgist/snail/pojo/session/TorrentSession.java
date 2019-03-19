package com.acgist.snail.pojo.session;

import java.util.List;

import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.acgist.snail.protocol.torrent.bean.Torrent;
import com.acgist.snail.protocol.torrent.bean.TorrentInfo;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.UniqueCodeUtils;

/**
 * 种子session
 */
public class TorrentSession {
	
	/**
	 * id：transaction_id（获取peer时使用）
	 */
	private Integer id;
	/**
	 * 种子
	 */
	private Torrent torrent;
	/**
	 * 种子信息
	 */
	private InfoHash infoHash;
	/**
	 * Peers
	 */
	private List<PeerSession> peers;

	private TorrentSession(Torrent torrent, InfoHash infoHash) throws DownloadException {
		if(torrent == null || infoHash == null) {
			throw new DownloadException("解析种子文件异常");
		}
		this.id = UniqueCodeUtils.buildInteger();
		this.torrent = torrent;
		this.infoHash = infoHash;
	}

	public static final TorrentSession newInstance(Torrent torrent, InfoHash infoHash) throws DownloadException {
		return new TorrentSession(torrent, infoHash);
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

	public Integer id() {
		return id;
	}
	
	public Torrent torrent() {
		return this.torrent;
	}
	
	public InfoHash infoHash() {
		return this.infoHash;
	}
	
}
