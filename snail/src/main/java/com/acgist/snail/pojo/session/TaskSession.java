package com.acgist.snail.pojo.session;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.acgist.snail.context.EntityContext;
import com.acgist.snail.context.SystemStatistics;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.downloader.DownloaderManager;
import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.gui.GuiManager;
import com.acgist.snail.pojo.IStatisticsSession;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.wrapper.MultifileSelectorWrapper;
import com.acgist.snail.protocol.Protocol.Type;
import com.acgist.snail.protocol.ProtocolManager;
import com.acgist.snail.utils.BeanUtils;
import com.acgist.snail.utils.DateUtils;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>任务信息</p>
 * 
 * @author acgist
 */
public final class TaskSession implements ITaskSession {
	
	/**
	 * <p>时间格式</p>
	 */
	private static final String PATTERN = "yyyy-MM-dd HH:mm";

	/**
	 * <p>下载器</p>
	 */
	private IDownloader downloader;
	/**
	 * <p>任务</p>
	 */
	private final TaskEntity entity;
	/**
	 * <p>统计</p>
	 */
	private final IStatisticsSession statistics;
	
	/**
	 * @param entity 任务
	 * 
	 * @throws DownloadException 下载异常
	 */
	private TaskSession(TaskEntity entity) throws DownloadException {
		if(entity == null) {
			throw new DownloadException("创建TaskSession失败（任务不存在）");
		}
		this.entity = entity;
		this.statistics = new StatisticsSession(true, SystemStatistics.getInstance().statistics());
	}
	
	/**
	 * <p>新建任务信息</p>
	 * 
	 * @param entity 任务
	 * 
	 * @return 任务信息
	 * 
	 * @throws DownloadException 下载异常
	 */
	public static final ITaskSession newInstance(TaskEntity entity) throws DownloadException {
		return new TaskSession(entity);
	}

	@Override
	public void verify() {
		if(this.downloader != null) {
			this.downloader.verify();
		}
	}
	
	@Override
	public void reset() {
		// 非常重要：如果任务被错误的保存为下载状态需要重置为等待状态（否者不能正常下载）
		if(this.download()) {
			this.setStatus(Status.AWAIT);
		}
	}
	
	@Override
	public IDownloader downloader() {
		return this.downloader;
	}

	@Override
	public IDownloader removeDownloader() {
		final IDownloader downloader = this.downloader;
		this.downloader = null;
		return downloader;
	}
	
	@Override
	public IDownloader buildDownloader() throws DownloadException {
		if(this.downloader != null) {
			return this.downloader;
		}
		this.downloader = ProtocolManager.getInstance().buildDownloader(this);
		return this.downloader;
	}
	
	@Override
	public File downloadFolder() {
		final File file = new File(this.entity.getFile());
		if(file.isFile()) {
			return file.getParentFile();
		} else {
			return file;
		}
	}
	
	@Override
	public List<String> multifileSelected() {
		final String description = this.entity.getDescription();
		if(StringUtils.isEmpty(description)) {
			return List.of();
		} else {
			final MultifileSelectorWrapper wrapper = MultifileSelectorWrapper.newDecoder(description);
			return wrapper.deserialize();
		}
	}
	
	@Override
	public IStatisticsSession statistics() {
		return this.statistics;
	}
	
	@Override
	public long downloadSize() {
		return this.statistics.downloadSize();
	}
	
	@Override
	public void downloadSize(long size) {
		this.statistics.downloadSize(size);
	}

	@Override
	public boolean await() {
		return this.entity.getStatus() == Status.AWAIT;
	}
	
	@Override
	public boolean pause() {
		return this.entity.getStatus() == Status.PAUSE;
	}
	
	@Override
	public boolean download() {
		return this.entity.getStatus() == Status.DOWNLOAD;
	}
	
	@Override
	public boolean complete() {
		return this.entity.getStatus() == Status.COMPLETE;
	}
	
	@Override
	public boolean inThreadPool() {
		return this.await() || this.download();
	}
	
	@Override
	public Map<String, Object> taskMessage() {
		return BeanUtils.toMap(this.entity).entrySet().stream()
			.filter(entry -> entry.getKey() != null && entry.getValue() != null)
			.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}
	
	//================Gui面板数据绑定================//
	
