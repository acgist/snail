package com.acgist.snail.gui;

import com.acgist.snail.utils.CollectionUtils;

import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;

/**
 * <p>窗口控制器</p>
 * 
 * @author acgist
 * @since 1.1.0
 */
public abstract class Controller {

	/**
	 * <p>双击次数：{@value}</p>
	 */
	protected static final int DOUBLE_CLICK_COUNT = 2;
	/**
	 * <p>FXML样式路径：{@value}</p>
	 */
	public static final String FXML_STYLE = "/style/fxml.css";
	/**
	 * <p>图标文件路径（16PX）：{@value}</p>
	 */
	public static final String LOGO_ICON_16 = "/image/16/logo.png";
	/**
	 * <p>图标文件路径（200PX）：{@value}</p>
	 */
	public static final String LOGO_ICON_200 = "/image/logo.png";
	
	/**
	 * <p>获取拖入面板的数据</p>
	 * <p>支持数据：文本、链接、文件（路径）</p>
	 * 
	 * @param event 拖拽事件
	 * 
	 * @return 文本、链接、文件（路径）
	 */
	protected final String dragboard(DragEvent event) {
		final Dragboard dragboard = event.getDragboard();
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
