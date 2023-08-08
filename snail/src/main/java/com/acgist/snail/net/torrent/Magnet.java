package com.acgist.snail.net.torrent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.acgist.snail.utils.BeanUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * 磁力链接
 * 协议链接：https://baike.baidu.com/item/%E7%A3%81%E5%8A%9B%E9%93%BE%E6%8E%A5/5867775
 * 注意：只支持单文件下载
 * 
 * @author acgist
 */
public final class Magnet implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 磁力链接类型
     * 
     * @author acgist
     */
    public enum Type {
        
        /**
         * MD5
         */
        MD5("urn:md5:"),
        /**
         * AICH
         */
        AICH("urn:aich:"),
        /**
         * Kazaa
         */
        KAZAA("urn:kzhash:"),
        /**
         * BTIH：BitTorrent
         */
        BTIH("urn:btih:"),
        /**
         * ED2K
         */
        ED2K("urn:ed2k:"),
        /**
         * SHA-1
         */
        SHA1("urn:sha1:"),
        /**
         * CRC-32
         */
        CRC32("urn:crc32:"),
        /**
         * TTH：TigerTree
         */
        TTH("urn:tree:tiger:"),
        /**
         * BitPrint
         */
        BITPRINT("urn:bitprint:");
        
        /**
         * XT前缀
         */
        private final String prefix;
        
        /**
         * @param prefix XT前缀
         */
        private Type(String prefix) {
            this.prefix = prefix;
        }
        
        /**
         * @return XT前缀
         */
        public String prefix() {
            return this.prefix;
        }
        
    }
    
    /**
     * 显示名称
     */
    private String dn;
    /**
     * 文件大小
     */
    private Long xl;
    /**
     * 资源URN
     * 文件散列值URN
     */
    private String xt;
    /**
     * 文件链接
     * 原始文件链接
     */
    private String as;
    /**
     * 绝对资源
     * 种子文件链接
     */
    private String xs;
    /**
     * Tracker服务器列表
     */
    private List<String> tr;
    /**
     * 磁力链接类型
     * 
     * @see Type
     */
    private Type type;
    /**
     * BT：InfoHashHex
     * 
     * @see Type
     */
    private String hash;

    /**
     * 添加Tracker服务器
     * 
     * @param tr Tracker服务器
     */
    public void addTr(String tr) {
        if(this.tr == null) {
            this.tr = new ArrayList<>();
        }
        this.tr.add(tr);
    }
    
    /**
     * 判断是否支持下载
     * 
     * @return 是否支持下载
     */
    public boolean supportDownload() {
        return
            this.type == Type.BTIH             &&
            StringUtils.isNotEmpty(this.hash);
    }
    
    /**
     * @return 显示名称
     */
    public String getDn() {
        return this.dn;
    }

    /**
     * @param dn 显示名称
     */
    public void setDn(String dn) {
        this.dn = dn;
    }

    /**
     * @return 文件大小
     */
    public Long getXl() {
        return this.xl;
    }

    /**
     * @param xl 文件大小
     */
    public void setXl(Long xl) {
        this.xl = xl;
    }

    /**
     * @return 资源URN
     */
    public String getXt() {
        return this.xt;
    }

    /**
     * @param xt 资源URN
     */
    public void setXt(String xt) {
        this.xt = xt;
    }

    /**
     * @return 文件链接
     */
    public String getAs() {
        return this.as;
    }

    /**
     * @param as 文件链接
     */
    public void setAs(String as) {
        this.as = as;
    }

    /**
     * @return 绝对资源
     */
    public String getXs() {
        return this.xs;
    }

    /**
     * @param xs 绝对资源
     */
    public void setXs(String xs) {
        this.xs = xs;
    }
    
    /**
     * @return Tracker服务器列表
     */
    public List<String> getTr() {
        return this.tr;
    }
    
    /**
     * @param tr Tracker服务器列表
     */
    public void setTr(List<String> tr) {
        this.tr = tr;
    }
    
    /**
     * @return 磁力链接类型
     */
    public Type getType() {
        return this.type;
    }
    
    /**
     * @param type 磁力链接类型
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * @return Hash
     */
    public String getHash() {
        return this.hash;
    }

    /**
     * @param hash Hash
     */
    public void setHash(String hash) {
        this.hash = hash;
    }

    @Override
    public String toString() {
        return BeanUtils.toString(this);
    }
    
}
