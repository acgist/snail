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
 * <p>PeerLauncher组：下载</p>
 * <dl>
 * 	<dt>对连接请求下载的PeerLauncher管理优化</dt>
 * 	<dd>创建PeerLauncher。</dd>
 * 	<dd>定时替换下载最慢的PeerLauncher。</dd>
 * </dl>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class PeerLauncherGroup {

	private static final Logger LOGGER = LoggerFactory.getLogger(PeerLauncherGroup.class);
	
	/**
	 * 同时创建PeerLauncher数量，不要超过TorrentSession里面线程池大小。
	 */
	private static final int BUILD_SIZE = 2;
	/**
	 * 单次创建PeerLauncher最大数量（包含失败）
	 */
	private static final int MAX_BUILD_SIZE = 60;
	
	/**
	 * 是否继续创建PeerLauncher
	 */
	private final AtomicBoolean build = new AtomicBoolean(false);
	/**
	 * 同时创建PeerLauncher信号量
	 */
	private final Semaphore buildSemaphore = new Semaphore(BUILD_SIZE);
	/**
	 * <p>优选的Peer</p>
	 * <p>每次优化时挑选出来可以进行下载的Peer，在发送pex消息时发送给接入和连接的Peer，发送完成后清空。</p>
	 */
	private final List<PeerSession> optimize = new ArrayList<>();
	/**
	 * PeerLauncher队列
	 */
	private final BlockingQueue<PeerLauncher> peerLaunchers;
	
	private final TorrentSession torrentSession;
	
	private PeerLauncherGroup(TorrentSession torrentSession) {
		this.torrentSession = torrentSession;
		this.peerLaunchers = new LinkedBlockingQueue<>();
	}
	
	public static final PeerLauncherGroup newInstance(TorrentSession torrentSession) {
		return new PeerLauncherGroup(torrentSession);
	}

	/**
	 * <p>优化PeerLauncher</p>
	 * <p>
	 * 挑选权重最低的PeerLauncher，剔除下载队列，将剔除的Peer插入到Peer队列头部，然后重新生成一个PeerLauncher。
	 * </p>
	 */
	public void optimize() {
		LOGGER.debug("优化PeerLauncher");
		synchronized (this.peerLaunchers) {
			try {
				inferiorPeerLaunchers();
				buildPeerLaunchers();
			} catch (Exception e) {
				LOGGER.error("优化PeerLauncher异常", e);
			}
		}
	}
	
	/**
	 * 获取优质的PeerSession，同时清空旧数据。
	 */
	public List<PeerSession> optimizePeerSession() {
		final var list = new ArrayList<>(this.optimize);
		this.optimize.clear();
		return list;
	}

	/**
	 * <p>资源释放</p>
	 * <p>释放所有连接的PeerLauncher。</p>
	 */
	public void release() {
		LOGGER.debug("释放PeerLauncherGroup");
		// 解除PeerLaunchers锁，防止暂停任务卡死。
		this.release(false);
		synchronized (this.peerLaunchers) {
			this.peerLaunchers.forEach(launcher -> {
				SystemThreadContext.submit(() -> {
					launcher.release();
				});
				PeerManager.getInstance().preference(this.torrentSession.infoHashHex(), launcher.peerSession());
			});
			this.peerLaunchers.clear();
		}
	}
	
	/**
	 * <p>创建PeerLauncher列表</p>
	 * <p>创建数量达到最大Peer连接或者重试次数超过{@link #MAX_BUILD_SIZE}时退出创建。</p>
	 */
	private void buildPeerLaunchers() {
		LOGGER.debug("优化PeerLauncher：创建PeerLauncher");
		int size = 0;
		this.build.set(true); // 重置创建状态
		// 重置信号量
		this.buildSemaphore.drainPermits();
		this.buildSemaphore.release(BUILD_SIZE);
		while(this.build.get()) {
			this.acquire();
			if(!this.build.get()) { // 再次判断状态
				break;
			}
			this.torrentSession.submit(() -> {
				boolean ok = true; // 是否继续创建
				try {
					ok = buildPeerLauncher();
				} catch (Exception e) {
					LOGGER.error("创建PeerLauncher异常", e);
				} finally {
					this.release(ok);
				}
			});
			if(++size >= MAX_BUILD_SIZE) {
				LOGGER.debug("优化PeerLauncher：超过单次最大创建数量退出循环");
				break;
			}
		}
	}
	
	/**
	 * <p>创建PeerLauncher加入下载队列，从Peer队列尾部拿出一个Peer创建下载，失败后插入Peer队列头部。</p>
	 * <dl>
	 * 	<dt>退出创建：</dt>
	 * 	<dd>任务不处于下载状态</dd>
	 * 	<dd>已经处于下载的PeerLauncher大于等于配置的最大PeerLauncher数量</dd>
	 * 	<dd>不能查找到更多的Peer</dd>
	 * </dl>
	 * 
	 * @return true-继续；false-停止；
	 */
	private boolean buildPeerLauncher() {
		if(!this.torrentSession.running()) {
			return false;
		}
		if(this.peerLaunchers.size() >= SystemConfig.getPeerSize()) {
			return false;
		}
		final PeerSession peerSession = PeerManager.getInstance().pick(this.torrentSession.infoHashHex());
		if(peerSession != null) {
			final PeerLauncher launcher = PeerLauncher.newInstance(peerSession, this.torrentSession);
			final boolean ok = launcher.handshake();
			if(ok) {
				peerSession.status(PeerConfig.STATUS_DOWNLOAD);
				this.offer(launcher);
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
	 * <p>劣质Peer释放资源，然后将其放入Peer队列头部。</p>
	 * <p>
	 * 挑选评分最低的PeerLauncher作为劣质Peer。
	 * 如果其中含有不可用的PeerLauncher，直接剔除该PeerLauncher。
	 * 如果存在不可用的PeerLauncher时，则不剔除劣质PeerLauncher。
	 * 必须循环完所有的PeerLauncher，清除评分进行新一轮的评分计算。
	 * </p>
	 * <p>不可用的PeerLauncher：状态不可用、评分=0。</p>
	 * 
	 * TODO：评分=0是否直接剔除
	 */
	private void inferiorPeerLaunchers() {
		LOGGER.debug("优化PeerLauncher：剔除劣质PeerLauncher");
		int index = 0;
		boolean unusable = false; // 是否已经剔除不可用的Peer
		long mark = 0, minMark = 0;
		PeerLauncher tmp = null;
		PeerLauncher inferior = null; // 劣质PeerLauncher
		final int size = this.peerLaunchers.size();
		while(true) {
			if(index++ >= size) {
				break;
			}
			tmp = this.peerLaunchers.poll();
			if(tmp == null) {
				break;
			}
			// 状态不可用的PeerLauncher直接剔除，不执行后面操作。
			if(!tmp.available()) {
				unusable = true;
				inferiorPeerLauncher(tmp);
				continue;
			}
			// 获取评分
			mark = tmp.mark();
			// 第一次评分忽略
			if(!tmp.marked()) {
				this.offer(tmp);
				continue;
			}
			if(mark > 0) {
				this.optimize.add(tmp.peerSession());
			} else {
				// 评分=0直接剔除
				unusable = true;
				inferiorPeerLauncher(tmp);
				continue;
			}
			if(inferior == null) {
				inferior = tmp;
				minMark = mark;
			} else if(mark < minMark) {
				this.offer(inferior);
				inferior = tmp;
				minMark = mark;
			} else {
				this.offer(tmp);
			}
		}
		// 已经剔除无用的PeerLauncher，劣质Peer重新加入队列。
		if(unusable) {
			if(inferior != null) {
				this.offer(inferior);
			}
		} else {
			inferiorPeerLauncher(inferior);
		}
	}
	
	/**
	 * <p>剔除劣质Peer</p>
	 * <p>释放Peer资源，将其放入Peer队列头部。</p>
	 */
	private void inferiorPeerLauncher(PeerLauncher peerLauncher) {
		if(peerLauncher != null) {
			final PeerSession peerSession = peerLauncher.peerSession();
			LOGGER.debug("剔除劣质PeerLauncher：{}-{}", peerSession.host(), peerSession.peerPort());
			SystemThreadContext.submit(() -> {
				peerLauncher.release();
			});
			PeerManager.getInstance().inferior(this.torrentSession.infoHashHex(), peerSession);
		}
	}
	
	private void offer(PeerLauncher peerLauncher) {
		final var ok = this.peerLaunchers.offer(peerLauncher);
		if(!ok) {
			LOGGER.warn("PeerLauncher丢失：{}", peerLauncher);
		}
	}
	
	/**
	 * <p>获取信号量</p>
	 */
	private void acquire() {
		try {
			this.buildSemaphore.acquire();
		} catch (InterruptedException e) {
			LOGGER.error("信号量获取异常", e);
		}
	}
	
	/**
	 * 释放信号量，设置创建状态。
	 */
	private void release(boolean build) {
		this.build.set(build);
		this.buildSemaphore.release();
	}

}
