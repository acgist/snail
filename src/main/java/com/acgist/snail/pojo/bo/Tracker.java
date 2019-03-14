package com.acgist.snail.pojo.bo;

import java.util.List;

public class Tracker {

	private String url;
	private List<Peer> peers;
	private boolean usable; // 是否可用
	private int interval; // 下次请求时间间隔
}
