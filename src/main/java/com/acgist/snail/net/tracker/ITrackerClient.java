package com.acgist.snail.net.tracker;

import java.util.List;

import com.acgist.snail.pojo.bean.Peer;

/**
 * tracker协议<br>
 * 基本协议：TCP（HTTP）、UDP
 */
public interface ITrackerClient {

	/**
	 * 查找peer
	 */
	List<Peer> find();
	
}
