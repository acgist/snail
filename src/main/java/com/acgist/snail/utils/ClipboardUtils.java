package com.acgist.snail.utils;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

/**
 * 剪切板工具
 */
public class ClipboardUtils {

	/**
	 * 剪切板拷贝
	 */
	public static final void copy(final String value) {
		final ClipboardContent content = new ClipboardContent();
		content.putString(value);
		final Clipboard clipboard = Clipboard.getSystemClipboard();
		clipboard.setContent(content);
	}
	
}
