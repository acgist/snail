package com.acgist.snail.net;

/**
 * 多文件下载器完成校验
 * 
 * @author acgist
 */
public interface IMultifileCompletedChecker {

	/**
	 * 检测任务是否下载完成
	 * 
	 * @return 是否下载完成
	 */
	boolean checkCompleted();
	
	/**
	 * 检测任务是否下载完成并且解锁
	 * 
	 * 注意：不要在该方法中实现释放资源等非幂等操作（可能会被多次调用）
	 */
	void checkCompletedAndUnlock();
	
}
