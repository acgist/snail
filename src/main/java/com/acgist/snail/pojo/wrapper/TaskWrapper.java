package com.acgist.snail.pojo.wrapper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.acgist.snail.module.config.FileTypeConfig.FileType;
import com.acgist.snail.pojo.entity.TaskEntity;
import com.acgist.snail.pojo.entity.TaskEntity.Status;
import com.acgist.snail.pojo.entity.TaskEntity.Type;
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

	public TaskWrapper(TaskEntity entity) {
		this.entity = entity;
	}

	public TaskEntity getEntity() {
		return entity;
	}

	public void setEntity(TaskEntity entity) {
		this.entity = entity;
	}
	
	// 功能 //
	
	/**
	 * 获取下载目录
	 */
	public File getFileFolder() {
		File file = new File(this.getFile());
		if(this.getType() == Type.torrent) {
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
	public List<String> files() {
		if(this.getType() != Type.torrent) {
			return List.of();
		}
		if(StringUtils.isEmpty(this.getDescription())) {
			return List.of();
		} else {
			return JSONUtils.jsonToJava(this.getDescription(), List.class);
		}
	}
	
	// Table 数据 //
	
	public String getNameValue() {
		return getName();
	}
	
	public String getStatusValue() {
		return getStatus().getValue();
	}
	
	public String getProgressValue() {
		return FileUtils.size(getSize());
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
	
	// GETTER SETTER 代理 //
	
	public String getId() {
		return entity.getId();
	}

	public void setId(String id) {
		entity.setId(id);
	}

	public Date getCreateDate() {
		return entity.getCreateDate();
	}

	public void setCreateDate(Date createDate) {
		entity.setCreateDate(createDate);
	}

	public Date getModifyDate() {
		return entity.getModifyDate();
	}

	public void setModifyDate(Date modifyDate) {
		entity.setModifyDate(modifyDate);
	}
	
	public String getName() {
		return entity.getName();
	}

	public void setName(String name) {
		entity.setName(name);
	}

	public Type getType() {
		return entity.getType();
	}

	public void setType(Type type) {
		entity.setType(type);
	}

	public FileType getFileType() {
		return entity.getFileType();
	}

	public void setFileType(FileType fileType) {
		entity.setFileType(fileType);
	}

	public String getFile() {
		return entity.getFile();
	}

	public void setFile(String file) {
		entity.setFile(file);
	}

	public String getUrl() {
		return entity.getUrl();
	}

	public void setUrl(String url) {
		entity.setUrl(url);
	}

	public String getTorrent() {
		return entity.getTorrent();
	}

	public void setTorrent(String torrent) {
		entity.setTorrent(torrent);
	}

	public Status getStatus() {
		return entity.getStatus();
	}

	public void setStatus(Status status) {
		entity.setStatus(status);
	}

	public Long getSize() {
		return entity.getSize();
	}

	public void setSize(Long size) {
		entity.setSize(size);
	}

	public Date getEndDate() {
		return entity.getEndDate();
	}

	public void setEndDate(Date endDate) {
		entity.setEndDate(endDate);
	}
	
	public String getDescription() {
		return entity.getDescription();
	}

	public void setDescription(String description) {
		entity.setDescription(description);
	}
	
}
