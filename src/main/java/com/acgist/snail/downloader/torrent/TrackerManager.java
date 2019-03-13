package com.acgist.snail.downloader.torrent;

import java.util.List;

import com.acgist.snail.pojo.bo.Tracker;

/**
 * tracker管理器：<br>
 * 1.通过种子文件获取tracker信息
 * 2.通过tracker和dht获取用户peer
 * 3.维护tracker
 */
public class TrackerManager {

	private List<Tracker> trackers;
	
}
