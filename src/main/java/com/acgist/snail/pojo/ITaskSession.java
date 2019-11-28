package com.acgist.snail.pojo;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.system.exception.DownloadException;

/**
 * <p>任务接口</p>
 * 
 * @author acgist
 * @since 1.2.0
 */
public interface ITaskSession extends ITaskSessionTable, ITaskSessionEntity, ITaskSessionRepository {

	/**
	 * 任务状态
	 */
	public enum Status {
		
		/**
		 * 任务添加到下载队列时处于等待状态
		 */
		AWAIT(		"等待中"),
		/**
		 * 任务下载时的状态：由下载管理器自动修改（不能直接设置此状态）
		 */
		DOWNLOAD(	"下载中"),
		/**
		 * 任务暂停
		 */
		PAUSE(		"暂停"),
		/**
		 * 任务完成：完成状态不能转换为其他任何状态
		 */
		COMPLETE(	"完成"),
		/**
		 * 任务失败
		 */
		FAIL(		"失败");
		
		/**
		 * 状态名称
		 */
		private final String value;
		
		private Status(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
		
	}
	
	/**
	 * 文件类型
	 */
	public enum FileType {
		
		/** 图片 */
		IMAGE(		"图片", "image.png"),
		/** 视频 */
		VIDEO(		"视频", "video.png"),
		/** 音频 */
		AUDIO(		"音频", "audio.png"),
		/** 脚本 */
		SCRIPT(		"脚本", "script.png"),
		/** BT */
		TORRENT(	"BT", "torrent.png"),
		/** 压缩 */
		COMPRESS(	"压缩", "compress.png"),
		/** 文档 */
		DOCUMENT(	"文档", "document.png"),
		/** 安装包 */
		INSTALL(	"安装包", "install.png"),
		/** 未知 */
		UNKNOWN(	"未知", "unknown.png");
		
		/**
		 * 类型名称
		 */
		private final String value;
		/**
		 * 类型图标
		 */
		private final String icon;

		private FileType(String value, String icon) {
			this.value = value;
			this.icon = icon;
		}

		public String value() {
			return value;
		}

		public String icon() {
			return icon;
		}

	}
	
	/**
	 * @return 下载器
	 */
	IDownloader downloader();

	/**
	 * <p>删除下载器</p>
	 */
	void removeDownloader();
	
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
	 * @return 下载目录
	 */
	File downloadFolder();
	
	/**
	 * @return BT任务已选择的下载文件列表
	 */
	List<String> selectTorrentFiles();

	/**
	 * @return 统计信息
	 */
	IStatisticsSession statistics();
	
	/**
	 * @return 已下载大小
	 */
	long downloadSize();
	
	/**
	 * @param size 已下载大小
	 */
	void downloadSize(long size);

	/**
	 * @return 等待状态
	 */
	boolean await();
	
	/**
	 * @return 暂停状态
	 */
	boolean pause();
	
	/**
	 * @return 下载状态
	 */
	boolean download();
	
	/**
	 * @return 完成状态
	 */
	boolean complete();
	
	/**
	 * @return 任务执行状态（在线程池中）：等待中、下载中
	 */
	boolean inThreadPool();
	
	/**
	 * <p>将任务信息转换为Map对象</p>
	 * 
	 * @return 任务信息
	 */
	Map<String, Object> taskMessage();
	
}
