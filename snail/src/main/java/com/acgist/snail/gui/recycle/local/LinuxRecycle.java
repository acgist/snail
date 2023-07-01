package com.acgist.snail.gui.recycle.local;

import java.io.File;
import java.nio.file.Paths;

import com.acgist.snail.gui.recycle.Recycle;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.utils.FileUtils;

/**
 * Linux回收站
 * 
 * TODO：测试
 * 
 * @author acgist
 */
public class LinuxRecycle extends Recycle {

    private static final Logger LOGGER = LoggerFactory.getLogger(LinuxRecycle.class);
    
    /**
     * 临时目录
     */
    private static final String TMP_PATH = "/tmp/acgist";
    
    static {
        FileUtils.buildFolder(TMP_PATH);
    }
    
    /**
     * Linux回收站
     * 
     * @param path
     */
    public LinuxRecycle(String path) {
        super(path);
    }

    @Override
    public boolean delete() {
        final File source = this.file;
        final File target = Paths.get(TMP_PATH, source.getName()).toFile();
        LOGGER.info("回收文件：{} - {}", source, target);
        return source.renameTo(target);
    }

}
