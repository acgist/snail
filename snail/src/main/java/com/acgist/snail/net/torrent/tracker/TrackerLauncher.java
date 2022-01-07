package com.acgist.snail.net.torrent.tracker;

import java.util.Map;

import com.acgist.snail.config.PeerConfig;
import com.acgist.snail.context.PeerContext;
import com.acgist.snail.context.TrackerContext;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.pojo.message.AnnounceMessage;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.pojo.session.TrackerSession;
import com.acgist.snail.utils.MapUtils;
import com.acgist.snail.utils.NumberUtils;

/**
 * <p>Tracker执行器</p>
 * <p>使用TrackerSession查询Peer信息</p>
 * 
 * @author acgist
 */
public final class TrackerLauncher {

	private static final Logger LOGGER = LoggerFactory.getLogger(TrackerLauncher.class);
	
	/**
	 * <p>transaction_id</p>
	 * <p>对应Tracker服务器和BT任务信息</p>
	 */
	private final Integer id;
	/**
	 * <p>可用状态</p>
	 */
	private boolean available = true;
	/**
	 * <p>是否需要释放</p>
	 */
	private boolean needRelease = false;
	/**
	 * <p>Tracker信息</p>
	 */
	private final TrackerSession session;
	/**
	 * <p>BT任务信息</p>
	 */
	private final TorrentSession torrentSession;
	
	/**
	 * @param session Tracker信息
	 * @param torrentSession BT任务信息
	 */
	private TrackerLauncher(TrackerSession session, TorrentSession torrentSession) {
		this.id = NumberUtils.build();
		this.session = session;
		this.torrentSession = torrentSession;
	}
	
	/**
	 * <p>新建TrackerLauncher</p>
	 * 
	 * @param session Tracker信息
	 * @param torrentSession BT任务信息
	 * 
	 * @return {@link TrackerLauncher}
	 */
	public static final TrackerLauncher newInstance(TrackerSession session, TorrentSession torrentSession) {
		return new TrackerLauncher(session, torrentSession);
	}

	/**
	 * <p>获取ID</p>
	 * 
	 * @return ID
	 */
	public Integer id() {
		return this.id;
	}
	
	/**
	 * <p>获取声明地址</p>
	 * 
	 * @return 声明地址
	 */
	public String announceUrl() {
		return this.session.announceUrl();
	}

	/**
	 * <p>查找Peer</p>
	 */
	public void findPeer() {
		if(this.available()) {
			LOGGER.debug("TrackerLauncher查找Peer：{}", this.session);
			this.needRelease = true;
			this.session.findPeers(this.id, this.torrentSession);
		}
	}

	/**
	 * <p>收到声明响应消息</p>
	 * 
	 * @param message 声明响应消息
	 */
	public void announce(AnnounceMessage message) {
		if(message == null) {
			return;
		}
		if(this.available()) {
			this.peer(message.peers());
		} else {
			LOGGER.debug("收到声明响应消息（TrackerLauncher无效）：{}", this.session);
		}
	}
	
	/**
	 * <p>添加Peer</p>
	 * 
	 * @param peers Peer列表
	 */
	private void peer(Map<String, Integer> peers) {
		if(MapUtils.isEmpty(peers)) {
			return;
		}
		final PeerContext peerContext = PeerContext.getInstance();
		peers.forEach((host, port) -> peerContext.newPeerSession(
			this.torrentSession.infoHashHex(),
			this.torrentSession.statistics(),
			host,
			port,
			PeerConfig.Source.TRACKER
		));
	}

	/**
	 * <p>释放资源</p>
	 */
	public void release() {
		if(this.needRelease && this.available()) {
			this.needRelease = false;
			try {
				if(this.torrentSession.completed()) {
					LOGGER.debug("TrackerLauncher完成通知：{}", this.session);
					this.session.completed(this.id, this.torrentSession);
				} else {
					LOGGER.debug("TrackerLauncher暂停通知：{}", this.session);
					this.session.stopped(this.id, this.torrentSession);
				}
			} catch (Exception e) {
				LOGGER.error("TrackerLauncher关闭异常", e);
			} finally {
				TrackerContext.getInstance().removeTrackerLauncher(this.id);
			}
		}
		// 立即标记释放资源
		this.available = false;
	}
	
	/**
	 * <p>判断是否可用</p>
	 * 
	 * @return 是否可用
	 * 
	 * @see #available
	 * @see TrackerSession#available()
	 */
	private boolean available() {
		return this.available && this.session.available();
	}
	
}
