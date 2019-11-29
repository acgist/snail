package com.acgist.snail.net.torrent.utp.bootstrap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.system.exception.NetException;

/**
 * <p>UTP请求队列</p>
 * 
 * @author acgist
 * @since 1.2.0
 */
public final class UtpRequestQueue {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UtpRequestQueue.class);

	private static final UtpRequestQueue INSTANCE = new UtpRequestQueue();
	
	/**
	 * <p>请求队列数量</p>
	 * <p>使用相同数量的线程处理请求</p>
	 */
	private static final int QUEUE_SIZE = 4;
	
	/**
	 * <p>队列索引</p>
	 * <p>每次获取请求队列后递增，保证UTP请求被均匀分配。</p>
	 */
	private final AtomicInteger index = new AtomicInteger(0);
	/**
	 * <p>请求队列处理线程池</p>
	 */
	private final ExecutorService executor;
	/**
	 * <p>请求处理队列集合</p>
	 */
	private final List<BlockingQueue<UtpRequest>> queues;
	
	public UtpRequestQueue() {
		LOGGER.debug("启动UTP请求处理线程：{}", QUEUE_SIZE);
		this.queues = new ArrayList<>(QUEUE_SIZE);
		this.executor = SystemThreadContext.newExecutor(QUEUE_SIZE, QUEUE_SIZE, 1000, 60, SystemThreadContext.SNAIL_THREAD_UTP_HANDLER);
		buildQueues();
	}
	
	public static final UtpRequestQueue getInstance() {
		return INSTANCE;
	}
	
	/**
	 * @return 请求处理队列
	 */
	public BlockingQueue<UtpRequest> queue() {
		final int index = this.index.getAndIncrement() % QUEUE_SIZE;
		return this.queues.get(index);
	}
	
	/**
	 * <p>创建请求队列和处理线程</p>
	 */
	private void buildQueues() {
		for (int index = 0; index < QUEUE_SIZE; index++) {
			final var queue = new LinkedBlockingQueue<UtpRequest>();
			buildQueueExecute(queue);
			this.queues.add(queue);
		}
	}

	/**
	 * <p>创建请求队列处理线程</p>
	 */
	private void buildQueueExecute(BlockingQueue<UtpRequest> queue) {
		this.executor.submit(() -> {
			while(true) {
				try {
					final var request = queue.take();
					request.execute();
				} catch (NetException e) {
					LOGGER.error("UTP请求执行异常", e);
				} catch (InterruptedException e) {
					LOGGER.debug("UTP请求执行异常", e);
					Thread.currentThread().interrupt();
				} catch (Exception e) {
					LOGGER.error("UTP请求执行异常", e);
				}
			}
		});
	}
	
	/**
	 * <p>关闭UTP请求处理线程池</p>
	 */
	public void shutdown() {
		LOGGER.info("关闭UTP请求处理线程池");
		SystemThreadContext.shutdown(this.executor);
	}
	
}
