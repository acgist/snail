package com.acgist.snail.net.peer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.acgist.snail.pojo.session.PeerSession;
import com.acgist.snail.pojo.session.StatisticsSession;

/**
 * PeerClient分组<br>
 * 每次剔除下载速度最低的一个PeerClient<br>
 */
public class PeerClientGroup {

	private List<PeerSession> peers = Collections.synchronizedList(new ArrayList<>());
	private List<PeerLauncher> launchers;
	
	/**
	 * 新增Peer
	 * @param parent torrent下载统计
	 * @param host 地址
	 * @param port 端口
	 */
	public void put(StatisticsSession parent, String host, Integer port) {
	}
	
	
}