	@Override
	public String getNameValue() {
		return this.entity.getName();
	}

	@Override
	public String getStatusValue() {
		if(this.download()) {
			return FileUtils.formatSize(this.statistics.downloadSpeed()) + "/S";
		} else {
			return this.entity.getStatus().getValue();
		}
	}
	
	@Override
	public String getProgressValue() {
		if(this.complete()) {
			return FileUtils.formatSize(this.entity.getSize());
		} else {
			return FileUtils.formatSize(this.statistics.downloadSize()) + "/" + FileUtils.formatSize(this.entity.getSize());
		}
	}

	@Override
	public String getCreateDateValue() {
		if(this.entity.getCreateDate() == null) {
			return "-";
		} else {
			return DateUtils.dateFormat(this.entity.getCreateDate(), PATTERN);
		}
	}
	
	@Override
	public String getEndDateValue() {
		if(this.entity.getEndDate() == null) {
			if(this.download()) {
				final long downloadSpeed = this.statistics.downloadSpeed();
				if(downloadSpeed == 0L) {
					return "-";
				} else {
					// 剩余下载时间
					long second = (this.entity.getSize() - this.statistics.downloadSize()) / downloadSpeed;
					if(second <= 0) {
						second = 0;
					}
					return DateUtils.format(second);
				}
			} else {
				return "-";
			}
		} else {
			return DateUtils.dateFormat(this.entity.getEndDate(), PATTERN);
		}
	}
	
	//================实体操作================//
	
	@Override
	public void update() {
		EntityContext.getInstance().update(this.entity);
	}
	
	@Override
	public void delete() {
		EntityContext.getInstance().delete(this.entity);
	}

	@Override
	public void updateStatus(Status status) {
		if(this.complete()) {
			return;
		}
		if(status == Status.COMPLETE) {
			this.entity.setEndDate(new Date()); // 设置完成时间
		}
		this.entity.setStatus(status);
		EntityContext.getInstance().update(this.entity);
		DownloaderManager.getInstance().refresh(); // 刷新下载
		GuiManager.getInstance().refreshTaskStatus(); // 刷新状态
	}
	
	//================实体================//
	
	@Override
	public String getId() {
		return this.entity.getId();
	}
	
	@Override
	public void setId(String id) {
		this.entity.setId(id);
	}
	
	@Override
	public String getName() {
		return this.entity.getName();
	}
	
	@Override
	public void setName(String name) {
		this.entity.setName(name);
	}
	
	@Override
	public Type getType() {
		return this.entity.getType();
	}
	
	@Override
	public void setType(Type type) {
		this.entity.setType(type);
	}
	
	@Override
	public FileType getFileType() {
		return this.entity.getFileType();
	}
	
	@Override
	public void setFileType(FileType fileType) {
		this.entity.setFileType(fileType);
	}
	
	@Override
	public String getFile() {
		return this.entity.getFile();
	}
	
	@Override
	public void setFile(String file) {
		this.entity.setFile(file);
	}
	
	@Override
	public String getUrl() {
		return this.entity.getUrl();
	}
	
	@Override
	public void setUrl(String url) {
		this.entity.setUrl(url);
	}
	
	@Override
	public String getTorrent() {
		return this.entity.getTorrent();
	}
	
	@Override
	public void setTorrent(String torrent) {
		this.entity.setTorrent(torrent);
	}
	
	@Override
	public Status getStatus() {
		return this.entity.getStatus();
	}
	
	@Override
	public void setStatus(Status status) {
		this.entity.setStatus(status);
	}
	
	@Override
	public Long getSize() {
		return this.entity.getSize();
	}
	
	@Override
	public void setSize(Long size) {
		this.entity.setSize(size);
	}
	
	@Override
	public Date getEndDate() {
		return this.entity.getEndDate();
	}
	
	@Override
	public void setEndDate(Date endDate) {
		this.entity.setEndDate(endDate);
	}
	
	@Override
	public String getDescription() {
		return this.entity.getDescription();
	}
	
	@Override
	public void setDescription(String description) {
		this.entity.setDescription(description);
	}
	
	@Override
	public byte[] getPayload() {
		return this.entity.getPayload();
	}

	@Override
	public void setPayload(byte[] payload) {
		this.entity.setPayload(payload);
	}
	
}
