package com.acgist.snail.context;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.net.DownloadException;

/**
 * <p>任务信息接口</p>
 * 
 * @author acgist
 */
public interface ITaskSession extends ITaskSessionStatus, ITaskSessionEntity, ITaskSessionHandler, ITaskSessionTable, IStatisticsGetter {

	/**
	 * <p>文件类型</p>
	 * 
	 * @author acgist
	 */
	public enum FileType {
		
		/**
		 * <p>图片</p>
		 */
		IMAGE("图片"),
		/**
		 * <p>视频</p>
		 */
		VIDEO("视频"),
		/**
		 * <p>音频</p>
		 */
		AUDIO("音频"),
		/**
		 * <p>脚本</p>
		 */
		SCRIPT("脚本"),
		/**
		 * <p>BT</p>
		 */
		TORRENT("BT"),
		/**
		 * <p>压缩</p>
		 */
		COMPRESS("压缩"),
		/**
		 * <p>文档</p>
		 */
		DOCUMENT("文档"),
		/**
		 * <p>安装</p>
		 */
		INSTALL("安装"),
		/**
		 * <p>未知</p>
		 */
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
	 * <p>获取下载器</p>
	 * 
	 * @return 下载器
	 */
	IDownloader downloader();
	
	/**
	 * <p>新建下载器</p>
	 * 
	 * @return 下载器
	 * 
	 * @throws DownloadException 下载异常
	 */
	IDownloader buildDownloader() throws DownloadException;
	
	/**
	 * <p>获取下载文件</p>
	 * 
	 * @return 下载文件
	 */
	File downloadFile();
	
	/**
	 * <p>获取下载目录</p>
	 * 
	 * @return 下载目录
	 */
	File downloadFolder();
	
	/**
	 * <p>获取选择下载文件列表</p>
	 * <p>注意：多文件下载任务</p>
	 * 
	 * @return 选择下载文件列表
	 */
	List<String> multifileSelected();

	/**
	 * <p>设置已经下载大小</p>
	 * 
	 * @param size 已经下载大小
	 */
	void downloadSize(long size);
	
	/**
	 * <p>更新任务大小</p>
	 */
	void buildDownloadSize();

	/**
	 * <p>获取任务信息</p>
	 * 
	 * @return 任务信息
	 */
	Map<String, Object> taskMessage();
	
}
