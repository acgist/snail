package com.acgist.snail.system.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.protocol.magnet.MagnetResolver;
import com.acgist.snail.system.exception.DownloadException;

/**
 * 磁力链接转换器管理器
 */
public class MagnetResolverManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(MagnetResolverManager.class);
	
	private static final MagnetResolverManager INSTANCE = new MagnetResolverManager();

	private MagnetResolverManager() {
	}
	
	public static final MagnetResolverManager getInstance() {
		return INSTANCE;
	}
	
	private List<MagnetResolver> RESOLVERS = new ArrayList<>();

	/**
	 * 下载种子文件
	 */
	public File download(String url) throws DownloadException {
		synchronized (RESOLVERS) {
			File file = null;
			for (MagnetResolver coder : RESOLVERS) {
				file = coder.execute(url);
				if(file != null) {
					break;
				}
			}
			return file;
		}
	}
	
	/**
	 * 设置解码器排序
	 */
	public void register(MagnetResolver resolver) {
		LOGGER.info("注册磁力链接解码器：{}", resolver.name());
		RESOLVERS.add(resolver);
	}

	/**
	 * 排序
	 */
	public void sort() {
		RESOLVERS.sort((a, b) -> {
			return a.order().compareTo(b.order());
		});
	}
	
}
