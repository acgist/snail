package com.acgist.snail.protocol.magnet;

import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.downloader.magnet.MagnetDownloader;
import com.acgist.snail.net.bt.TorrentManager;
import com.acgist.snail.pojo.bean.Magnet;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.entity.TaskEntity.Status;
import com.acgist.snail.pojo.entity.TaskEntity.Type;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.protocol.magnet.bootstrap.MagnetReader;
import com.acgist.snail.system.config.FileTypeConfig.FileType;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * 磁力链接协议（只支持BT磁力链接）
 * 磁力链接通过Tracker服务器获取Peer，然后交换种子，如果找不到Peer则转换失败。
 * 其他实现方式：
 * 	1.通过DHT查询（要找很久）
 * 	2.使用第三方的种子库（磁力链接转种子）
 * 转换失败：删除Peer/Torrent
 * 
 * @author acgist
 * @since 1.0.0
 */
public class MagnetProtocol extends Protocol {

	public static final String MAGNET_PREFIX = "magnet:?xt=urn:btih:"; // 磁力链接前缀
	
	public static final String MAGNET_REGEX = "magnet:\\?.+"; // 磁力链接正则表达式
	
	public static final String MAGNET_HASH_32_REGEX = "[a-zA-Z0-9]{32}"; // 32位磁力链接HASH正则表达式
	public static final String MAGNET_HASH_40_REGEX = "[a-zA-Z0-9]{40}"; // 40位磁力链接HASH正则表达式
	
	private static final MagnetProtocol INSTANCE = new MagnetProtocol();
	
	private Magnet magnet;
	
	private MagnetProtocol() {
		super(Type.magnet, MAGNET_REGEX, MAGNET_HASH_32_REGEX, MAGNET_HASH_40_REGEX);
	}
	
	public static final MagnetProtocol getInstance() {
		return INSTANCE;
	}
	
	@Override
	public String name() {
		return "磁力链接";
	}
	
	@Override
	public IDownloader buildDownloader(TaskSession taskSession) {
		return MagnetDownloader.newInstance(taskSession);
	}
	
	@Override
	public boolean available() {
		return true;
	}
	
	@Override
	protected Protocol convert() throws DownloadException {
		return null;
	}
	
	@Override
	protected boolean buildTaskEntity() throws DownloadException {
		magnet();
		if(TorrentManager.getInstance().exist(this.magnet.getHash())) {
			throw new DownloadException("BT任务已经存在");
		}
		final TaskEntity taskEntity = new TaskEntity();
		final String fileName = buildFileName(); // 文件名称
		taskEntity.setUrl(this.url);
		taskEntity.setType(this.type);
		taskEntity.setStatus(Status.await);
		taskEntity.setName(fileName);
		taskEntity.setFile(buildFile(fileName));
		taskEntity.setFileType(FileType.torrent);
		taskEntity.setSize(0L); // 后面选择下载文件时再修改
		this.taskEntity = taskEntity;
		buildTorrentFolder();
		return true;
	}
	
	@Override
	protected void cleanMessage() {
		this.magnet = null;
	}
	
	private void magnet() throws DownloadException {
		final MagnetReader reader = MagnetReader.newInstance(this.url);
		this.magnet = reader.magnet();
	}
	
	@Override
	protected String buildFileName() {
		return this.magnet.getHash();
	}
	
	/**
	 * 创建下载目录
	 */
	private void buildTorrentFolder() {
		FileUtils.buildFolder(this.taskEntity.getFile(), false);
	}
	
	/**
	 * 验证磁力链接
	 */
	public static final boolean verify(String url) {
		return
			verifyMagnet(url) ||
			verifyMagnetHash32(url) ||
			verifyMagnetHash40(url);
	}

	/**
	 * 验证磁力链接
	 */
	public static final boolean verifyMagnet(String url) {
		return StringUtils.regex(url, MAGNET_REGEX, true);
	}
	
	/**
	 * 验证32位磁力链接HASH
	 */
	public static final boolean verifyMagnetHash32(String url) {
		return StringUtils.regex(url, MAGNET_HASH_32_REGEX, true);
	}
	
	/**
	 * 验证40位磁力链接HASH
	 */
	public static final boolean verifyMagnetHash40(String url) {
		return StringUtils.regex(url, MAGNET_HASH_40_REGEX, true);
	}
	
	/**
	 * 将磁力链接HASH转为磁力链接
	 */
	public static final String buildMagnet(String hash) {
		if(verifyMagnet(hash)) {
			return hash;
		}
		return MAGNET_PREFIX + hash.toLowerCase();
	}
	
}
