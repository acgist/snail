package com.acgist.snail.net.torrent.bootstrap;

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
import com.acgist.snail.utils.ThreadUtils;

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
	 * <p>注：不要超过TorrentSession线程池大小（如果是固定线程池）</p>
	 */
	private static final int BUILD_SIZE = 3;
	/**
	 * <p>单次创建PeerDownloader最大数量（包含失败）</p>
	 */
	private static final int MAX_BUILD_SIZE = 64;
	/**
	 * <p>自旋时间</p>
	 */
	private static final int SPIN_LOCK_TIME = 1000;
	
	/**
	 * <p>是否继续创建PeerDownloader</p>
	 */
	private final AtomicBoolean build = new AtomicBoolean(false);
	/**
	 * <p>创建PeerDownloader信号量</p>
	 */
	private final Semaphore buildSemaphore = new Semaphore(BUILD_SIZE);
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
	 * <p>剔除劣质PeerDownloader、创建PeerDownloader</p>
	 */
	public void optimize() {
		LOGGER.debug("优化PeerDownloader");
		this.spinLock();
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
	 * <p>资源释放</p>
	 * <p>释放所有PeerDownloader</p>
	 */
	public void release() {
		LOGGER.debug("释放PeerDownloaderGroup");
		// 解除PeerDownloader锁：防止暂停任务同时执行优化任务导致卡死
		this.release(false);
		synchronized (this.peerDownloaders) {
			this.peerDownloaders.forEach(launcher -> {
				SystemThreadContext.submit(() -> {
					launcher.release();
				});
				PeerManager.getInstance().preference(this.torrentSession.infoHashHex(), launcher.peerSession());
			});
			this.peerDownloaders.clear();
		}
	}
	
	/**
	 * <p>自旋</p>
	 * <p>下载器检查是否找到Peer，如果没有找到进行自旋等待。</p>
	 */
	private void spinLock() {
		final PeerManager peerManager = PeerManager.getInstance();
		final String infoHashHex = this.torrentSession.infoHashHex();
		while(this.torrentSession.downloading()) {
			if(peerManager.havePeerSession(infoHashHex)) {
				break;
			}
			ThreadUtils.sleep(SPIN_LOCK_TIME);
		}
	}
	
	/**
	 * <p>创建PeerDownloader列表</p>
	 * <p>创建数量达到最大Peer连接数量或者创建次数超过{@link #MAX_BUILD_SIZE}时退出创建</p>
	 */
	private void buildPeerDownloaders() {
		LOGGER.debug("创建PeerDownloader");
		int size = 0;
		this.build.set(true); // 重置创建状态
		this.buildSemaphore.drainPermits(); // 重置信号量
		this.buildSemaphore.release(BUILD_SIZE);
		while(this.build.get()) {
			this.acquire(); // 获取信号量
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
					this.release(ok); // 释放信号量
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
	 * 	<dd>处于下载中的Peer数量大于等于配置的最大数量</dd>
	 * 	<dd>不能查找到更多的Peer</dd>
	 * </dl>
	 * 
	 * @return true-继续；false-停止；
	 */
	private boolean buildPeerDownloader() {
		if(!this.torrentSession.downloading()) {
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
			LOGGER.debug("挑选Peer失败");
			return false;
		}
	}
	
	/**
	 * <p>剔除劣质Peer</p>
	 * <p>劣质Peer：评分最低的Peer</p>
	 * <p>直接剔除：不可用的Peer（评分=0、状态不可用）</p>
	 * <p>劣质Peer：释放劣质Peer，然后放入Peer队列头部（如果最后Peer列表小于系统最大数量不剔除劣质Peer）</p>
	 * <p>必须循环完所有的PeerDownloader，从而清除评分进行新一轮的评分，防止评分被累计计算。</p>
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
	 * <p>PeerDownloader加入队列</p>
	 */
	private void offer(PeerDownloader peerDownloader) {
		final var ok = this.peerDownloaders.offer(peerDownloader);
		if(!ok) {
			LOGGER.warn("PeerDownloader丢失：{}", peerDownloader);
		}
	}
	
	/**
	 * <p>剔除劣质Peer</p>
	 * <p>释放Peer资源、放入Peer队列头部</p>
	 */
	private void inferiorPeerDownloader(PeerDownloader peerDownloader) {
		if(peerDownloader != null) {
			final PeerSession peerSession = peerDownloader.peerSession();
			LOGGER.debug("剔除劣质PeerDownloader：{}-{}", peerSession.host(), peerSession.port());
			SystemThreadContext.submit(() -> {
				peerDownloader.release();
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
	 * <p>释放信号量：设置创建状态</p>
	 */
	private void release(boolean build) {
		this.build.set(build);
		this.buildSemaphore.release();
	}

}
