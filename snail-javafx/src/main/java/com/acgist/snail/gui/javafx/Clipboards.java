package com.acgist.snail.gui.javafx;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

/**
 * 剪切板助手
 * 
 * @author acgist
 */
public final class Clipboards {

    private Clipboards() {
    }
    
    /**
     * 拷贝文本内容到剪切板
     * 
     * @param value 文本内容
     */
    public static final void copy(final String value) {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(value);
        clipboard.setContent(content);
    }
    
    /**
     * @return 剪切板文本内容
     */
    public static final String get() {
        return Clipboard.getSystemClipboard().getString();
    }
    
}
