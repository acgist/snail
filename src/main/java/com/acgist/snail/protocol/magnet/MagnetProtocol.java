package com.acgist.snail.protocol.magnet;

import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.downloader.magnet.MagnetDownloader;
import com.acgist.snail.net.torrent.TorrentManager;
import com.acgist.snail.net.torrent.peer.bootstrap.ltep.MetadataMessageHandler;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.ITaskSession.FileType;
import com.acgist.snail.pojo.bean.Magnet;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.protocol.magnet.bootstrap.MagnetBuilder;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.FileUtils;

/**
 * <p>磁力链接协议</p>
 * <p>原理：磁力链接通过Tracker服务器和DHT网络获取Peer，然后使用{@linkplain MetadataMessageHandler 扩展协议}交换种子。</p>
 * <dl>
 * 	<dt>其他实现方式</dt>
 * 	<dd>使用第三方种子库（磁力链接转种子）</dd>
 * </dl>
 * 
 * TODO：磁力链接交换完成后修改文件大小
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class MagnetProtocol extends Protocol {
	
	private static final MagnetProtocol INSTANCE = new MagnetProtocol();

	/**
	 * <p>磁力链接信息</p>
	 */
	private Magnet magnet;
	
	private MagnetProtocol() {
		super(Type.MAGNET);
	}
	
	public static final MagnetProtocol getInstance() {
		return INSTANCE;
	}
	
	@Override
	public String name() {
		return "磁力链接";
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
		final Magnet magnet = MagnetBuilder.newInstance(this.url).build();
		exist(magnet);
		magnet(magnet);
	}
	
	@Override
	protected String buildFileName() {
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
		this.taskEntity.setSize(0L);
	}
	
	@Override
	protected void done() {
		buildTorrentFolder();
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>注意：一定要先检查磁力链接是否已经存在（如果已经存在不能赋值：防止清除已下载任务）</p>
	 */
	@Override
	protected void cleanMessage(boolean ok) {
		if(!ok) {
			// 清除种子信息
			if(this.magnet != null) {
				TorrentManager.getInstance().remove(this.magnet.getHash());
			}
		}
		this.magnet = null;
	}
	
	/**
	 * <p>是否已经存在下载任务</p>
	 */
	private void exist(Magnet magnet) throws DownloadException {
		if(TorrentManager.getInstance().exist(magnet.getHash())) {
			throw new DownloadException("任务已经存在");
		}
	}
	
	/**
	 * <p>设置磁力链接</p>
	 */
	private void magnet(Magnet magnet) throws DownloadException {
		this.magnet = magnet;
	}
	
	/**
	 * <p>创建下载目录</p>
	 */
	private void buildTorrentFolder() {
		FileUtils.buildFolder(this.taskEntity.getFile(), false);
	}
	
}
