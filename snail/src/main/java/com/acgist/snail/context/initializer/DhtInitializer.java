package com.acgist.snail.context.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.torrent.dht.DhtManager;
import com.acgist.snail.net.torrent.dht.NodeManager;

/**
 * <p>初始化DHT</p>
 * 
 * @author acgist
 */
public final class DhtInitializer extends Initializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(DhtInitializer.class);
	
	/**
	 * <p>禁止创建实例</p>
	 */
	private DhtInitializer() {
	}
	
	public static final DhtInitializer newInstance() {
		return new DhtInitializer();
	}
	
	@Override
	protected void init() {
		LOGGER.info("初始化DHT");
		DhtManager.getInstance();
		NodeManager.getInstance();
	}

}
