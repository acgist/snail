package com.acgist.snail.context;

/**
 * 上传速度和下载速度接口
 * 
 * @author acgist
 */
public interface ISpeedGetter {

    /**
     * @return 上传速度
     */
    long getUploadSpeed();
    
    /**
     * @return 下载速度
     */
    long getDownloadSpeed();
    
    /**
     * 重置上传速度和下载速度
     */
    void reset();
    
    /**
     * 重置上传速度
     */
    void resetUploadSpeed();
    
    /**
     * 重置下载速度
     */
    void resetDownloadSpeed();
    
}
