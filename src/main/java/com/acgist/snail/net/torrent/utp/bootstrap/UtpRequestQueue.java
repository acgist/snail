package com.acgist.snail.net.torrent.utp.bootstrap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.context.SystemThreadContext;
import com.acgist.snail.system.exception.NetException;

/**
 * UTP请求队列
 * 
 * @author acgist
 * @since 1.2.0
 */
public class UtpRequestQueue {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UtpRequestQueue.class);

	private static final UtpRequestQueue INSTANCE = new UtpRequestQueue();
	
	/**
	 * 队列数量
	 */
	private static final int QUEUE_SIZE = 4;
	
	/**
	 * 获取队列序号，每次获取请求队列后递增。
	 */
	private volatile int index = 0;
	/**
	 * UTP请求队列处理线程池
	 */
	private final ExecutorService executor;
	/**
	 * 请求处理队列集合
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
	 * 获取请求处理队列
	 */
	public BlockingQueue<UtpRequest> queue() {
		final int index = this.index % QUEUE_SIZE;
		this.index++;
		return this.queues.get(index);
	}
	
	/**
	 * 创建队列
	 */
	private void buildQueues() {
		for (int index = 0; index < QUEUE_SIZE; index++) {
			final var queue = new LinkedBlockingQueue<UtpRequest>();
			buildQueueExecute(queue);
			this.queues.add(queue);
		}
	}

	/**
	 * 创建队列处理线程
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
	 * 关闭UTP请求处理线程池
	 */
	public void shutdown() {
		LOGGER.info("关闭UTP请求处理线程池");
		SystemThreadContext.shutdown(this.executor);
	}
	
}
