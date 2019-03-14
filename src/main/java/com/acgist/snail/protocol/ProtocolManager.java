package com.acgist.snail.protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.acgist.snail.downloader.IDownloader;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.ThreadUtils;

/**
 * 下载协议管理器
 */
public class ProtocolManager {

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
	public IDownloader buildDownloader(String url) throws DownloadException {
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
			return protocol.downloader();
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
