package com.acgist.snail.pojo.session;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.acgist.snail.downloader.DownloaderManager;
import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.gui.GuiHandler;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.wrapper.TorrentSelectorWrapper;
import com.acgist.snail.protocol.Protocol.Type;
import com.acgist.snail.protocol.ProtocolManager;
import com.acgist.snail.repository.impl.TaskRepository;
import com.acgist.snail.system.context.SystemStatistics;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.DateUtils;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * Task Session
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class TaskSession {

	/**
	 * 下载状态
	 */
	public enum Status {
		
		/**
		 * 任务添加到下载队列时处于等待状态
		 */
		AWAIT(		"等待中"),
		/**
		 * 任务下载时的状态：不能直接设置此状态，由下载管理器自动修改。
		 */
		DOWNLOAD(	"下载中"),
		/**
		 * 任务暂停
		 */
		PAUSE(		"暂停"),
		/**
		 * 任务完成：完成状态不能转换为其他任何状态
		 */
		COMPLETE(	"完成"),
		/**
		 * 任务失败
		 */
		FAIL(		"失败");
		
		/**
		 * 状态名称
		 */
		private final String value;
		
		private Status(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
		
	}
	
	/**
	 * 时间格式工厂
	 */
	private static final ThreadLocal<SimpleDateFormat> FORMATER = ThreadLocal.withInitial(() -> {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm");
	});

	/**
	 * 下载器
	 */
	private IDownloader downloader;
	/**
	 * 任务
	 */
	private final TaskEntity entity;
	/**
	 * 统计
	 */
	private final StatisticsSession statistics;
	
	private TaskSession(TaskEntity entity) throws DownloadException {
		if(entity == null) {
			throw new DownloadException("创建TaskSession失败（任务不存在）");
		}
		this.entity = entity;
		this.statistics = new StatisticsSession(true, SystemStatistics.getInstance().getSystemStatistics());
	}
	
	public static final TaskSession newInstance(TaskEntity entity) throws DownloadException {
		return new TaskSession(entity);
	}
	
	public TaskEntity entity() {
		return this.entity;
	}
	
	public String name() {
		return this.entity.getName();
	}
	
	/**
	 * 获取下载器
	 */
	public IDownloader downloader() {
		return this.downloader;
	}

	/**
	 * 设置下载器
	 */
	public void downloader(IDownloader downloader) {
		this.downloader = downloader;
	}

	/**
	 * 删除下载器
	 */
	public void removeDownloader() {
		this.downloader = null;
	}
	
	/**
	 * 获取下载目录
	 */
	public File downloadFolder() {
		final File file = new File(this.entity.getFile());
		if(file.isFile()) {
			return file.getParentFile();
		} else {
			return file;
		}
	}
	
	/**
	 * 获取BT任务已选择的下载文件
	 */
	public List<String> downloadTorrentFiles() {
		if(this.entity.getType() != Type.TORRENT) {
			return List.of();
		}
		final String description = this.entity.getDescription();
		if(StringUtils.isEmpty(description)) {
			return List.of();
		} else {
			final TorrentSelectorWrapper wrapper = TorrentSelectorWrapper.newDecoder(description);
			return wrapper.deserialize();
		}
	}

	/**
	 * 更新状态，刷新下载。
	 */
	public void updateStatus(Status status) {
		if(complete()) {
			return;
		}
		final TaskRepository repository = new TaskRepository();
		if(status == Status.COMPLETE) {
			this.entity.setEndDate(new Date()); // 设置完成时间
		}
		this.entity.setStatus(status);
		repository.update(this.entity);
		DownloaderManager.getInstance().refresh(); // 刷新下载
		GuiHandler.getInstance().refreshTaskStatus(); // 刷新状态
	}
	
	public StatisticsSession statistics() {
		return this.statistics;
	}
	
	/**
	 * 已下载大小
	 */
	public long downloadSize() {
		return this.statistics.downloadSize();
	}
	
	/**
	 * 设置已下载大小
	 */
	public void downloadSize(long size) {
		this.statistics.downloadSize(size);
	}

	/**
	 * 等待状态
	 */
	public boolean await() {
		return this.entity.getStatus() == Status.AWAIT;
	}
	
	/**
	 * 暂停状态
	 */
	public boolean pause() {
		return this.entity.getStatus() == Status.PAUSE;
	}
	
	/**
	 * 下载状态
	 */
	public boolean download() {
		return this.entity.getStatus() == Status.DOWNLOAD;
	}
	
	/**
	 * 完成状态
	 */
	public boolean complete() {
		return this.entity.getStatus() == Status.COMPLETE;
	}
	
	/**
	 * 任务执行状态（在线程池中）：等待中或者下载中
	 */
	public boolean inThreadPool() {
		return await() || download();
	}
	
	/**
	 * 创建下载器，如果已经存在直接返回，否者创建下载器。
	 */
	public IDownloader buildDownloader() throws DownloadException {
		if(this.downloader != null) {
			return this.downloader;
		}
		return ProtocolManager.getInstance().buildDownloader(this);
	}
	
	// JavaFX Table数据绑定 //
	
	/**
	 * 任务名称
	 */
	public String getNameValue() {
		return this.entity.getName();
	}

	/**
	 * 任务状态
	 */
	public String getStatusValue() {
		if(download()) {
			return FileUtils.formatSize(this.statistics.downloadSecond()) + "/S";
		} else {
			return this.entity.getStatus().getValue();
		}
	}
	
	/**
	 * 任务进度
	 */
	public String getProgressValue() {
		if(complete()) {
			return FileUtils.formatSize(this.entity.getSize());
		} else {
			return FileUtils.formatSize(this.statistics.downloadSize()) + "/" + FileUtils.formatSize(this.entity.getSize());
		}
	}

	/**
	 * 创建时间
	 */
	public String getCreateDateValue() {
		if(this.entity.getCreateDate() == null) {
			return "-";
		}
		return FORMATER.get().format(this.entity.getCreateDate());
	}
	
	/**
	 * 完成时间
	 */
	public String getEndDateValue() {
		if(this.entity.getEndDate() == null) {
			if(download()) {
				final long downloadSecond = this.statistics.downloadSecond();
				if(downloadSecond == 0L) {
					return "-";
				} else {
					final long second = (this.entity.getSize() - this.statistics.downloadSize()) / downloadSecond;
					return DateUtils.formatSecond(second);
				}
			} else {
				return "-";
			}
		}
		return FORMATER.get().format(this.entity.getEndDate());
	}
	
}
