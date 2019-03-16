package com.acgist.snail.pojo.session;

import java.util.List;

import com.acgist.snail.pojo.bean.Peer;
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
	 * announce事件
	 */
	public enum Event {
		
		none(0), // none
		completed(1), // 完成
		started(2), // 开始
		stopped(3); // 停止
		
		private int event;

		private Event(int event) {
			this.event = event;
		}

		public int event() {
			return this.event;
		}
		
	}
	
	/**
	 * 动作
	 */
	public enum Action {
		
		connect(0), // 连接
		announce(1), // 获取信息
		scrape(2), // 刷新信息
		error(3); // 错误
		
		private int action;

		private Action(int action) {
			this.action = action;
		}
		
		public int action() {
			return this.action;
		}
		
	}
	
	/**
	 * id：transaction_id
	 */
	private Long id;
	/**
	 * 种子信息
	 */
	private InfoHash infoHash;
	/**
	 * 种子
	 */
	private Torrent torrent;
	/**
	 * Peers
	 */
	private List<Peer> peers;

	private TorrentSession(Torrent torrent, InfoHash infoHash) throws DownloadException {
		if(torrent == null || infoHash == null) {
			throw new DownloadException("解析种子文件异常");
		}
		this.id = UniqueCodeUtils.buildLong();
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

	public Long id() {
		return id;
	}
	
	public Torrent torrent() {
		return this.torrent;
	}
	
	public InfoHash infoHash() {
		return this.infoHash;
	}
	
}
