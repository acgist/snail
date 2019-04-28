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
import com.acgist.snail.utils.ThreadUtils;

/**
 * 下载协议管理器
 */
public class ProtocolManager {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolManager.class);

	private static final ProtocolManager INSTANCE = new ProtocolManager();
	
	private final List<Protocol> protocols;
	private final AtomicBoolean availableLock;
	
	private ProtocolManager() {
		protocols = new ArrayList<>();
		availableLock = new AtomicBoolean(false);
	}
	
	public static final ProtocolManager getInstance() {
		return INSTANCE;
	}
	
	/**
	 * 注册协议
	 */
	public <T extends Protocol> void register(Protocol protocol) {
		LOGGER.info("注册下载协议：{}", protocol.name());
		protocols.add(protocol);
	}
	
	/**
	 * 设置状态
	 */
	public void available(boolean available) {
		synchronized (availableLock) {
			this.availableLock.set(true);
			availableLock.notifyAll();
		}
	}

	/**
	 * 新建下载任务
	 */
	public TaskSession build(String url) throws DownloadException {
		synchronized (protocols) {
			return protocol(url).build();
		}
	}

	/**
	 * 获取下载协议
	 */
	public Protocol protocol(String url) throws DownloadException {
		Optional<Protocol> optional = protocols.stream()
			.filter(protocol -> protocol.available())
			.map(protocol -> protocol.init(url))
			.filter(protocol -> protocol.verify())
			.findFirst();
		if(optional.isEmpty()) {
			throw new DownloadException("不支持的下载协议：" + url);
		}
		return optional.get();
	}
	
	/**
	 * 是否可用，阻塞线程
	 */
	public boolean available() throws DownloadException {
		if(!availableLock.get()) {
			synchronized (availableLock) {
				ThreadUtils.wait(availableLock, Duration.ofSeconds(Byte.MAX_VALUE));
			}
		}
		if(SystemContext.available()) {
			return true;
		} else {
			throw new DownloadException("系统正在关闭中");
		}
	}
	
}
