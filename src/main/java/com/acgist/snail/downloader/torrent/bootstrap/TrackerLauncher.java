package com.acgist.snail.downloader.torrent.bootstrap;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.tracker.TrackerClient;
import com.acgist.snail.pojo.message.AnnounceMessage;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.utils.UniqueCodeUtils;

/**
 * <p>TrackerClient发射器</p>
 * <p>定时循环查询Peer信息。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class TrackerLauncher implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerLauncher.class);
	
	private final TrackerClient client; // 客户端
	
	private final TaskSession taskSession;
	private final TorrentSession torrentSession;
//	private final TrackerLauncherGroup trackerLauncherGroup;
	
	private final Integer id; // id：transaction_id（获取peer时使用）
	private Integer interval; // 下次等待时间
	private Integer done; // 已完成数量
	private Integer undone; // 未完成数量
	private boolean run = false; // 是否已经运行
	private boolean available = true; // 可用
	
	private TrackerLauncher(TrackerClient client, TorrentSession torrentSession) {
		this.id = UniqueCodeUtils.buildInteger();
		this.client = client;
		this.taskSession = torrentSession.taskSession();
		this.torrentSession = torrentSession;
//		this.trackerLauncherGroup = torrentSession.trackerLauncherGroup();
	}
	
	public static final TrackerLauncher newInstance(TrackerClient client, TorrentSession torrentSession) {
		return new TrackerLauncher(client, torrentSession);
	}

	public Integer id() {
		return this.id;
	}

	@Override
	public void run() {
		run = true;
		if(available()) {
			LOGGER.debug("TrackerClient查找Peer：{}", client.announceUrl());
			client.findPeers(this.id, this.torrentSession);
		}
	}

	/**
	 * <p>解析announce信息</p>
	 * <p>添加Peer，同时设置下次查询定时任务。</p>
	 */
	public void announce(AnnounceMessage message) {
		if(message == null) {
			return;
		}
		if(!available()) {
			return;
		}
		this.interval = message.getInterval();
		this.done = message.getDone();
		this.undone = message.getUndone();
		this.torrentSession.peer(message.getPeers());
		LOGGER.debug("已完成Peer数量：{}，未完成的Peer数量：{}，下次请求时间：{}", this.done, this.undone, this.interval);
		if(this.interval != null) { // 添加重复执行
			torrentSession.timer(this.interval, TimeUnit.SECONDS, this);
		}
	}

	/**
	 * <p>释放资源</p>
	 * <p>发送Tracker stop消息，如果下载完成的发送complete信息。</p>
	 */
	public void release() {
		this.available = false;
		if(run) {
			SystemThreadContext.submit(() -> {
				this.client.stop(this.id, this.torrentSession);
				if(this.taskSession.complete()) { // 任务完成
					this.client.complete(this.id, this.torrentSession);
				}
			});
		}
	}
	
	/**
	 * <p>可用状态：TrackerClient可用并且没有释放资源。</p>
	 */
	private boolean available() {
		return client.available() && available;
	}
	
}
