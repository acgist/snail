package com.acgist.snail.net.tracker;

import com.acgist.snail.pojo.message.AnnounceMessage;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.utils.UniqueCodeUtils;

/**
 * tracker<br>
 * 定时循环查询
 */
public class TrackerLauncher implements Runnable {

	private AbstractTrackerClient client; // 客户端

	private TorrentSession torrentSession; // torrent session
	
	private final Integer id; // id：transaction_id（获取peer时使用）
	private Integer interval; // 下次等待时间
	private Integer done; // 已完成数量
	private Integer undone; // 未完成数量

	public TrackerLauncher() {
		this.id = UniqueCodeUtils.buildInteger();
	}
	
	public TorrentSession torrentSession() {
		return this.torrentSession;
	}
	
	public Integer id() {
		return this.id;
	}

	@Override
	public void run() {
	}

	/**
	 * 设置信息
	 */
	public void announce(AnnounceMessage message) {
	}
	
}
