package com.acgist.snail.pojo.entity;

import java.util.Date;

import com.acgist.snail.pojo.ITaskSession.FileType;
import com.acgist.snail.pojo.ITaskSession.Status;
import com.acgist.snail.pojo.ITaskSessionEntity;
import com.acgist.snail.protocol.Protocol.Type;

/**
 * Entity - 任务
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class TaskEntity extends BaseEntity implements ITaskSessionEntity {

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
	 * 协议类型
	 */
	private Type type;
	/**
	 * 文件类型
	 */
	private FileType fileType;
	/**
	 * 文件路径或目录路径
	 */
	private String file;
	/**
	 * <p>下载链接：FTP、HTTP、磁力链接、种子任务</p>
	 * <p>需要转换协议的下载链接（例如：迅雷链接）会直接转换为实际下载链接保存</p>
	 */
	private String url;
	/**
	 * <p>种子任务种子文件路径</p>
	 * <p>种子任务下载时默认复制一份种子文件到下载目录</p>
	 */
	private String torrent;
	/**
	 * 任务状态
	 */
	private Status status;
	/**
	 * 文件大小（B）
	 */
	private Long size;
	/**
	 * 完成时间
	 */
	private Date endDate;
	/**
	 * </p>下载描述</p>
	 * </p>种子任务保存选择下载文件列表（B编码）</p>
	 */
	private String description;
	
	public TaskEntity() {
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public Type getType() {
		return type;
	}
	
	@Override
	public void setType(Type type) {
		this.type = type;
	}
	
	@Override
	public FileType getFileType() {
		return fileType;
	}
	
	@Override
	public void setFileType(FileType fileType) {
		this.fileType = fileType;
	}
	
	@Override
	public String getFile() {
		return file;
	}
	
	@Override
	public void setFile(String file) {
		this.file = file;
	}
	
	@Override
	public String getUrl() {
		return url;
	}
	
	@Override
	public void setUrl(String url) {
		this.url = url;
	}
	
	@Override
	public String getTorrent() {
		return torrent;
	}
	
	@Override
	public void setTorrent(String torrent) {
		this.torrent = torrent;
	}
	
	@Override
	public Status getStatus() {
		return status;
	}
	
	@Override
	public void setStatus(Status status) {
		this.status = status;
	}
	
	@Override
	public Long getSize() {
		return size;
	}
	
	@Override
	public void setSize(Long size) {
		this.size = size;
	}
	
	@Override
	public Date getEndDate() {
		return endDate;
	}
	
	@Override
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	@Override
	public String getDescription() {
		return description;
	}
	
	@Override
	public void setDescription(String description) {
		this.description = description;
	}

}
