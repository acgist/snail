package com.acgist.snail.protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.wrapper.TaskWrapper;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.ThreadUtils;

/**
 * 下载协议管理器
 */
public class ProtocolManager {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolManager.class);

	private static final ProtocolManager INSTANCE = new ProtocolManager();
	
	private boolean available; // 初始化完成
	private List<Protocol> protocols;
	
	private ProtocolManager() {
		available = false;
		protocols = new ArrayList<>();
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
		this.available = available;
	}

	/**
	 * 新建下载任务
	 */
	public TaskWrapper build(String url) throws DownloadException {
		synchronized (protocols) {
			Optional<Protocol> optional = protocols.stream()
				.filter(protocol -> protocol.available())
				.map(protocol -> protocol.init(url))
				.filter(protocol -> protocol.verify())
				.findFirst();
			if(optional.isEmpty()) {
				throw new DownloadException("不支持的下载协议：" + url);
			}
			Protocol protocol = optional.get();
			return protocol.build();
		}
	}

	/**
	 * 是否可用，阻塞线程
	 */
	public void available() {
		while(!available) {
			ThreadUtils.sleep(100);
		}
	}
	
}
