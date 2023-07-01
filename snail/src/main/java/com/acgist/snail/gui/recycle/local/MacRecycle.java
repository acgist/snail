package com.acgist.snail.gui.recycle.local;

import java.io.File;
import java.nio.file.Paths;

import com.acgist.snail.gui.recycle.Recycle;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;

/**
 * Mac回收站
 * 
 * TODO：测试
 * 
 * @author acgist
 */
public class MacRecycle extends Recycle {

    private static final Logger LOGGER = LoggerFactory.getLogger(MacRecycle.class);
    
    /**
     * 废纸篓目录
     */
    private static final String TRASH_PATH = "~/.Trash";
    
    /**
     * Mac回收站
     * 
     * @param path 删除文件路径
     */
    public MacRecycle(String path) {
        super(path);
    }

    @Override
    public boolean delete() {
        final File source = this.file;
        final File target = Paths.get(TRASH_PATH, source.getName()).toFile();
        LOGGER.info("回收文件：{} - {}", source, target);
        return source.renameTo(target);
    }

}
