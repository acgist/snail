package com.acgist.snail.pojo.entity;

import java.util.Date;

import com.acgist.snail.pojo.ITaskSession.FileType;
import com.acgist.snail.pojo.ITaskSessionEntity;
import com.acgist.snail.pojo.ITaskStatus.Status;
import com.acgist.snail.protocol.Protocol.Type;

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
	 */
	private String file;
	/**
	 * <p>下载链接：FTP、HTTP、磁力链接、BT任务</p>
	 * <p>需要转换协议的下载链接（例如：迅雷链接）会直接转换为实际下载链接保存</p>
	 */
	private String url;
	/**
	 * <p>BT任务种子文件路径</p>
	 * <p>BT任务下载时默认复制一份种子文件到下载目录</p>
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
	private Date endDate;
	/**
	 * <p>下载描述</p>
	 * <p>多文件下载时保持下载文件列表（B编码）</p>
	 * <p>BT任务文件列表</p>
	 * <p>HLS任务文件链接</p>
	 */
	private String description;
	/**
	 * <p>任务负载</p>
	 * <p>BT任务已下载Piece位图</p>
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
	public Date getEndDate() {
		return this.endDate;
	}
	
	@Override
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
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
	
}
