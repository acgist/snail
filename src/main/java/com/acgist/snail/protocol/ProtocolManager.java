package com.acgist.snail.protocol;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.system.context.SystemContext;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.StringUtils;
import com.acgist.snail.utils.ThreadUtils;

/**
 * <p>下载协议管理器</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class ProtocolManager {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolManager.class);

	private static final ProtocolManager INSTANCE = new ProtocolManager();
	
	/**
	 * <p>下载协议</p>
	 */
	private final List<Protocol> protocols;
	/**
	 * <p>可用锁：协议没有加载完成时阻塞所有获取协议的线程</p>
	 */
	private final AtomicBoolean availableLock = new AtomicBoolean(false);
	
	private ProtocolManager() {
		this.protocols = new ArrayList<>();
	}
	
	public static final ProtocolManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * <p>注册协议</p>
	 * 
	 * @param protocol 协议
	 * 
	 * @return {@link ProtocolManager}
	 */
	public ProtocolManager register(Protocol protocol) {
		LOGGER.info("注册下载协议：{}", protocol.name());
		this.protocols.add(protocol);
		return this;
	}
	
	/**
	 * <p>创建下载器</p>
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
			.filter(protocol -> protocol.available())
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
		final Protocol protocol = protocol(url);
		if(protocol == null) {
			throw new DownloadException("不支持的下载链接：" + url);
		}
		return protocol.buildTaskSession(url.trim());
	}

	/**
	 * <p>判断是否支持下载</p>
	 * 
	 * @param url 下载链接
	 * 
	 * @return {@code true}-支持；{@code false}-不支持；
	 */
	public boolean support(String url) {
		return protocol(url) != null;
	}

	/**
	 * <p>获取下载协议</p>
	 * 
	 * @param url 下载链接
	 * 
	 * @return 下载协议：{@code null}-不支持
	 */
	public Protocol protocol(String url) {
		if(StringUtils.isEmpty(url)) {
			return null;
		}
		final Optional<Protocol> optional = this.protocols.stream()
			.filter(protocol -> protocol.available())
			.filter(protocol -> protocol.verify(url.trim()))
			.findFirst();
		if(optional.isEmpty()) {
			return null;
		}
		return optional.get();
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
					ThreadUtils.wait(this.availableLock, Duration.ofSeconds(Byte.MAX_VALUE));
				}
			}
		}
		if(SystemContext.available()) {
			return true;
		} else {
			throw new DownloadException("系统正在关闭中");
		}
	}

	/**
	 * <p>设置可用状态（释放阻塞线程）</p>
	 * 
	 * @param available 是否可用
	 */
	public void available(boolean available) {
		synchronized (this.availableLock) {
			this.availableLock.set(true);
			this.availableLock.notifyAll();
		}
	}

}
