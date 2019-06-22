package com.acgist.snail.downloader.torrent;

import java.io.IOException;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.Downloader;
import com.acgist.snail.net.bt.TorrentManager;
import com.acgist.snail.net.bt.peer.bootstrap.PeerManager;
import com.acgist.snail.pojo.bean.Magnet;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.pojo.session.TorrentSession;
import com.acgist.snail.protocol.magnet.bootstrap.MagnetReader;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.ThreadUtils;

/**
 * <p>BT下载器</p>
 * <p>
 * 	下载流程：
 * 		加载种子
 * 		加载Tracker
 * 		获取Peer
 * 		连接Peer
 * 		Peer扩展（DHT）
 * 		交换位图
 * 		开始下载
 * </p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class TorrentDownloader extends Downloader {

	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentDownloader.class);
	
	private TorrentSession torrentSession;
	
	private Object downloadLock = new Object(); // 下载锁
	
	private TorrentDownloader(TaskSession taskSession) {
		super(taskSession);
		loadTorrent();
	}

	public static final TorrentDownloader newInstance(TaskSession taskSession) {
		return new TorrentDownloader(taskSession);
	}

	@Override
	public void open() {
		loadDownload();
	}

	@Override
	public void download() throws IOException {
		while(ok()) {
			synchronized (downloadLock) {
				ThreadUtils.wait(downloadLock, Duration.ofSeconds(Integer.MAX_VALUE));
				this.complete = torrentSession.torrentStreamGroup().complete();
			}
		}
	}

	@Override
	public void unlockDownload() {
		synchronized (downloadLock) {
			downloadLock.notifyAll();
		}
	}
	
	@Override
	public void release() {
		torrentSession.releaseDownload();
	}

	/**
	 * 删除时需要释放文件资源
	 */
	@Override
	public void delete() {
		if(this.torrentSession != null) {
			this.torrentSession.releaseUpload();
			final String infoHashHex = this.torrentSession.infoHashHex();
			PeerManager.getInstance().remove(infoHashHex);
			TorrentManager.getInstance().remove(infoHashHex);
		}
		super.delete();
	}
	
	/**
	 * <p>加载任务</p>
	 * <p>创建时立即加载任务，使任务可以被分享。</p>
	 */
	private void loadTorrent() {
		final var entity = this.taskSession.entity();
		final String path = entity.getTorrent();
		try {
			final Magnet magnet = MagnetReader.newInstance(entity.getUrl()).magnet();
			final String infoHashHex = magnet.getHash();
			this.torrentSession = TorrentManager.getInstance().newTorrentSession(infoHashHex, path);
			this.torrentSession.upload(this.taskSession);
		} catch (DownloadException e) {
			fail("任务加载失败");
			LOGGER.error("任务加载异常", e);
			return;
		}
	}
	
	/**
	 * 加载下载
	 */
	private void loadDownload() {
		try {
			this.complete = this.torrentSession.download();
		} catch (DownloadException e) {
			fail("任务加载失败");
			LOGGER.error("任务加载异常", e);
			return;
		}
	}

}
