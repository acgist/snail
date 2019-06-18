package com.acgist.snail.system.manager;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.protocol.Protocol;
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
public class ProtocolManager {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolManager.class);

	private static final ProtocolManager INSTANCE = new ProtocolManager();
	
	/**
	 * 下载协议
	 */
	private final List<Protocol> protocols;
	
	private final AtomicBoolean availableLock;
	
	private ProtocolManager() {
		this.protocols = new ArrayList<>();
		this.availableLock = new AtomicBoolean(false);
	}
	
	public static final ProtocolManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 注册协议
	 */
	public <T extends Protocol> ProtocolManager register(Protocol protocol) {
		LOGGER.info("注册下载协议：{}", protocol.name());
		this.protocols.add(protocol);
		return this;
	}
	
	/**
	 * 新建下载任务
	 */
	public TaskSession build(String url) throws DownloadException {
		final Protocol protocol = protocol(url);
		if(protocol == null) {
			throw new DownloadException("不支持的下载协议：" + url);
		}
		synchronized (protocol) {
			return protocol.build();
		}
	}

	/**
	 * 是否支持下载
	 */
	public boolean support(String url) {
		final Protocol protocol = protocol(url);
		if(protocol == null) {
			return false;
		}
		return true;
	}

	/**
	 * 获取下载协议
	 */
	public Protocol protocol(String url) {
		if(StringUtils.isEmpty(url)) {
			return null;
		}
		synchronized (this.protocols) {
			final Optional<Protocol> optional = this.protocols.stream()
				.filter(protocol -> protocol.available())
				.peek(protocol -> protocol.init(url))
				.filter(protocol -> protocol.verify())
				.findFirst();
			if(optional.isEmpty()) {
				return null;
			}
			return optional.get();
		}
	}

	/**
	 * 状态是否可用，阻塞线程。
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
	 * 设置可用状态，唤醒阻塞线程。
	 */
	public void available(boolean available) {
		synchronized (this.availableLock) {
			this.availableLock.set(true);
			this.availableLock.notifyAll();
		}
	}

}
