package com.acgist.snail.net.torrent.peer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import com.acgist.snail.config.PeerConfig;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.ITaskSession;
import com.acgist.snail.context.SystemThreadContext;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.torrent.TorrentSession;
import com.acgist.snail.utils.ThreadUtils;

/**
 * <p>PeerDownloader组</p>
 * <p>主要功能：新建PeerDownloader、剔除劣质PeerDownloader</p>
 * 
 * @author acgist
 */
public final class PeerDownloaderGroup {

	private static final Logger LOGGER = LoggerFactory.getLogger(PeerDownloaderGroup.class);
	
	/**
	 * <p>同时新建PeerDownloader数量：{@value}</p>
	 * <p>如果TorrentSession是固定线程池：不要超过线程池的大小</p>
	 */
	private static final int BUILD_SIZE = 3;
	/**
	 * <p>单次新建PeerDownloader最大数量：{@value}</p>
	 */
	private static final int MAX_BUILD_SIZE = 64;
	
	/**
	 * <p>是否继续新建</p>
	 */
	private final AtomicBoolean build;
	/**
	 * <p>新建信号量</p>
	 */
	private final Semaphore buildSemaphore;
	/**
	 * <p>任务信息</p>
	 */
	private final ITaskSession taskSession;
	/**
	 * <p>BT任务信息</p>
	 */
	private final TorrentSession torrentSession;
	/**
	 * <p>PeerDownloader队列</p>
	 */
	private final BlockingQueue<PeerDownloader> peerDownloaders;
	
	/**
	 * @param torrentSession BT任务信息
	 */
	private PeerDownloaderGroup(TorrentSession torrentSession) {
		this.build = new AtomicBoolean(false);
		this.buildSemaphore = new Semaphore(BUILD_SIZE);
		this.taskSession = torrentSession.taskSession();
		this.torrentSession = torrentSession;
		this.peerDownloaders = new LinkedBlockingQueue<>();
	}
	
	/**
	 * <p>新建PeerDownloader组</p>
	 * 
	 * @param torrentSession BT任务信息
	 * 
	 * @return {@link PeerDownloaderGroup}
	 */
	public static final PeerDownloaderGroup newInstance(TorrentSession torrentSession) {
		return new PeerDownloaderGroup(torrentSession);
	}

	/**
	 * <p>优化PeerDownloader</p>
	 */
	public void optimize() {
		LOGGER.debug("优化PeerDownloader：{}", this.torrentSession);
		this.spinLock();
		synchronized (this.peerDownloaders) {
			try {
				this.inferiorPeerDownloaders();
				this.buildPeerDownloaders();
			} catch (Exception e) {
				LOGGER.error("优化PeerDownloader异常", e);
			}
		}
	}
	
	/**
	 * <p>资源释放</p>
	 */
	public void release() {
		LOGGER.debug("释放PeerDownloaderGroup：{}", this.torrentSession);
		// 释放信号量：防止暂停任务时正在执行优化任务导致获取不到锁导致卡死
		this.release(false);
		synchronized (this.peerDownloaders) {
			this.peerDownloaders.forEach(downloader -> {
				SystemThreadContext.submit(downloader::release);
				// 下载列表中的Peer属于优质Peer
				PeerContext.getInstance().preference(this.torrentSession.infoHashHex(), downloader.peerSession());
			});
			this.peerDownloaders.clear();
		}
	}
	
	/**
	 * <p>自旋等待</p>
	 * <p>检测是否找到Peer：如果没有找到进行自旋等待</p>
	 */
	private void spinLock() {
		final PeerContext peerContext = PeerContext.getInstance();
		final String infoHashHex = this.torrentSession.infoHashHex();
		while(this.taskSession.statusDownload()) {
			if(peerContext.isNotEmpty(infoHashHex)) {
				break;
			}
			ThreadUtils.sleep(SystemConfig.ONE_SECOND_MILLIS);
		}
	}
	
	/**
	 * <p>新建PeerDownloader列表</p>
	 */
	private void buildPeerDownloaders() {
		LOGGER.debug("新建PeerDownloader：{}", this.torrentSession);
		int size = 0;
		// 重置新建状态
		this.build.set(true);
		// 重置信号量
		this.buildSemaphore.drainPermits();
		this.buildSemaphore.release(BUILD_SIZE);
		while(this.build.get()) {
			this.acquire();
			if(!this.build.get()) {
				LOGGER.debug("不能继续新建PeerDownloader：退出循环");
				break;
			}
			this.torrentSession.submit(() -> {
				boolean success = true;
				try {
					success = this.buildPeerDownloader();
				} catch (Exception e) {
					LOGGER.error("新建PeerDownloader异常", e);
				} finally {
					this.release(success);
				}
			});
			if(++size >= MAX_BUILD_SIZE) {
				LOGGER.debug("不能继续新建PeerDownloader：超过单次新建最大数量");
				break;
			}
		}
	}
	
