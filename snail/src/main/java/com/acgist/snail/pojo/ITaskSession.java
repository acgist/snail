package com.acgist.snail.pojo;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.downloader.IDownloader;

/**
 * <p>任务信息</p>
 * 
 * @author acgist
 */
public interface ITaskSession extends ITaskStatus, ITaskSessionEntity, ITaskSessionHandler, ITaskSessionTable, IStatisticsSessionGetter {

	/**
	 * <p>文件类型</p>
	 * 
	 * @author acgist
	 */
	public enum FileType {
		
		/** 图片 */
		IMAGE("图片"),
		/** 视频 */
		VIDEO("视频"),
		/** 音频 */
		AUDIO("音频"),
		/** 脚本 */
		SCRIPT("脚本"),
		/** BT */
		TORRENT("BT"),
		/** 压缩 */
		COMPRESS("压缩"),
		/** 文档 */
		DOCUMENT("文档"),
		/** 安装包 */
		INSTALL("安装包"),
		/** 未知 */
		UNKNOWN("未知");
		
		/**
		 * <p>类型名称</p>
		 */
		private final String value;

		/**
		 * @param value 类型名称
		 */
		private FileType(String value) {
			this.value = value;
		}

		/**
		 * <p>获取类型名称</p>
		 * 
		 * @return 类型名称
		 */
		public String getValue() {
			return value;
		}

	}
	
	/**
	 * <p>创建下载器</p>
	 * <p>如果已经存在下载器直接返回，否者创建下载器。</p>
	 * 
	 * @return 下载器
	 * 
	 * @throws DownloadException 下载异常
	 */
	IDownloader buildDownloader() throws DownloadException;
	
	/**
	 * <p>获取下载目录</p>
	 * 
	 * @return 下载目录
	 */
	File downloadFolder();
	
	/**
	 * <p>获取多文件下载任务选择下载文件列表</p>
	 * 
	 * @return 多文件下载任务选择下载文件列表
	 */
	List<String> multifileSelected();

	/**
	 * <p>获取已下载大小</p>
	 * 
	 * @return 已下载大小
	 */
	long downloadSize();
	
	/**
	 * <p>设置已下载大小</p>
	 * 
	 * @param size 已下载大小
	 */
	void downloadSize(long size);

	/**
	 * <p>获取任务信息（Map）</p>
	 * 
	 * @return 任务信息
	 */
	Map<String, Object> taskMessage();
	
}
