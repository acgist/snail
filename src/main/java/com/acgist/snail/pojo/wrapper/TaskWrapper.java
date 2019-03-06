package com.acgist.snail.pojo.wrapper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.acgist.snail.context.SystemStatistical;
import com.acgist.snail.module.exception.DownloadException;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.entity.TaskEntity.Status;
import com.acgist.snail.pojo.entity.TaskEntity.Type;
import com.acgist.snail.repository.impl.TaskRepository;
import com.acgist.snail.utils.DateUtils;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.JSONUtils;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.window.main.TaskTimer;

/**
 * wrapper - 任务
 */
public class TaskWrapper {

	private ThreadLocal<SimpleDateFormat> formater = new ThreadLocal<>() {
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm");
		};
	};
	
	private TaskEntity entity;
	
	private Long sizeSecond = 0L; // 每秒下载速度
	private AtomicLong downloadSize = new AtomicLong(0); // 已经下载大小
	private AtomicLong downloadBuffer = new AtomicLong(0); // 下载速度采样

	public TaskWrapper(TaskEntity entity) throws DownloadException {
		if(entity == null) {
			throw new DownloadException("创建下载任务失败");
		}
		this.entity = entity;
	}
	
	// 功能 //

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
	 * TODO：泛型
	 */
	@SuppressWarnings("unchecked")
	public List<String> downloadTorrentFiles() {
		if(entity.getType() != Type.torrent) {
			return List.of();
		}
		String description = entity.getDescription();
		if(StringUtils.isEmpty(description)) {
			return List.of();
		} else {
			return JSONUtils.jsonToJava(description, List.class);
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
	public void statistical(long size) {
		downloadSize.addAndGet(size);
		downloadBuffer.addAndGet(size);
		SystemStatistical.statistical(size);
	}
	
	/**
	 * 累计下载大小
	 */
	public long downloadSize() {
		return downloadSize.get();
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
	
	// Table 数据 //
	
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
			long size = downloadBuffer.getAndSet(0);
			sizeSecond = size / TaskTimer.REFRESH_TIME;
			if(sizeSecond == 0L) {
				return Status.download.getValue();
			} else {
				return FileUtils.formatSize(sizeSecond) + "/S";
			}
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
				if(sizeSecond == 0L) {
					return "-";
				} else {
					long second = (entity.getSize() - downloadSize.longValue()) / sizeSecond;
					return DateUtils.formatSecond(second);
				}
			} else {
				return "-";
			}
		}
		return formater.get().format(entity.getEndDate());
	}

}
