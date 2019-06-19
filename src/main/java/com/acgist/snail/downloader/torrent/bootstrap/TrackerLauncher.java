package com.acgist.snail.downloader.torrent.bootstrap;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.bt.tracker.bootstrap.TrackerClient;
import com.acgist.snail.pojo.message.AnnounceMessage;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.system.manager.PeerManager;
import com.acgist.snail.system.manager.TrackerManager;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.UniqueCodeUtils;

/**
 * <p>Tracker发射器</p>
 * <p>使用TrackerClient查询Peer信息。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class TrackerLauncher {

	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerLauncher.class);
	
	private final TrackerClient client; // 客户端
	
	private final TorrentSession torrentSession;
//	private final TrackerLauncherGroup trackerLauncherGroup;
	
	private final Integer id; // id：transaction_id：连接时使用
	private Integer interval; // 下次等待时间
	private Integer done; // 已完成数量
	private Integer undone; // 未完成数量
	private boolean needRelease = false; // 是否需要释放
	private boolean available = true; // 可用
	
	private TrackerLauncher(TrackerClient client, TorrentSession torrentSession) {
		this.id = UniqueCodeUtils.build();
		this.client = client;
		this.torrentSession = torrentSession;
//		this.trackerLauncherGroup = torrentSession.trackerLauncherGroup();
	}
	
	public static final TrackerLauncher newInstance(TrackerClient client, TorrentSession torrentSession) {
		return new TrackerLauncher(client, torrentSession);
	}

	/**
	 * 获取当前TrackerLauncher的ID
	 */
	public Integer id() {
		return this.id;
	}

	public void findPeer() {
		this.needRelease = true;
		if(available()) {
			LOGGER.debug("TrackerClient查找Peer：{}", client.announceUrl());
			this.client.findPeers(this.id, this.torrentSession);
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
		this.peer(message.getPeers());
		LOGGER.debug("已完成Peer数量：{}，未完成的Peer数量：{}，下次请求时间：{}", this.done, this.undone, this.interval);
	}
	
	/**
	 * 设置Peer
	 */
	private void peer(Map<String, Integer> peers) {
		if(CollectionUtils.isEmpty(peers)) {
			return;
		}
		final PeerManager manager = PeerManager.getInstance();
		peers.forEach((host, port) -> {
			manager.newPeerSession(
				this.torrentSession.infoHashHex(),
				this.torrentSession.statistics(),
				host,
				port,
				PeerConfig.SOURCE_TRACKER);
		});
	}

	/**
	 * <p>释放资源</p>
	 * <p>发送Tracker stop消息，如果下载完成的发送complete信息。</p>
	 */
	public void release() {
		this.available = false;
		if(this.needRelease && available()) {
			SystemThreadContext.submit(() -> {
				if(this.torrentSession.complete()) { // 任务完成
					this.client.complete(this.id, this.torrentSession);
				} else { // 任务暂停
					this.client.stop(this.id, this.torrentSession);
				}
				TrackerManager.getInstance().release(this.id);
			});
		}
	}
	
	/**
	 * <p>可用状态：TrackerClient可用并且没有释放资源。</p>
	 */
	private boolean available() {
		return this.client.available() && this.available;
	}
	
}
