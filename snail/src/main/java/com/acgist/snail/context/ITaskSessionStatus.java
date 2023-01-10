package com.acgist.snail.context;

/**
 * 任务信息状态接口
 * 
 * @author acgist
 */
public interface ITaskSessionStatus {

	/**
	 * 任务状态
	 * 任务添加时应该设置状态为等待中，等待系统分配下载资源，而不是立即设置状态为下载中。
	 * 
	 * @author acgist
	 */
	public enum Status {
		
		/**
		 * 等待中任务
		 * 任务添加到下载队列还没有开始下载
		 */
		AWAIT("等待中"),
		/**
		 * 下载中任务
		 * 任务已经分配资源开始下载
		 */
		DOWNLOAD("下载中"),
		/**
		 * 暂停任务
		 */
		PAUSE("暂停"),
		/**
		 * 完成任务
		 */
		COMPLETED("完成"),
		/**
		 * 失败任务
		 */
		FAIL("失败"),
		/**
		 * 删除任务
		 */
		DELETE("删除");
		
		/**
		 * 状态名称
		 */
		private final String value;
		
		/**
		 * @param value 状态名称
		 */
		private Status(String value) {
			this.value = value;
		}

		/**
		 * @return 状态名称
		 */
		public String getValue() {
			return value;
		}
		
	}
	
	/**
	 * @return 是否处于等待状态
	 */
	boolean statusAwait();
	
	/**
	 * @return 是否处于下载状态
	 */
	boolean statusDownload();
	
	/**
	 * @return 是否处于暂停状态
	 */
	boolean statusPause();
	
	/**
	 * @return 是否处于完成状态
	 */
	boolean statusCompleted();
	
	/**
	 * @return 是否处于失败状态
	 */
	boolean statusFail();
	
	/**
	 * @return 是否处于删除状态
	 */
	boolean statusDelete();
	
	/**
	 * @return 是否处于执行状态
	 * 
	 * @see #statusAwait()
	 * @see #statusDownload()
	 */
	boolean statusRunning();
	
}
