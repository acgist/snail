package com.acgist.snail.pojo.session;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.downloader.ed2k.Ed2kDownloader;
import com.acgist.snail.downloader.ftp.FtpDownloader;
import com.acgist.snail.downloader.http.HttpDownloader;
import com.acgist.snail.downloader.torrent.TorrentDownloader;
import com.acgist.snail.gui.main.TaskDisplay;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.entity.TaskEntity.Status;
import com.acgist.snail.pojo.entity.TaskEntity.Type;
import com.acgist.snail.repository.impl.TaskRepository;
import com.acgist.snail.system.context.SystemStatistical;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.DateUtils;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.JsonUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * session - 任务<br>
 * 下载任务信息统计
 */
public class TaskSession {

	private ThreadLocal<SimpleDateFormat> formater = new ThreadLocal<>() {
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm");
		};
	};
	
	private TaskEntity entity;
	
	private long lastTime = System.currentTimeMillis(); // 最后一次统计时间
	private long bufferSecond = 0L; // 每秒下载速度
	private AtomicLong downloadSize = new AtomicLong(0); // 已经下载大小
	private AtomicLong downloadBuffer = new AtomicLong(0); // 下载速度采样

	private TaskSession(TaskEntity entity) throws DownloadException {
		if(entity == null) {
			throw new DownloadException("创建下载任务失败");
		}
		this.entity = entity;
	}
	
	// 功能 //

	public static final TaskSession newInstance(TaskEntity entity) throws DownloadException {
		return new TaskSession(entity);
	}
	
	public TaskEntity entity() {
		return entity;
	}
	
	/**
	 * 获取下载目录
	 */
	public File downloadFolder() {
		File file = new File(entity.getFile());
		if(entity.getType() == Type.torrent) {
			return file;
		} else {
			return file.getParentFile();
		}
	}
	
	/**
	 * 获取已选择的下载文件
	 */
	public List<String> downloadTorrentFiles() {
		if(entity.getType() != Type.torrent) {
			return List.of();
		}
		String description = entity.getDescription();
		if(StringUtils.isEmpty(description)) {
			return List.of();
		} else {
			return JsonUtils.toList(description, String.class);
		}
	}

	/**
	 * 更新状态
	 */
	public void updateStatus(Status status) {
		if(complete()) {
			return;
		}
		TaskRepository repository = new TaskRepository();
		if(status == Status.complete) {
			this.entity.setEndDate(new Date()); // 设置完成时间
		}
		this.entity.setStatus(status);
		repository.update(this.entity);
		TaskDisplay.getInstance().refreshTaskData(); // 刷新状态
	}
	
	/**
	 * 删除数据
	 */
	public void delete() {
		TaskRepository repository = new TaskRepository();
		repository.delete(entity.getId());
	}
	
	/**
	 * 下载统计
	 */
	public void statistical(long buffer) {
		downloadSize.addAndGet(buffer);
		downloadBuffer.addAndGet(buffer);
		long now = System.currentTimeMillis();
		long interval = now - lastTime;
		if(interval > TaskDisplay.REFRESH_TIME.toMillis()) {
			long oldBuffer = downloadBuffer.getAndSet(0);
			bufferSecond = oldBuffer * 1000 / interval;
			lastTime = now;
		}
		SystemStatistical.getInstance().statistical(buffer);
	}
	
	/**
	 * 累计下载大小
	 */
	public long downloadSize() {
		return downloadSize.get();
	}
	
	/**
	 * 设置累计下载大小
	 */
	public void downloadSize(long size) {
		downloadSize.set(size);
	}
	
	/**
	 * 获取已下载大小
	 */
	public void loadDownloadSize() {
		if(entity.getType() == Type.http) {
			long size = FileUtils.fileSize(entity.getFile());
			downloadSize(size);
		}
	}
	
	/**
	 * 等待状态
	 */
	public boolean await() {
		return entity.getStatus() == Status.await;
	}
	
	/**
	 * 下载状态
	 */
	public boolean download() {
		return entity.getStatus() == Status.download;
	}
	
	/**
	 * 完成状态
	 */
	public boolean complete() {
		return entity.getStatus() == Status.complete;
	}
	
	/**
	 * 任务执行状态：等待中或者下载中
	 */
	public boolean run() {
		return await() || download();
	}
	
	/**
	 * 获取下载任务
	 */
	public IDownloader downloader() throws DownloadException {
		var type = this.entity().getType();
		switch (type) {
			case ftp:
				return FtpDownloader.newInstance(this);
			case ed2k:
				return Ed2kDownloader.newInstance(this);
			case http:
				return HttpDownloader.newInstance(this);
			case torrent:
				return TorrentDownloader.newInstance(this);
		}
		throw new DownloadException("不支持的下载类型：" + type);
	}
	
	// Table数据绑定 //
	
	/**
	 * 任务名称
	 */
	public String getNameValue() {
		return entity.getName();
	}

	/**
	 * 任务状态
	 */
	public String getStatusValue() {
		if(download()) {
			return FileUtils.formatSize(bufferSecond) + "/S";
		} else {
			return entity.getStatus().getValue();
		}
	}
	
	/**
	 * 任务进度
	 */
	public String getProgressValue() {
		if(complete()) {
			return FileUtils.formatSize(entity.getSize());
		} else {
			return FileUtils.formatSize(downloadSize.longValue()) + "/" + FileUtils.formatSize(entity.getSize());
		}
	}

	/**
	 * 创建时间
	 */
	public String getCreateDateValue() {
		if(entity.getCreateDate() == null) {
			return "-";
		}
		return formater.get().format(entity.getCreateDate());
	}
	
	/**
	 * 完成时间
	 */
	public String getEndDateValue() {
		if(entity.getEndDate() == null) {
			if(download()) {
				if(bufferSecond == 0L) {
					return "-";
				} else {
					long second = (entity.getSize() - downloadSize.longValue()) / bufferSecond;
					return DateUtils.formatSecond(second);
				}
			} else {
				return "-";
			}
		}
		return formater.get().format(entity.getEndDate());
	}
	
	
	
}