	/**
	 * <p>新建PeerDownloader</p>
	 * 
	 * @return 是否继续新建
	 */
	private boolean buildPeerDownloader() {
		if(!this.taskSession.statusDownload()) {
			// 任务没有下载
			return false;
		}
		if(this.peerDownloaders.size() >= SystemConfig.getPeerSize()) {
			// 下载队列数量超过最大下载数量
			return false;
		}
		final PeerSession peerSession = PeerContext.getInstance().pick(this.torrentSession.infoHashHex());
		if(peerSession != null) {
			final PeerDownloader peerDownloader = PeerDownloader.newInstance(peerSession, this.torrentSession);
			final boolean success = peerDownloader.handshake();
			if(success) {
				peerSession.status(PeerConfig.STATUS_DOWNLOAD);
				this.offer(peerDownloader);
			} else {
				// 握手失败：放回队列
				PeerContext.getInstance().inferior(this.torrentSession.infoHashHex(), peerSession);
			}
			return true;
		} else {
			// 不能查找更多Peer
			return false;
		}
	}
	
	/**
	 * <p>剔除劣质Peer</p>
	 */
	private void inferiorPeerDownloaders() {
		LOGGER.debug("剔除劣质PeerDownloader：{}", this.torrentSession);
		int index = 0;
		// 当前下载评分
		long downloadMark = 0;
		// 最小下载评分
		long minDownloadMark = 0;
		// 当前PeerDownloader
		PeerDownloader downloader = null;
		// 劣质PeerDownloader：评分最小
		PeerDownloader minDownloader = null;
		final int size = this.peerDownloaders.size();
		while(index++ < size) {
			downloader = this.peerDownloaders.poll();
			if(downloader == null) {
				break;
			}
			// 状态无效：直接剔除
			if(!downloader.available()) {
				LOGGER.debug("剔除劣质PeerDownloader（状态无效）");
				this.inferior(downloader);
				continue;
			}
			// 必须获取评分：全部重置
			downloadMark = downloader.downloadMark();
			if(downloadMark <= 0L) {
				// 没有评分：长时间没有请求的下载
				LOGGER.debug("剔除劣质PeerDownloader（没有评分）");
				this.inferior(downloader);
				continue;
			}
			if(minDownloader == null) {
				minDownloader = downloader;
				minDownloadMark = downloadMark;
			} else if(downloadMark < minDownloadMark) {
				this.offer(minDownloader);
				minDownloader = downloader;
				minDownloadMark = downloadMark;
			} else {
				this.offer(downloader);
			}
		}
		if(minDownloader != null) {
			if(this.peerDownloaders.size() < SystemConfig.getPeerSize()) {
				// 当前连接数量小于系统配置最大数量：不用剔除
				this.offer(minDownloader);
			} else {
				LOGGER.debug("剔除劣质PeerDownloader（最低评分）");
				this.inferior(minDownloader);
			}
		}
	}
	
	/**
	 * <p>PeerDownloader加入队列</p>
	 * 
	 * @param peerDownloader PeerDownloader
	 */
	private void offer(PeerDownloader peerDownloader) {
		if(!this.peerDownloaders.offer(peerDownloader)) {
			LOGGER.warn("PeerDownloader丢失：{}", peerDownloader);
		}
	}
	
	/**
	 * <p>剔除劣质PeerDownloader</p>
	 * 
	 * @param peerDownloader 劣质PeerDownloader
	 */
	private void inferior(PeerDownloader peerDownloader) {
		if(peerDownloader != null) {
			LOGGER.debug("剔除劣质PeerDownloader：{}", peerDownloader);
			SystemThreadContext.submit(peerDownloader::release);
			PeerContext.getInstance().inferior(this.torrentSession.infoHashHex(), peerDownloader.peerSession());
		}
	}
	
	/**
	 * <p>获取信号量</p>
	 */
	private void acquire() {
		try {
			this.buildSemaphore.acquire();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			LOGGER.debug("信号量获取异常", e);
		}
	}
	
	/**
	 * <p>释放信号量：设置新建状态</p>
	 * 
	 * @param build 是否继续新建
	 */
	private void release(boolean build) {
		this.build.set(build);
		this.buildSemaphore.release();
	}

}
