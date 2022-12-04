package com.acgist.snail.protocol.magnet;

import com.acgist.snail.context.ITaskSession;
import com.acgist.snail.context.ITaskSession.FileType;
import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.downloader.magnet.MagnetDownloader;
import com.acgist.snail.net.DownloadException;
import com.acgist.snail.net.torrent.Magnet;
import com.acgist.snail.net.torrent.TorrentContext;
import com.acgist.snail.net.torrent.peer.extension.MetadataMessageHandler;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>磁力链接协议</p>
 * <p>原理：磁力链接通过Tracker服务器和DHT网络获取Peer，然后使用{@linkplain MetadataMessageHandler 扩展协议}交换种子。</p>
 * 
 * @author acgist
 */
public final class MagnetProtocol extends Protocol {
	
	private static final MagnetProtocol INSTANCE = new MagnetProtocol();
	
	public static final MagnetProtocol getInstance() {
		return INSTANCE;
	}

	/**
	 * <p>磁力链接</p>
	 */
	private Magnet magnet;
	
	private MagnetProtocol() {
		super(Type.MAGNET, "磁力链接");
	}
	
	@Override
	public boolean available() {
		return true;
	}
	
	@Override
	public IDownloader buildDownloader(ITaskSession taskSession) {
		return MagnetDownloader.newInstance(taskSession);
	}
	
	@Override
	protected void prep() throws DownloadException {
		final Magnet magnetCheck = MagnetBuilder.newInstance(this.url).build();
		this.checkExist(magnetCheck);
		this.magnet = magnetCheck;
	}
	
	@Override
	protected String buildFileName() {
		final String dn = this.magnet.getDn();
		if(StringUtils.isNotEmpty(dn)) {
			return dn;
		}
		return this.magnet.getHash();
	}
	
	@Override
	protected void buildName(String fileName) {
		this.taskEntity.setName(fileName);
	}
	
	@Override
	protected void buildFileType(String fileName) {
		this.taskEntity.setFileType(FileType.TORRENT);
	}
	
	@Override
	protected void buildSize() throws DownloadException {
		// 磁力链接下载完成修改大小
		this.taskEntity.setSize(0L);
	}
	
	@Override
	protected void done() {
		this.buildFolder();
	}
	
	@Override
	protected void release(boolean success) {
		if(!success && this.magnet != null) {
			// 清除种子信息
			TorrentContext.getInstance().remove(this.magnet.getHash());
		}
		this.magnet = null;
		super.release(success);
	}
	
	/**
	 * <p>检查任务是否已经存在</p>
	 * <p>一定要先检查BT任务是否已经存在（如果已经存在不能赋值：防止清除下载任务）</p>
	 * 
	 * @param magnet 磁力链接
	 * 
	 * @throws DownloadException 下载异常
	 */
	private void checkExist(Magnet magnet) throws DownloadException {
		if(TorrentContext.getInstance().exist(magnet.getHash())) {
			throw new DownloadException("任务已经存在");
		}
	}
	
	/**
	 * <p>新建下载目录</p>
	 */
	private void buildFolder() {
		FileUtils.buildFolder(this.taskEntity.getFile());
	}
	
}
