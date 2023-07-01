package com.acgist.snail.gui.recycle;

import java.io.File;
import java.nio.file.Paths;

import com.acgist.snail.utils.StringUtils;

/**
 * 回收站
 * 
 * @author acgist
 */
public abstract class Recycle {

    /**
     * 删除文件路径
     * 必须完整路径（不能填写相对路径）
     */
    protected final String path;
    /**
     * 删除文件
     */
    protected final File file;
    
    /**
     * 回收站
     * 
     * @param path 文件路径
     */
    protected Recycle(String path) {
        if(StringUtils.isEmpty(path)) {
            throw new IllegalArgumentException("删除文件路径错误：" + path);
        }
        this.file = Paths.get(path).toFile();
        this.path = file.getAbsolutePath();
    }
    
    /**
     * 删除文件
     * 
     * @return 是否删除成功
     */
    public abstract boolean delete();

}
