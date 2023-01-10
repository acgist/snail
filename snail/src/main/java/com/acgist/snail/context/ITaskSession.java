package com.acgist.snail.context;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.net.DownloadException;

/**
 * 任务信息接口
 * 
 * @author acgist
 */
public interface ITaskSession extends ITaskSessionStatus, ITaskSessionEntity, ITaskSessionHandler, ITaskSessionTable, IStatisticsGetter {

	/**
	 * 文件类型
	 * 
	 * @author acgist
	 */
	public enum FileType {
		
		/**
		 * 音频
		 */
		AUDIO("音频"),
		/**
		 * 图片
		 */
		IMAGE("图片"),
		/**
		 * 视频
		 */
		VIDEO("视频"),
		/**
		 * 脚本
		 */
		SCRIPT("脚本"),
		/**
		 * 安装
		 */
		INSTALL("安装"),
		/**
		 * BT
		 */
		TORRENT("BT"),
		/**
		 * 压缩
		 */
		COMPRESS("压缩"),
		/**
		 * 文档
		 */
		DOCUMENT("文档"),
		/**
		 * 未知
		 */
		UNKNOWN("未知");
		
		/**
		 * 类型名称
		 */
		private final String value;

		/**
		 * @param value 类型名称
		 */
		private FileType(String value) {
			this.value = value;
		}

		/**
		 * @return 类型名称
		 */
		public String getValue() {
			return value;
		}

	}
	
	/**
	 * @return 下载器
	 */
	IDownloader downloader();
	
	/**
	 * 新建下载器
	 * 
	 * @return 下载器
	 * 
	 * @throws DownloadException 下载异常
	 */
	IDownloader buildDownloader() throws DownloadException;
	
	/**
	 * @return 下载文件
	 */
	File downloadFile();
	
	/**
	 * @return 下载目录
	 */
	File downloadFolder();
	
	/**
	 * 注意：多文件下载任务配置选择下载文件
	 * 
	 * @return 选择下载文件列表
	 */
	List<String> multifileSelected();

	/**
	 * 设置已经下载大小
	 * 
	 * @param size 已经下载大小
	 */
	void downloadSize(long size);
	
	/**
	 * 更新任务大小
	 */
	void buildDownloadSize();

	/**
	 * @return 任务信息
	 */
	Map<String, Object> taskMessage();
	
}
