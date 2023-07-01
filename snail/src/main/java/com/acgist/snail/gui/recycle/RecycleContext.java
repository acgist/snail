package com.acgist.snail.gui.recycle;

import java.util.function.Function;

import com.acgist.snail.context.IContext;
import com.acgist.snail.context.SystemContext.SystemType;
import com.acgist.snail.gui.recycle.local.LinuxRecycle;
import com.acgist.snail.gui.recycle.local.MacRecycle;
import com.acgist.snail.gui.recycle.local.WindowsRecycle;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.utils.StringUtils;

/**
 * 回收站上下文
 * 
 * @author acgist
 */
public final class RecycleContext implements IContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecycleContext.class);
    
    /**
     * 回收站构建器
     */
    private static final Function<String, Recycle> BUILDER;
    
    static {
        final SystemType systemType = SystemType.local();
        LOGGER.debug("初始化系统回收站：{}", systemType);
        if(systemType == SystemType.WINDOWS) {
            BUILDER = WindowsRecycle::new;
        } else if(systemType == SystemType.LINUX) {
            BUILDER = LinuxRecycle::new;
        } else if(systemType == SystemType.MAC) {
            BUILDER = MacRecycle::new;
        } else {
            LOGGER.warn("系统不支持回收站：{}", systemType);
            BUILDER = null;
        }
    }
    
    private RecycleContext() {
    }
    
    /**
     * 新建回收站
     * 
     * @param path 文件路径
     * 
     * @return 回收站
     */
    public static final Recycle newInstance(String path) {
        if(BUILDER == null) {
            return null;
        }
        return BUILDER.apply(path);
    }
    
    /**
     * 回收站删除文件
     * 
     * @param path 文件路径
     * 
     * @return 是否删除成功
     */
    public static final boolean recycle(final String path) {
        if(StringUtils.isEmpty(path)) {
            LOGGER.warn("回收站删除文件失败（路径错误）：{}", path);
            return false;
        }
        final Recycle recycle = RecycleContext.newInstance(path);
        if(recycle == null) {
            LOGGER.debug("回收站删除文件失败（不支持）：{}", path);
            return false;
        }
        return recycle.delete();
    }
    
}
