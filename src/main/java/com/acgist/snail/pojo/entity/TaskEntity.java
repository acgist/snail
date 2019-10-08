package com.acgist.snail.pojo.entity;

import java.util.Date;

import com.acgist.snail.pojo.session.TaskSession.Status;
import com.acgist.snail.protocol.Protocol.Type;
import com.acgist.snail.utils.FileUtils.FileType;

/**
 * Entity - 任务
 * 
 * @author acgist
 * @since 1.0.0
 */
public class TaskEntity extends BaseEntity {

	private static final long serialVersionUID = 1L;

	/**
	 * 任务表名
	 */
	public static final String TABLE_NAME = "tb_task";
	/**
	 * 任务名称
	 */
	public static final String PROPERTY_NAME = "name";
	
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
	 * <p>下载地址：FTP、HTTP、磁力链接。</p>
	 * <p>迅雷下载链接直接转换为实际地址保存。</p>
	 */
	private String url;
	/**
	 * <p>BT任务种子文件路径。</p>
	 * <p>种子文件下载时默认复制一份保存到下载目录。</p>
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
	 * </p>描述：BT任务保存选择的下载文件</p>
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
