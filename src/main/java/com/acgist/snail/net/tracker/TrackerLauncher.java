package com.acgist.snail.net.tracker;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.message.AnnounceMessage;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.utils.UniqueCodeUtils;

/**
 * tracker<br>
 * 定时循环查询
 */
public class TrackerLauncher implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerLauncher.class);
	
	private AbstractTrackerClient client; // 客户端

	private TorrentSession session; // torrent session
	
	private final Integer id; // id：transaction_id（获取peer时使用）
	private Integer interval; // 下次等待时间
	private Integer done; // 已完成数量
	private Integer undone; // 未完成数量

	public TrackerLauncher(AbstractTrackerClient client, TorrentSession session) {
		this.id = UniqueCodeUtils.buildInteger();
		this.client = client;
		this.session = session;
	}
	
	public TorrentSession torrentSession() {
		return this.session;
	}
	
	public Integer id() {
		return this.id;
	}

	@Override
	public void run() {
		if(session.taskSession().download()) {
			client.findPeers(this.id, this.session);;
		}
	}

	/**
	 * announce信息<br>根据返回信息定时查询
	 */
	public void announce(AnnounceMessage message) {
		if(message == null) {
			return;
		}
		this.interval = message.getInterval();
		this.done = message.getDone();
		this.undone = message.getUndone();
		this.session.peer(message.getPeers());
		LOGGER.debug("已完成Peer数量：{}，未完成的Peer数量：{}，下次请求时间：{}", this.done, this.undone, this.interval);
		if(this.interval != null) { // 添加重复执行
			var taskSession = session.taskSession();
			if(client.available() && taskSession.download()) { // 客户端可用并且任务在下载中
				SystemThreadContext.timer(this.interval, TimeUnit.SECONDS, this);
			}
		}
	}
	
}
