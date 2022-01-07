package com.acgist.snail.net.torrent.utp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import com.acgist.snail.context.SystemThreadContext;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;

/**
 * <p>UTP请求队列</p>
 * <p>请求队列用来异步处理UTP请求，每个接收窗口对应一个请求队列，每个请求队列可以处理多个接收窗口。</p>
 * 
 * @author acgist
 */
public final class UtpRequestQueue {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UtpRequestQueue.class);

	private static final UtpRequestQueue INSTANCE = new UtpRequestQueue();
	
	public static final UtpRequestQueue getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>请求队列数量：{@value}</p>
	 */
	private static final int QUEUE_SIZE = 4;
	
	/**
	 * <p>是否可用</p>
	 */
	private boolean available;
	/**
	 * <p>请求队列索引</p>
	 * <p>获取请求队列递增：保证UTP连接被均匀分配到请求队列</p>
	 */
	private final AtomicInteger queueIndex;
	/**
	 * <p>请求队列处理线程池</p>
	 * 
	 * @see #QUEUE_SIZE
	 */
	private final ExecutorService executor;
	/**
	 * <p>请求队列集合</p>
	 * 
	 * @see #QUEUE_SIZE
	 */
	private final List<BlockingQueue<UtpRequest>> queues;
	
	private UtpRequestQueue() {
		LOGGER.debug("启动UTP请求队列：{}", QUEUE_SIZE);
		this.available = true;
		this.queueIndex = new AtomicInteger(0);
		this.queues = new ArrayList<>(QUEUE_SIZE);
		this.executor = SystemThreadContext.newExecutor(QUEUE_SIZE, QUEUE_SIZE, 1000, 60, SystemThreadContext.SNAIL_THREAD_UTP_QUEUE);
		this.buildQueues();
	}
	
	/**
	 * <p>获取请求队列</p>
	 * 
	 * @return 请求队列
	 */
	public BlockingQueue<UtpRequest> queue() {
		final int index = this.queueIndex.getAndIncrement() % QUEUE_SIZE;
		return this.queues.get(Math.abs(index));
	}
	
	/**
	 * <p>新建请求队列</p>
	 */
	private void buildQueues() {
		for (int index = 0; index < QUEUE_SIZE; index++) {
			final var queue = new LinkedBlockingQueue<UtpRequest>();
			this.executor.submit(() -> this.queueExecute(queue));
			this.queues.add(queue);
		}
	}

	/**
	 * <p>处理请求队列</p>
	 * 
	 * @param queue 请求队列
	 */
	private void queueExecute(BlockingQueue<UtpRequest> queue) {
		while(this.available) {
			try {
				queue.take().execute();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				LOGGER.debug("UTP处理请求异常", e);
			} catch (Exception e) {
				LOGGER.error("UTP处理请求异常", e);
			}
		}
	}
	
	/**
	 * <p>关闭UTP请求队列处理线程池</p>
	 */
	public void shutdown() {
		LOGGER.debug("关闭UTP请求队列处理线程池");
		this.available = false;
		SystemThreadContext.shutdown(this.executor);
	}
	
}
