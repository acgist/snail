package com.acgist.snail.net.torrent;

/**
 * 种子文件基本信息
 * 
 * @author acgist
 */
public abstract class TorrentFileMatedata {

    /**
     * 文件Ed2k：{@value}
     */
    public static final String ATTR_ED2K = "ed2k";
    /**
     * 文件大小：{@value}
     */
    public static final String ATTR_LENGTH = "length";
    /**
     * 文件Hash：{@value}
     */
    public static final String ATTR_FILEHASH = "filehash";
    
    /**
     * 文件Ed2k
     */
    protected byte[] ed2k;
    /**
     * 文件大小
     */
    protected Long length;
    /**
     * 文件Hash
     */
    protected byte[] filehash;
    
    /**
     * 获取文件Ed2k
     * 
     * @return 文件Ed2k
     */
    public byte[] getEd2k() {
        return this.ed2k;
    }
    
    /**
     * 设置文件Ed2k
     * 
     * @param ed2k 文件Ed2k
     */
    public void setEd2k(byte[] ed2k) {
        this.ed2k = ed2k;
    }
    
    /**
     * 获取文件大小
     * 
     * @return 文件大小
     */
    public Long getLength() {
        return this.length;
    }

    /**
     * 设置文件大小
     * 
     * @param length 文件大小
     */
    public void setLength(Long length) {
        this.length = length;
    }

    /**
     * 获取文件Hash
     * 
     * @return 文件Hash
     */
    public byte[] getFilehash() {
        return this.filehash;
    }

    /**
     * 设置文件Hash
     * 
     * @param filehash 文件Hash
     */
    public void setFilehash(byte[] filehash) {
        this.filehash = filehash;
    }
    
}
