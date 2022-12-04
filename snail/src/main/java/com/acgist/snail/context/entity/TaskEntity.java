package com.acgist.snail.context.entity;

import java.util.Date;
import java.util.Objects;

import com.acgist.snail.context.ITaskSession.FileType;
import com.acgist.snail.context.ITaskSessionEntity;
import com.acgist.snail.context.ITaskSessionStatus.Status;
import com.acgist.snail.protocol.Protocol.Type;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>Entity - 任务</p>
 * 
 * @author acgist
 */
public final class TaskEntity extends Entity implements ITaskSessionEntity {

	private static final long serialVersionUID = 1L;

	/**
	 * <p>任务名称</p>
	 */
	private String name;
	/**
	 * <p>协议类型</p>
	 */
	private Type type;
	/**
	 * <p>文件类型</p>
	 */
	private FileType fileType;
	/**
	 * <p>文件路径或目录路径</p>
	 * <p>单文件下载：文件路径</p>
	 * <p>多文件下载：目录路径</p>
	 */
	private String file;
	/**
	 * <p>下载链接</p>
	 * <p>种子文件：保存磁力链接</p>
	 * <p>迅雷链接：保存实际下载链接</p>
	 */
	private String url;
	/**
	 * <p>BT任务种子文件路径</p>
	 */
	private String torrent;
	/**
	 * <p>任务状态</p>
	 */
	private Status status;
	/**
	 * <p>文件大小（B）</p>
	 */
	private Long size;
	/**
	 * <p>完成时间</p>
	 */
	private Date completedDate;
	/**
	 * <p>下载描述</p>
	 * <p>多文件下载：保持下载文件列表（B编码）</p>
	 * <p>BT任务：下载文件路径列表</p>
	 * <p>HLS任务：下载文件链接列表</p>
	 */
	private String description;
	/**
	 * <p>任务负载</p>
	 * <p>BT任务：已经下载Piece位图</p>
	 */
	private byte[] payload;
	
	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public Type getType() {
		return this.type;
	}
	
	@Override
	public void setType(Type type) {
		this.type = type;
	}
	
	@Override
	public FileType getFileType() {
		return this.fileType;
	}
	
	@Override
	public void setFileType(FileType fileType) {
		this.fileType = fileType;
	}
	
	@Override
	public String getFile() {
		return this.file;
	}
	
	@Override
	public void setFile(String file) {
		this.file = file;
	}
	
	@Override
	public String getUrl() {
		return this.url;
	}
	
	@Override
	public void setUrl(String url) {
		this.url = url;
	}
	
	@Override
	public String getTorrent() {
		return this.torrent;
	}
	
	@Override
	public void setTorrent(String torrent) {
		this.torrent = torrent;
	}
	
	@Override
	public Status getStatus() {
		return this.status;
	}
	
	@Override
	public void setStatus(Status status) {
		this.status = status;
	}
	
	@Override
	public Long getSize() {
		return this.size;
	}
	
	@Override
	public void setSize(Long size) {
		this.size = size;
	}
	
	@Override
	public Date getCompletedDate() {
		return this.completedDate;
	}
	
	@Override
	public void setCompletedDate(Date completedDate) {
		this.completedDate = completedDate;
	}
	
	@Override
	public String getDescription() {
		return this.description;
	}
	
	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public byte[] getPayload() {
		return this.payload;
	}

	@Override
	public void setPayload(byte[] payload) {
		this.payload = payload;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.id);
	}
	
	@Override
	public boolean equals(Object object) {
		if(this == object) {
			return true;
		}
		if(object instanceof TaskEntity entity) {
			return StringUtils.equals(this.id, entity.id);
		}
		return false;
	}
	
}
