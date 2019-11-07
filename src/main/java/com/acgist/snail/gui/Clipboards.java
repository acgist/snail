package com.acgist.snail.gui;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

/**
 * <p>剪切板</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class Clipboards {

	/**
	 * 不允许实例化
	 */
	private Clipboards() {
	}
	
	/**
	 * 将文本内容拷贝到剪切板
	 * 
	 * @param value 文本内容
	 */
	public static final void copy(final String value) {
		final ClipboardContent content = new ClipboardContent();
		content.putString(value);
		final Clipboard clipboard = Clipboard.getSystemClipboard();
		clipboard.setContent(content);
	}
	
}
