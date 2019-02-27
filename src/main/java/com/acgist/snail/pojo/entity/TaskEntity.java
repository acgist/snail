package com.acgist.snail.pojo.entity;

import java.util.Date;

import com.acgist.snail.module.config.FileTypeConfig.FileType;

/**
 * entity - 任务
 */
public class TaskEntity extends BaseEntity {

	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "tb_task";
	
	public static final String PROPERTY_NAME = "name"; // 任务名称

	/**
	 * 下载状态
	 */
	public enum Status {
		
		await("等待中"),
		download("下载中"),
		pause("暂停"),
		complete("完成");
		
		private String value;

		private Status(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
		
	}
	
	/**
	 * 下载类型
	 */
	public enum Type {
		
		ftp,
		http,
		ed2k,
		torrent;
		
	}
	
	/**
	 * 任务名称
	 */
	private String name;
	/**
	 * 下载类型
	 */
	private Type type;
	/**
	 * 文件类型
	 */
	private FileType fileType;
	/**
	 * 下载文件路径
	 */
	private String file;
	/**
	 * 下载地址：FTP、HTTP、磁力链接
	 */
	private String url;
	/**
	 * 种子文件路径
	 */
	private String torrent;
	/**
	 * 状态
	 */
	private Status status;
	/**
	 * 大小（KB）
	 */
	private Integer size;
	/**
	 * 完成时间
	 */
	private Date endDate;
	/**
	 * 描述：下载其他信息
	 */
	private String description;
	
	public TaskEntity() {
	}
	
	public TaskEntity(String name, Type type, FileType fileType, String file, String url, String torrent, Status status, Integer size, Date endDate, String description) {
		this.name = name;
		this.type = type;
		this.fileType = fileType;
		this.file = file;
		this.url = url;
		this.torrent = torrent;
		this.status = status;
		this.size = size;
		this.endDate = endDate;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public FileType getFileType() {
		return fileType;
	}

	public void setFileType(FileType fileType) {
		this.fileType = fileType;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTorrent() {
		return torrent;
	}

	public void setTorrent(String torrent) {
		this.torrent = torrent;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
