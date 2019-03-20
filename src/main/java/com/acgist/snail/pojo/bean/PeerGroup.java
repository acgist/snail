package com.acgist.snail.pojo.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.StatisticsSession;

/**
 * Peer组
 */
public class PeerGroup {

	private List<PeerSession> peers = Collections.synchronizedList(new ArrayList<>());

	private PeerGroup() {
	}

	public static final PeerGroup newInstance() {
		return new PeerGroup();
	}
	
	/**
	 * 新增Peer
	 * @param parent torrent下载统计
	 * @param host 地址
	 * @param port 端口
	 */
	public void put(StatisticsSession parent, String host, Integer port) {
		
	}
	
}
