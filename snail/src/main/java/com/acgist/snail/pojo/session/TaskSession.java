package com.acgist.snail.pojo.session;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.acgist.snail.context.EntityContext;
import com.acgist.snail.context.GuiContext;
import com.acgist.snail.context.ProtocolContext;
import com.acgist.snail.context.StatisticsContext;
import com.acgist.snail.context.SystemThreadContext;
import com.acgist.snail.context.TaskContext;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.pojo.IStatisticsSession;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.wrapper.MultifileSelectorWrapper;
import com.acgist.snail.protocol.Protocol.Type;
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
		this.statistics = new StatisticsSession(true, StatisticsContext.getInstance().statistics());
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
	public IDownloader downloader() {
		return this.downloader;
	}
	
	@Override
	public IDownloader buildDownloader() throws DownloadException {
		if(this.downloader != null) {
			return this.downloader;
		}
		this.downloader = ProtocolContext.getInstance().buildDownloader(this);
		return this.downloader;
	}
	
	@Override
	public File downloadFile() {
		return new File(this.getFile());
	}
	
	@Override
	public File downloadFolder() {
		final File file = this.downloadFile();
		if(file.isFile()) {
			return file.getParentFile();
		} else {
			return file;
		}
	}
	
	@Override
	public List<String> multifileSelected() {
		final String description = this.getDescription();
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
	public void buildDownloadSize() {
		this.downloadSize(FileUtils.fileSize(this.getFile()));
	}
	
	@Override
	public boolean statusAwait() {
		return this.getStatus() == Status.AWAIT;
	}
	
	@Override
	public boolean statusPause() {
		return this.getStatus() == Status.PAUSE;
	}
	
	@Override
	public boolean statusDownload() {
		return this.getStatus() == Status.DOWNLOAD;
	}
	
	@Override
	public boolean statusCompleted() {
		return this.getStatus() == Status.COMPLETED;
	}
	
	@Override
	public boolean statusFail() {
		return this.getStatus() == Status.FAIL;
	}
	
	@Override
	public boolean statusDelete() {
		return this.getStatus() == Status.DELETE;
	}
	
	@Override
	public boolean statusRunning() {
		return this.statusAwait() || this.statusDownload();
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
		return this.getName();
	}

	@Override
	public String getStatusValue() {
		if(this.statusDownload()) {
			return FileUtils.formatSize(this.statistics.downloadSpeed()) + "/S";
		} else {
			return this.getStatus().getValue();
		}
	}
	
	@Override
	public String getProgressValue() {
		if(this.statusCompleted()) {
			return FileUtils.formatSize(this.getSize());
		} else {
			return FileUtils.formatSize(this.downloadSize()) + "/" + FileUtils.formatSize(this.getSize());
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
		if(this.getEndDate() == null) {
			if(this.statusDownload()) {
				final long downloadSpeed = this.statistics.downloadSpeed();
				if(downloadSpeed == 0L) {
					return "-";
				} else {
					// 剩余下载时间
					long second = (this.getSize() - this.downloadSize()) / downloadSpeed;
					if(second <= 0) {
						second = 0;
					}
					return DateUtils.format(second);
				}
			} else {
				return "-";
			}
		} else {
			return DateUtils.dateFormat(this.getEndDate(), PATTERN);
		}
	}
	
	//================实体操作================//
	
	@Override
	public void reset() {
		// 非常重要：如果任务被错误的保存为下载状态需要重置为等待状态（否者不能正常下载）
		if(this.statusDownload()) {
			this.setStatus(Status.AWAIT);
		}
	}
	
	@Override
	public void start() throws DownloadException {
		if(this.statusDownload()) {
			// 任务已经开始不修改状态
			return;
		}
		if(this.statusCompleted()) {
			// 任务已经完成不修改状态
			return;
		}
		// 提交下载队列
		TaskContext.getInstance().submit(this);
		this.updateStatus(Status.AWAIT);
	}
	
	@Override
	public void restart() throws DownloadException {
		// 暂停任务
		this.pause();
		// 删除下载器
		this.downloader = null;
		if(this.statusCompleted()) {
			// 已经完成任务：修改状态、清空完成时间
			this.setStatus(Status.AWAIT);
			this.setEndDate(null);
		}
		this.start();
	}
	
	@Override
	public void await() {
		if(this.statusDownload()) {
			// 下载中的任务修改等待
			this.setStatus(Status.AWAIT);
			// 直接调用解除下载：不用保存状态
			this.unlockDownload();
		}
	}
	
	@Override
	public void pause() {
		if(this.statusPause()) {
			// 任务已经暂停不修改状态
			return;
		}
		if(this.statusCompleted()) {
			// 任务已经完成不修改状态
			return;
		}
		this.updateStatus(Status.PAUSE);
	}
	
	@Override
	public void repause() {
		if(this.statusCompleted()) {
			this.setStatus(Status.PAUSE);
			this.setEndDate(null);
			this.update();
		}
	}
	
	@Override
	public void delete() {
		if(this.statusDelete()) {
			// 任务已经删除不修改状态
			return;
		}
		if(this.statusDownload()) {
			// 正在下载：标记删除下载结束自动释放
			this.updateStatus(Status.DELETE);
		} else if(this.downloader != null) {
			// 没有下载：异步删除
			SystemThreadContext.submit(this.downloader::delete);
		}
		// 删除下载任务
		TaskContext.getInstance().remove(this);
		// 删除下载器
		this.downloader = null;
		// 删除实体
		EntityContext.getInstance().delete(this.entity);
	}

	@Override
	public void refresh() throws DownloadException {
		if(this.downloader != null) {
			this.downloader.refresh();
		}
	}
	
	@Override
	public boolean verify() throws DownloadException {
		if(this.downloader == null) {
			return this.downloadFile().exists();
		}
		return this.downloader.verify();
	}
	
	@Override
	public void unlockDownload() {
		if(this.downloader != null) {
			this.downloader.unlockDownload();
		}
	}
	
	@Override
	public void update() {
		EntityContext.getInstance().update(this.entity);
	}
	
	@Override
	public void updateStatus(Status status) {
		if(this.statusCompleted()) {
			return;
		}
		if(status == Status.COMPLETED) {
			this.setEndDate(new Date()); // 设置完成时间
		}
		this.setStatus(status);
		this.update();
		this.unlockDownload(); // 状态修改完成才能调用
		TaskContext.getInstance().refresh(); // 刷新下载
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
		GuiContext.getInstance().refreshTaskStatus(); // 刷新状态
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
