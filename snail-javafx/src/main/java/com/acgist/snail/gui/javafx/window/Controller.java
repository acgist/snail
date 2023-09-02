package com.acgist.snail.gui.javafx.window;

import java.io.File;
import java.util.List;

import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.utils.CollectionUtils;

import javafx.fxml.Initializable;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;

/**
 * 窗口控制器
 * 
 * @author acgist
 */
public abstract class Controller implements Initializable {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);

    /**
     * 双击次数：{@value}
     */
    protected static final int DOUBLE_CLICK_COUNT = 2;
    
    /**
     * 拖入面板数据
     * 支持数据：链接、文本、文件（路径）
     * 
     * @param event 拖拽事件
     * 
     * @return 链接、文本、文件（路径）
     */
    protected final String dragboard(DragEvent event) {
        final Dragboard dragboard = event.getDragboard();
        if (dragboard.hasFiles()) {
            // 优先判断文件
            final List<File> files = dragboard.getFiles();
            if (CollectionUtils.isNotEmpty(files)) {
                for (File file : files) {
                    if(file.isFile()) {
                        return file.getAbsolutePath();
                    }
                }
            }
        } else if (dragboard.hasUrl()) {
            return dragboard.getUrl();
        } else if (dragboard.hasString()) {
            return dragboard.getString();
        }
        if(LOGGER.isInfoEnabled()) {
            LOGGER.info("不支持的拖拽对象：{}", dragboard.getContentTypes());
        }
        return null;
    }

    /**
     * 释放控制器资源
     */
    public void release() {
        LOGGER.debug("释放控制器资源");
    }
    
}
