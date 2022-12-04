package com.acgist.snail.context;

/**
 * <p>任务信息状态接口</p>
 * 
 * @author acgist
 */
public interface ITaskSessionStatus {

	/**
	 * <p>任务状态</p>
	 * 
	 * @author acgist
	 */
	public enum Status {
		
		/**
		 * <p>等待中任务：任务添加到下载队列还没有开始下载</p>
		 */
		AWAIT("等待中"),
		/**
		 * <p>下载中任务：任务开始下载</p>
		 * <p>注意：添加任务不要设置该状态</p>
		 */
		DOWNLOAD("下载中"),
		/**
		 * <p>暂停任务</p>
		 */
		PAUSE("暂停"),
		/**
		 * <p>完成任务</p>
		 */
		COMPLETED("完成"),
		/**
		 * <p>失败任务</p>
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
	 * <p>判断任务是否处于下载状态</p>
	 * 
	 * @return 是否处于下载状态
	 */
	boolean statusDownload();
	
	/**
	 * <p>判断任务是否处于暂停状态</p>
	 * 
	 * @return 是否处于暂停状态
	 */
	boolean statusPause();
	
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
	 * 
	 * @return 是否处于执行状态
	 * 
	 * @see #statusAwait()
	 * @see #statusDownload()
	 */
	boolean statusRunning();
	
}
