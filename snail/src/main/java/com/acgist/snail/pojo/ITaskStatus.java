package com.acgist.snail.pojo;

/**
 * <p>任务状态</p>
 * 
 * @author acgist
 */
public interface ITaskStatus {

	/**
	 * <p>任务状态</p>
	 * 
	 * @author acgist
	 */
	public enum Status {
		
		/**
		 * <p>任务添加到下载队列时处于等待状态</p>
		 */
		AWAIT("等待中"),
		/**
		 * <p>任务下载时的状态：由下载管理器自动修改（不能直接设置此状态）</p>
		 */
		DOWNLOAD("下载中"),
		/**
		 * <p>任务暂停</p>
		 */
		PAUSE("暂停"),
		/**
		 * <p>任务完成：完成状态不能转换为其他任何状态</p>
		 */
		COMPLETED("完成"),
		/**
		 * <p>任务失败</p>
		 */
		FAIL("失败"),
		/**
		 * <p>删除任务</p>
		 */
		DELETE("删除");
		
		/**
		 * <p>状态名称</p>
		 */
		private final String value;
		
		/**
		 * @param value 状态名称
		 */
		private Status(String value) {
			this.value = value;
		}

		/**
		 * <p>获取状态名称</p>
		 * 
		 * @return 状态名称
		 */
		public String getValue() {
			return value;
		}
		
	}
	
	/**
	 * <p>判断任务是否处于等待状态</p>
	 * 
	 * @return 是否处于等待状态
	 */
	boolean statusAwait();
	
	/**
	 * <p>判断任务是否处于暂停状态</p>
	 * 
	 * @return 是否处于暂停状态
	 */
	boolean statusPause();
	
	/**
	 * <p>判断任务是否处于下载状态</p>
	 * 
	 * @return 是否处于下载状态
	 */
	boolean statusDownload();
	
	/**
	 * <p>判断任务是否处于完成状态</p>
	 * 
	 * @return 是否处于完成状态
	 */
	boolean statusCompleted();
	
	/**
	 * <p>判断任务是否处于失败状态</p>
	 * 
	 * @return 是否处于失败状态
	 */
	boolean statusFail();
	
	/**
	 * <p>判断任务是否处于删除状态</p>
	 * 
	 * @return 是否处于删除状态
	 */
	boolean statusDelete();
	
	/**
	 * <p>判断任务是否处于执行状态</p>
	 * <p>执行状态（在线程池中）：等待中、下载中</p>
	 * 
	 * @return 是否处于执行状态
	 * 
	 * @see #statusAwait()
	 * @see #statusDownload()
	 */
	boolean statusRunning();
	
}
