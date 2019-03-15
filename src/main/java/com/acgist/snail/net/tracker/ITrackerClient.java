package com.acgist.snail.net.tracker;

import java.util.List;

import com.acgist.snail.pojo.bean.Peer;

/**
 * tracker协议
 */
public interface ITrackerClient {

	/**
	 * 查找peer
	 */
	List<Peer> find();
	
}
