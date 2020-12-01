package com.acgist.snail.pojo;

import java.util.Date;

import com.acgist.snail.pojo.ITaskSession.FileType;
import com.acgist.snail.pojo.ITaskSession.Status;
import com.acgist.snail.protocol.Protocol.Type;

/**
 * <p>任务 - 实体接口</p>
 * 
 * @author acgist
 */
public interface ITaskSessionEntity {

	/**
	 * @return 任务ID
	 */
	String getId();
	
	/**
	 * @param id 任务ID
	 */
	void setId(String id);
	
	/**
	 * @return 任务名称
	 */
	String getName();
	
	/**
	 * @param name 任务名称
	 */
	void setName(String name);
	
	/**
	 * @return 协议类型
	 */
	Type getType();
	
	/**
	 * @param type 协议类型
	 */
	void setType(Type type);
	
	/**
	 * @return 文件类型
	 */
	FileType getFileType();
	
	/**
	 * @param fileType 文件类型
	 */
	void setFileType(FileType fileType);
	
	/**
	 * @return 文件路径或目录路径
	 */
	String getFile();
	
	/**
	 * @param file 文件路径或目录路径
	 */
	void setFile(String file);
	
	/**
	 * @return 下载链接
	 */
	String getUrl();
	
	/**
	 * @param url 下载链接
	 */
	void setUrl(String url);
	
	/**
	 * @return BT任务种子文件路径
	 */
	String getTorrent();
	
	/**
	 * @param torrent BT任务种子文件路径
	 */
	void setTorrent(String torrent);
	
	/**
	 * @return 任务状态
	 */
	Status getStatus();
	
	/**
	 * @param status 任务状态
	 */
	void setStatus(Status status);
	
	/**
	 * @return 文件大小（B）
	 */
	Long getSize();
	
	/**
	 * @param size 文件大小（B）
	 */
	void setSize(Long size);
	
	/**
	 * @return 完成时间
	 */
	Date getEndDate();
	
	/**
	 * @param endDate 完成时间
	 */
	void setEndDate(Date endDate);
	
	/**
	 * @return 下载描述
	 */
	String getDescription();
	
	/**
	 * @param description 下载描述
	 */
	void setDescription(String description);
	
}
