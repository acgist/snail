package com.acgist.snail.net.torrent.bootstrap;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
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
 * <p>PeerLauncher组：下载</p>
 * <p>
 * 对正在进行下载的PeerLauncher管理：<br>
 * <ul>
 * 	<li>创建PeerLauncher。</li>
 * 	<li>定时替换下载最慢的PeerLauncher。</li>
 * </ul>
 * </p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class PeerLauncherGroup {

	private static final Logger LOGGER = LoggerFactory.getLogger(PeerLauncherGroup.class);
	
	/**
	 * 同时创建PeerLauncher个数
	 */
	private static final int PARALLEL_BUILD_SIZE = 3;
	/**
	 * 是否继续创建PeerLauncher
	 */
	private final AtomicBoolean build;
	private final TorrentSession torrentSession;
	/**
	 * PeerLauncher下载队列
	 */
	private final BlockingQueue<PeerLauncher> peerLaunchers;
	/**
	 * 优选的Peer，每次优化时挑选出来可以进行下载的Peer，在优化后发送Pex消息发送给连接的Peer，发送完成后清空。
	 */
	private final List<PeerSession> optimize = new ArrayList<>();
	
	private PeerLauncherGroup(TorrentSession torrentSession) {
		this.build = new AtomicBoolean(false);
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
	 * 
	 * @return 当前可以使用的PeerSession
	 */
	public void optimize() {
		LOGGER.debug("优化PeerLauncher");
		synchronized (this.peerLaunchers) {
			try {
				inferiorPeerLauncher();
				buildPeerLaunchers();
			} catch (Exception e) {
				LOGGER.error("优化PeerLauncher异常", e);
			}
		}
	}
	
	/**
	 * 获取优秀的PeerSession，同时清空旧数据。
	 */
	public List<PeerSession> optimizePeerSession() {
		final var list = new ArrayList<>(this.optimize);
		this.optimize.clear();
		return list;
	}

	/**
	 * <p>资源释放</p>
	 * <p>释放所有正在下载的PeerLauncher。</p>
	 */
	public void release() {
		LOGGER.debug("释放PeerLauncherGroup");
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
	 * 生成PeerLauncher列表，生成到不能继续生成为止。
	 */
	private void buildPeerLaunchers() {
		LOGGER.debug("优化PeerLauncher-创建下载PeerLauncher");
		int size = 0;
		this.build.set(true);
		while(this.build.get()) {
			this.torrentSession.submit(() -> {
				try {
					buildPeerLauncher();
				} catch (Exception e) {
					LOGGER.error("创建PeerLauncher异常", e);
				}
			});
			if(++size > PARALLEL_BUILD_SIZE) {
				synchronized (this.build) {
					ThreadUtils.wait(this.build, Duration.ofSeconds(10));
				}
			}
		}
	}
	
	/**
	 * <p>新建PeerLauncher加入下载队列，从Peer列表尾部拿出一个Peer创建下载。</p>
	 * <p>如果任务不处于下载状态、已经处于下载的PeerLauncher大于等于配置的最大PeerLauncher数量、不能查找到更多的Peer时返回不能继续生成。</p>
	 * 
	 * @return true-继续生成；false-不继续生成
	 */
	private boolean buildPeerLauncher() {
		if(!this.torrentSession.downloading()) {
			notifyBuild(false);
			return false;
		}
		if(this.peerLaunchers.size() >= SystemConfig.getPeerSize()) {
			notifyBuild(false);
			return false;
		}
		final PeerSession peerSession = PeerManager.getInstance().pick(this.torrentSession.infoHashHex());
		if(peerSession != null) {
			final PeerLauncher launcher = PeerLauncher.newInstance(peerSession, this.torrentSession);
			final boolean ok = launcher.handshake();
			if(ok) {
				peerSession.status(PeerConfig.STATUS_DOWNLOAD); // 设置下载中
				this.peerLaunchers.offer(launcher);
			} else { // 失败后需要放回队列。
				PeerManager.getInstance().inferior(this.torrentSession.infoHashHex(), peerSession);
			}
			notifyBuild(true);
			return true;
		} else {
			notifyBuild(false);
			return false;
		}
	}
	
	/***
	 * <p>选择劣质Peer，释放资源，然后将劣质Peer放入Peer队列头部。</p>
	 * <p>
	 * 挑选权重最低的PeerLauncher作为劣质Peer，如果其中含有不可用的PeerLauncher，直接剔除该PeerLauncher，
	 * 但是依旧需要循环完所有的PeerLauncher，清除权重进行新一轮的权重计算。
	 * 如果存在不可用的PeerLauncher时，则不剔除分数最低的PeerLauncher。
	 * </p>
	 * <p>
	 * 不可用的Peer：状态不可用或者下载量=0。
	 * </p>
	 */
	private void inferiorPeerLauncher() {
		LOGGER.debug("优化PeerLauncher-剔除劣质PeerLauncher");
		int index = 0;
		boolean unusable = false; // 不可用
		int mark = 0, minMark = 0;
		PeerLauncher tmp = null; // 临时
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
			if(!tmp.available()) { // 如果当前挑选的是不可用的PeerLauncher不执行后面操作
				unusable = true;
				inferiorPeerLauncher(tmp);
				continue;
			}
			mark = tmp.mark(); // 清空权重
			if(mark > 0) { // 添加可用
				this.optimize.add(tmp.peerSession());
			} else { // 如果速度=0，直接剔除
				unusable = true;
				inferiorPeerLauncher(tmp);
				continue;
			}
			if(inferior == null) {
				inferior = tmp;
				minMark = mark;
			} else if(mark < minMark) {
				this.peerLaunchers.offer(inferior);
				inferior = tmp;
				minMark = mark;
			} else {
				this.peerLaunchers.offer(tmp);
			}
		}
		if(unusable) { // 已经删除无用的Peer
			if(inferior != null) {
				this.peerLaunchers.offer(inferior);
			}
		} else {
			inferiorPeerLauncher(inferior);
		}
	}
	
	/**
	 * 剔除劣质Peer，释放资源，放入Peer队列头部。
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
	
	/**
	 * 唤醒创建线程
	 */
	private void notifyBuild(boolean build) {
		synchronized (this.build) {
			this.build.set(build);
			this.build.notifyAll();
		}
	}

}
