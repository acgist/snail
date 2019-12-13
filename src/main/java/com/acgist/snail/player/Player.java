package com.acgist.snail.player;

import java.io.OutputStream;

/**
 * <p>播放器</p>
 * 
 * @author acgist
 * @since 1.3.0
 */
public abstract class Player {

	/**
	 * <p>播放文件</p>
	 */
	protected final String file;
	
	protected Player(String file) {
		this.file = file;
	}
	
	/**
	 * <p>设置响应头</p>
	 */
	public abstract void header();
	
	/**
	 * <p>开始播放</p>
	 * 
	 * @param output 播放输出流
	 */
	public abstract void play(OutputStream output);
	
}
