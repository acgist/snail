package com.acgist.snail.context;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import com.acgist.snail.IContext;
import com.acgist.snail.Snail;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.protocol.Protocol;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>下载协议上下文</p>
 * 
 * @author acgist
 */
public final class ProtocolContext implements IContext {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolContext.class);

	private static final ProtocolContext INSTANCE = new ProtocolContext();
	
	public static final ProtocolContext getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>下载协议</p>
	 */
	private final List<Protocol> protocols;
	/**
	 * <p>可用状态锁</p>
	 * <p>协议没有加载完成阻塞所有获取协议线程</p>
	 */
	private final AtomicBoolean availableLock;
	
	private ProtocolContext() {
		this.protocols = new ArrayList<>();
		this.availableLock = new AtomicBoolean(false);
	}
	
	/**
	 * <p>注册协议</p>
	 * 
	 * @param protocol 协议
	 * 
	 * @return ProtocolContext
	 */
	public ProtocolContext register(Protocol protocol) {
		if(this.protocols.contains(protocol)) {
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("下载协议已经注册：{}", protocol.name());
			}
		} else {
			if(LOGGER.isInfoEnabled()) {
				LOGGER.info("注册下载协议：{}", protocol.name());
			}
			this.protocols.add(protocol);
		}
		return this;
	}
	
	/**
	 * <p>新建下载器</p>
	 * 
	 * @param taskSession 任务信息
	 * 
	 * @return 下载器
	 * 
	 * @throws DownloadException 下载异常
	 */
	public IDownloader buildDownloader(ITaskSession taskSession) throws DownloadException {
		final var type = taskSession.getType();
		final Optional<Protocol> optional = this.protocols.stream()
			.filter(Protocol::available)
			.filter(protocol -> protocol.type() == type)
			.findFirst();
		if(optional.isEmpty()) {
			throw new DownloadException("不支持的下载类型：" + type);
		}
		final IDownloader downloader = optional.get().buildDownloader(taskSession);
		if(downloader == null) {
			throw new DownloadException("不支持的下载类型：" + type);
		}
		return downloader;
	}
	
	/**
	 * <p>新建下载任务</p>
	 * 
	 * @param url 下载链接
	 * 
	 * @return 任务信息
	 * 
	 * @throws DownloadException 下载异常
	 */
	public ITaskSession buildTaskSession(String url) throws DownloadException {
		final Optional<Protocol> protocol = this.protocol(url);
		if(protocol.isEmpty()) {
			throw new DownloadException("不支持的下载链接：" + url);
		}
		return protocol.get().buildTaskSession(url);
	}

	/**
	 * <p>判断是否支持下载</p>
	 * 
	 * @param url 下载链接
	 * 
	 * @return 是否支持
	 */
	public boolean support(String url) {
		return this.protocol(url).isPresent();
	}

	/**
	 * <p>获取下载协议</p>
	 * 
	 * @param url 下载链接
	 * 
	 * @return 下载协议
	 */
	public Optional<Protocol> protocol(final String url) {
		if(StringUtils.isEmpty(url)) {
			return Optional.empty();
		}
		final String verify = url.strip();
		return this.protocols.stream()
			.filter(Protocol::available)
			.filter(protocol -> protocol.verify(verify))
			.findFirst();
	}

	/**
	 * <p>判断状态是否可用（阻塞线程）</p>
	 * 
	 * @return 是否可用
	 * 
	 * @throws DownloadException 下载异常
	 */
	public boolean available() throws DownloadException {
		if(!this.availableLock.get()) {
			synchronized (this.availableLock) {
				if(!this.availableLock.get()) {
					try {
						this.availableLock.wait(Short.MAX_VALUE);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						LOGGER.debug("线程等待异常", e);
					}
				}
			}
		}
		if(Snail.available()) {
			return true;
		} else {
			throw new DownloadException("系统没有启动");
		}
	}

	/**
	 * <p>设置可用状态（释放阻塞线程）</p>
	 * 
	 * @param available 是否可用
	 */
	public void available(boolean available) {
		synchronized (this.availableLock) {
			this.availableLock.set(available);
			this.availableLock.notifyAll();
		}
	}
	
}
