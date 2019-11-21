package com.acgist.snail.net.torrent.bootstrap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.torrent.peer.bootstrap.PeerManager;
import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.system.config.PeerConfig;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.context.SystemThreadContext;

/**
 * <p>PeerDownloader组</p>
 * <dl>
 * 	<dt>管理PeerDownloader</dt>
 * 	<dd>创建PeerDownloader</dd>
 * 	<dd>剔除劣质PeerDownloader</dd>
 * </dl>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class PeerDownloaderGroup {

	private static final Logger LOGGER = LoggerFactory.getLogger(PeerDownloaderGroup.class);
	
	/**
	 * <p>同时创建PeerDownloader数量</p>
	 * <p>注：如果TorrentSession线程池固定大小，不要超过线程池大小。</p>
	 */
	private static final int BUILD_SIZE = 2;
	/**
	 * 单次创建PeerDownloader最大数量（包含失败）
	 */
	private static final int MAX_BUILD_SIZE = 64;
	
	/**
	 * 是否继续创建PeerDownloader
	 */
	private final AtomicBoolean build = new AtomicBoolean(false);
	/**
	 * 创建PeerDownloader信号量
	 */
	private final Semaphore buildSemaphore = new Semaphore(BUILD_SIZE);
	/**
	 * <p>优质Peer</p>
	 * <p>每次优化时挑选的优质Peer（可以下载），在发送pex消息时发送给连接的Peer，发送完成后清空。</p>
	 */
	private final List<PeerSession> optimize = new ArrayList<>();
	/**
	 * <p>PeerDownloader队列</p>
	 */
	private final BlockingQueue<PeerDownloader> peerDownloaders = new LinkedBlockingQueue<>();
	
	private final TorrentSession torrentSession;
	
	private PeerDownloaderGroup(TorrentSession torrentSession) {
		this.torrentSession = torrentSession;
	}
	
	public static final PeerDownloaderGroup newInstance(TorrentSession torrentSession) {
		return new PeerDownloaderGroup(torrentSession);
	}

	/**
	 * <p>优化PeerDownloader</p>
	 */
	public void optimize() {
		LOGGER.debug("优化PeerDownloader");
		synchronized (this.peerDownloaders) {
			try {
				inferiorPeerDownloaders();
				buildPeerDownloaders();
			} catch (Exception e) {
				LOGGER.error("优化PeerDownloader异常", e);
			}
		}
	}
	
	/**
	 * <p>获取优质Peer</p>
	 * <p>返回后清除优质Peer列表</p>
	 * 
	 * @return 优质Peer
	 */
	public List<PeerSession> optimizePeerSession() {
		final var list = new ArrayList<>(this.optimize);
		this.optimize.clear();
		return list;
	}

	/**
	 * <p>资源释放</p>
	 * <p>释放所有PeerDownloader</p>
	 */
	public void release() {
		LOGGER.debug("释放PeerDownloaderGroup");
		// 解除PeerDownloader锁：防止暂停任务卡死
		this.release(false);
		synchronized (this.peerDownloaders) {
			this.peerDownloaders.forEach(launcher -> {
				SystemThreadContext.submit(() -> {
					launcher.releaseDownload();
				});
				PeerManager.getInstance().preference(this.torrentSession.infoHashHex(), launcher.peerSession());
			});
			this.peerDownloaders.clear();
		}
	}
	
	/**
	 * <p>创建PeerDownloader列表</p>
	 * <p>创建数量达到最大Peer连接数量或者重试次数超过{@link #MAX_BUILD_SIZE}时退出创建</p>
	 */
	private void buildPeerDownloaders() {
		LOGGER.debug("创建PeerDownloader");
		int size = 0;
		this.build.set(true); // 重置创建状态
		this.buildSemaphore.drainPermits(); // 重置信号量
		this.buildSemaphore.release(BUILD_SIZE);
		while(this.build.get()) {
			this.acquire();
			if(!this.build.get()) { // 再次判断状态
				break;
			}
			this.torrentSession.submit(() -> {
				boolean ok = true; // 是否继续创建
				try {
					ok = buildPeerDownloader();
				} catch (Exception e) {
					LOGGER.error("创建PeerDownloader异常", e);
				} finally {
					this.release(ok);
				}
			});
			if(++size >= MAX_BUILD_SIZE) {
				LOGGER.debug("超过PeerDownloader单次最大创建数量：退出循环");
				break;
			}
		}
	}
	
	/**
	 * <p>创建PeerDownloader</p>
	 * <p>从Peer队列尾部拿出一个Peer创建下载，失败后插入Peer队列头部。</p>
	 * <dl>
	 * 	<dt>跳出创建循环</dt>
	 * 	<dd>任务不处于下载状态</dd>
	 * 	<dd>已经处于下载的PeerDownloader大于等于配置的最大PeerDownloader数量</dd>
	 * 	<dd>不能查找到更多的Peer</dd>
	 * </dl>
	 * 
	 * @return true-继续；false-停止；
	 */
	private boolean buildPeerDownloader() {
		if(!this.torrentSession.running()) {
			return false;
		}
		if(this.peerDownloaders.size() >= SystemConfig.getPeerSize()) {
			return false;
		}
		final PeerSession peerSession = PeerManager.getInstance().pick(this.torrentSession.infoHashHex());
		if(peerSession != null) {
			final PeerDownloader peerDownloader = PeerDownloader.newInstance(peerSession, this.torrentSession);
			final boolean ok = peerDownloader.handshake();
			if(ok) {
				peerSession.status(PeerConfig.STATUS_DOWNLOAD);
				this.offer(peerDownloader);
			} else {
				// 失败后需要放回队列
				PeerManager.getInstance().inferior(this.torrentSession.infoHashHex(), peerSession);
			}
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * <p>剔除劣质Peer</p>
	 * <p>劣质Peer：评分最低的Peer</p>
	 * <p>直接剔除：不可用的Peer（评分=0、状态不可用）</p>
	 * <p>释放劣质Peer，然后将其放入Peer队列头部。如果最后Peer列表小于系统最大数量不剔除劣质Peer。</p>
	 * <p>必须循环完所有的PeerDownloader，清除评分进行新一轮的评分计算。</p>
	 */
	private void inferiorPeerDownloaders() {
		LOGGER.debug("剔除劣质PeerDownloader");
		int index = 0;
		PeerDownloader tmp = null;
		PeerDownloader inferior = null; // 劣质PeerDownloader
		long downloadMark = 0, minMark = 0;
		final int size = this.peerDownloaders.size();
		while(true) {
			if(index++ >= size) {
				break;
			}
			tmp = this.peerDownloaders.poll();
			if(tmp == null) {
				break;
			}
			// 状态不可用直接剔除
			if(!tmp.available()) {
				inferiorPeerDownloader(tmp);
				continue;
			}
			// 获取评分
			downloadMark = tmp.downloadMark();
			// 首次评分忽略
			if(!tmp.marked()) {
				this.offer(tmp);
				continue;
			}
			if(downloadMark <= 0) {
				inferiorPeerDownloader(tmp);
				continue;
			} else {
				this.optimize.add(tmp.peerSession());
			}
			if(inferior == null) {
				inferior = tmp;
				minMark = downloadMark;
			} else if(downloadMark < minMark) {
				this.offer(inferior);
				inferior = tmp;
				minMark = downloadMark;
			} else {
				this.offer(tmp);
			}
		}
		if(inferior != null) {
			// 如果当前Peer连接数量小于系统配置最大数量不剔除
			if(this.peerDownloaders.size() < SystemConfig.getPeerSize()) {
				this.offer(inferior);
			} else {
				inferiorPeerDownloader(inferior);
			}
		}
	}
	
	/**
	 * PeerDownloader加入队列
	 */
	private void offer(PeerDownloader peerDownloader) {
		final var ok = this.peerDownloaders.offer(peerDownloader);
		if(!ok) {
			LOGGER.warn("PeerDownloader丢失：{}", peerDownloader);
		}
	}
	
	/**
	 * <p>剔除劣质Peer</p>
	 * <p>释放Peer资源，将其放入Peer队列头部。</p>
	 */
	private void inferiorPeerDownloader(PeerDownloader peerDownloader) {
		if(peerDownloader != null) {
			final PeerSession peerSession = peerDownloader.peerSession();
			LOGGER.debug("剔除劣质PeerDownloader：{}-{}", peerSession.host(), peerSession.port());
			SystemThreadContext.submit(() -> {
				peerDownloader.releaseDownload();
			});
			PeerManager.getInstance().inferior(this.torrentSession.infoHashHex(), peerSession);
		}
	}
	
	/**
	 * <p>获取信号量</p>
	 */
	private void acquire() {
		try {
			this.buildSemaphore.acquire();
		} catch (InterruptedException e) {
			LOGGER.debug("信号量获取异常", e);
			Thread.currentThread().interrupt();
		}
	}
	
	/**
	 * 释放信号量：设置创建状态
	 */
	private void release(boolean build) {
		this.build.set(build);
		this.buildSemaphore.release();
	}

}
