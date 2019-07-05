package com.acgist.snail.system.initializer.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.evaluation.PeerEvaluator;
import com.acgist.snail.system.initializer.Initializer;

/**
 * <p>初始化Peer管理器</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class PeerEvaluatorInitializer extends Initializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(PeerEvaluatorInitializer.class);
	
	private PeerEvaluatorInitializer() {
	}
	
	public static final PeerEvaluatorInitializer newInstance() {
		return new PeerEvaluatorInitializer();
	}
	
	@Override
	protected void init() {
		LOGGER.info("初始化Peer管理器");
		PeerEvaluator.getInstance().init();
	}

}
