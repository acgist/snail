package com.acgist.snail.net.peer.extension;

/**
 * http://www.bittorrent.org/beps/bep_0009.html
 */
public class UtMetadataMessageHandler {

	/**
	 * 顺序不可变
	 */
	public enum Type {
		
		request,
		data,
		reject;
		
	}
	
	public void request() {
	}

	public void data() {
	}

	public void reject() {
	}
	
}
