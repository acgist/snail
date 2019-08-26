package com.acgist.snail.gui;

import com.acgist.snail.utils.CollectionUtils;

import javafx.scene.input.Dragboard;

/**
 * 抽象控制器
 * 
 * @author acgist
 * @since 1.1.0
 */
public abstract class Controller {

	/**
	 * 获取拖入面板的数据（Dragboard），获取文本、链接、文件。
	 * 
	 * @return 文本、链接、文件。
	 */
	protected String dragboard(Dragboard dragboard) {
		if(dragboard.hasFiles()) {
			final var files = dragboard.getFiles();
			if(CollectionUtils.isNotEmpty(files)) {
				return files.get(0).getPath();
			}
		} else if(dragboard.hasUrl()) {
			return dragboard.getUrl();
		} else if(dragboard.hasString()) {
			return dragboard.getString();
		}
		return null;
	}
	
}
