package com.acgist.snail.pojo.session;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.acgist.snail.config.SymbolConfig;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.EntityContext;
import com.acgist.snail.context.GuiContext;
import com.acgist.snail.context.ProtocolContext;
import com.acgist.snail.context.StatisticsContext;
import com.acgist.snail.context.TaskContext;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.StatisticsGetter;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.wrapper.DescriptionWrapper;
import com.acgist.snail.protocol.Protocol.Type;
import com.acgist.snail.utils.BeanUtils;
import com.acgist.snail.utils.DateUtils;
import com.acgist.snail.utils.FileUtils;

/**
 * <p>任务信息</p>
 * 
 * @author acgist
 */
public final class TaskSession extends StatisticsGetter implements ITaskSession {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TaskSession.class);
	
	/**
	 * <p>时间格式：{@value}</p>
	 */
	private static final String PATTERN = "yyyy-MM-dd HH:mm";
	/**
	 * <p>任务状态：{@value}</p>
	 */
	private static final String TASK_STATUS_VALUE = "statusValue";
	/**
	 * <p>删除等待时间（毫秒）：{@value}</p>
	 */
	private static final long DELETE_TIMEOUT = 2L * SystemConfig.ONE_SECOND_MILLIS;

	/**
	 * <p>下载器</p>
	 */
	private IDownloader downloader;
	/**
	 * <p>任务</p>
	 */
	private final TaskEntity entity;
	/**
	 * <p>删除锁</p>
	 */
	private final AtomicBoolean deleteLock;
	
	/**
	 * @param entity 任务
	 */
	private TaskSession(TaskEntity entity) {
		super(new StatisticsSession(true, StatisticsContext.getInstance().statistics()));
		this.entity = entity;
		this.deleteLock = new AtomicBoolean(false);
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
		if(entity == null) {
			throw new DownloadException("新建TaskSession失败（entity）");
		}
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
		return DescriptionWrapper.newDecoder(this.getDescription()).deserialize();
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
	public Map<String, Object> taskMessage() {
		final Map<String, Object> data = BeanUtils.toMap(this.entity).entrySet().stream()
			.filter(entry -> entry.getKey() != null && entry.getValue() != null)
			.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		data.put(TASK_STATUS_VALUE, this.getStatusValue());
		return data;
	}
	
	@Override
	public boolean statusAwait() {
		return this.getStatus() == Status.AWAIT;
	}
	
	@Override
	public boolean statusDownload() {
		return this.getStatus() == Status.DOWNLOAD;
	}
	
	@Override
	public boolean statusPause() {
		return this.getStatus() == Status.PAUSE;
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
	public String getNameValue() {
		return this.getName();
	}

	@Override
	public String getStatusValue() {
		if(this.statusDownload()) {
			return FileUtils.formatSpeed(this.statistics.downloadSpeed());
		} else {
			return this.getStatus().getValue();
		}
	}
	
	@Override
	public String getProgressValue() {
		if(this.statusCompleted()) {
			return FileUtils.formatSize(this.getSize());
		} else {
			return SymbolConfig.Symbol.SLASH.join(FileUtils.formatSize(this.downloadSize()), FileUtils.formatSize(this.getSize()));
		}
	}

	@Override
	public String getCreateDateValue() {
		if(this.entity.getCreateDate() == null) {
			return SymbolConfig.Symbol.MINUS.toString();
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
					return SymbolConfig.Symbol.MINUS.toString();
				} else {
					// 剩余下载时间
					long second = (this.getSize() - this.downloadSize()) / downloadSpeed;
					if(second <= 0) {
						second = 0;
					}
					return DateUtils.format(second);
				}
			} else {
				return SymbolConfig.Symbol.MINUS.toString();
			}
		} else {
			return DateUtils.dateFormat(this.getEndDate(), PATTERN);
		}
	}
	
	@Override
	public void reset() {
		if(this.statusDownload()) {
			this.setStatus(Status.AWAIT);
		}
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
		// 删除旧下载器
		this.downloader = null;
		if(this.statusCompleted()) {
			// 已经完成任务：修改状态、清空完成时间
			this.setStatus(Status.AWAIT);
			this.setEndDate(null);
		}
		this.start();
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
			// 任务已经处于删除状态
			return;
		}
		if(this.statusDownload()) {
			// 正在下载：标记删除下载结束释放资源
			this.deleteLock.set(true);
			this.updateStatus(Status.DELETE);
			this.lockDelete();
		}
		// 删除旧下载器
		if(this.downloader != null) {
			this.downloader.delete();
			this.downloader = null;
		}
		// 删除任务
		TaskContext.getInstance().remove(this);
		// 删除实体
		EntityContext.getInstance().delete(this.entity);
	}
	
	/**
	 * <p>添加删除锁</p>
	 * <p>下载中任务删除时需要等待文件释放：防止删除文件失败</p>
	 */
	private void lockDelete() {
		if(this.deleteLock.get()) {
			synchronized (this.deleteLock) {
				if(this.deleteLock.get()) {
					try {
						this.deleteLock.wait(DELETE_TIMEOUT);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						LOGGER.debug("线程等待异常", e);
					}
				}
			}
		}
	}
	
	@Override
	public void unlockDelete() {
		synchronized (this.deleteLock) {
			this.deleteLock.set(false);
			this.deleteLock.notifyAll();
		}
	}

	@Override
	public void refresh() throws DownloadException {
		if(this.downloader != null) {
			this.downloader.refresh();
		}
	}
	
	@Override
	public boolean verify() {
		// 任务状态
		if(this.entity.getStatus() != Status.COMPLETED) {
			return false;
		}
		// 下载文件
		if(this.downloader == null) {
			return this.downloadFile().exists();
		}
		// 下载任务
		try {
			return this.downloader.verify();
		} catch (DownloadException e) {
			LOGGER.error("校验下载文件异常", e);
		}
		return false;
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
			// 设置完成时间
			this.setEndDate(new Date());
		}
		this.setStatus(status);
		this.update();
		// 释放下载锁：修改状态后再释放
		this.unlockDownload();
		// 刷新下载任务
		TaskContext.getInstance().refresh();
	}

	@Override
	public void magnetToTorrent() {
		if(this.getType() == Type.MAGNET) {
			// 修改任务下载类型
			this.setType(Type.TORRENT);
			// 修改已经下载大小
			this.downloadSize(0L);
		}
	}
	
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
		// 刷新状态
		GuiContext.getInstance().refreshTaskStatus();
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
	
	@Override
	public String toString() {
		return BeanUtils.toString(this, this.getName());
	}
	
}
