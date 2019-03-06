package com.acgist.snail.pojo.wrapper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;

import com.acgist.snail.module.exception.DownloadException;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.entity.TaskEntity.Status;
import com.acgist.snail.pojo.entity.TaskEntity.Type;
import com.acgist.snail.repository.impl.TaskRepository;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.JSONUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * wrapper - 任务
 */
public class TaskWrapper{

	private ThreadLocal<SimpleDateFormat> formater = new ThreadLocal<>() {
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm");
		};
	};

	private TaskEntity entity;

	public TaskWrapper(TaskEntity entity) throws DownloadException {
		if(entity == null) {
			throw new DownloadException("创建下载任务失败");
		}
		this.entity = entity;
	}

	public TaskEntity entity() {
		return entity;
	}

	// 功能 //
	
	/**
	 * 获取下载目录
	 */
	public File downloadFileFolder() {
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
	public List<String> torrentDownloadFiles() {
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
		TaskRepository repository = new TaskRepository();
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
	
	// Table 数据 //
	
	public String getNameValue() {
		return entity.getName();
	}
	
	public String getStatusValue() {
		return entity.getStatus().getValue();
	}
	
	public String getProgressValue() {
		return FileUtils.size(entity.getSize());
	}
	
	public String getCreateDateValue() {
		if(entity.getCreateDate() == null) {
			return "-";
		}
		return formater.get().format(entity.getCreateDate());
	}
	
	public String getEndDateValue() {
		if(entity.getEndDate() == null) {
			return "-";
		}
		return formater.get().format(entity.getEndDate());
	}

}
