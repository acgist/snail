package com.acgist.snail.pojo.entity;

import java.util.Date;

import com.acgist.snail.system.config.FileTypeConfig.FileType;

/**
 * Entity - 任务
 * 
 * @author acgist
 * @since 1.0.0
 */
public class TaskEntity extends BaseEntity {

	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "tb_task";
	
	public static final String PROPERTY_NAME = "name"; // 任务名称

	/**
	 * 下载状态
	 */
	public enum Status {
		
		/**
		 * 任务添加进入下载前的状态
		 */
		await("等待中"),
		/**
		 * 任务下载时的状态，不能直接设置为此状态，有下载管理器自动修改为下载中
		 */
		download("下载中"),
		/**
		 * 任务暂停
		 */
		pause("暂停"),
		/**
		 * 任务完成，完成状态不能转换为其他任何状态
		 */
		complete("完成"),
		/**
		 * 任务失败
		 */
		fail("失败");
		
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
		magnet,
		thunder,
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
	 * 下载地址：FTP、HTTP、磁力链接。<br>
	 * 迅雷下载链接直接转换为实际地址保存。
	 */
	private String url;
	/**
	 * BT任务种子文件路径<br>
	 * 种子文件下载时默认复制一份保存到下载目录
	 */
	private String torrent;
	/**
	 * 任务状态
	 */
	private Status status;
	/**
	 * 大小（B）
	 */
	private Long size;
	/**
	 * 完成时间
	 */
	private Date endDate;
	/**
	 * 描述：<br>
	 * BT下载保存选择的下载文件
	 */
	private String description;
	
	public TaskEntity() {
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

	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
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
