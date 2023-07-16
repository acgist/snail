package com.acgist.snail.net.hls;

import java.util.List;

import javax.crypto.Cipher;

import com.acgist.snail.config.SymbolConfig;
import com.acgist.snail.utils.BeanUtils;

/**
 * M3U8信息
 * 
 * @author acgist
 */
public final class M3u8 {
    
    /**
     * 类型
     * 
     * @author acgist
     */
    public enum Type {
        
        /**
         * 文件列表
         */
        FILE,
        /**
         * M3U8列表
         */
        M3U8,
        /**
         * 流媒体文件列表
         */
        STREAM;
        
    }
    
    /**
     * 加密协议
     * 
     * @author acgist
     */
    public enum Protocol {
        
        /**
         * 明文
         */
        NONE("NONE"),
        /**
         * AES-128
         */
        AES_128("AES-128"),
        /**
         * SAMPLE-AES
         */
        SAMPLE_AES("SAMPLE-AES");
        
        /**
         * 加密算法名称
         */
        private final String value;
        
        /**
         * @param value 加密算法名称
         */
        private Protocol(String value) {
            this.value = value;
        }
        
        /**
         * @param value 加密算法名称
         * 
         * @return 加密协议
         */
        public static final Protocol of(String value) {
            final Protocol[] protocols = Protocol.values();
            for (Protocol protocol : protocols) {
                if(protocol.value.equalsIgnoreCase(value)) {
                    return protocol;
                }
            }
            return null;
        }
        
    }
    
    /**
     * 类型
     */
    private final Type type;
    /**
     * 加密套件（可以为空表示明文）
     */
    private final Cipher cipher;
    /**
     * 文件列表
     * 多级M3U8列表：按照码率从小到大排序
     */
    private final List<String> links;
    
    /**
     * @param type   类型
     * @param cipher 加密套件
     * @param links  文件列表
     */
    public M3u8(Type type, Cipher cipher, List<String> links) {
        this.type   = type;
        this.cipher = cipher;
        this.links  = links;
    }
    
    /**
     * @return 获取类型
     */
    public Type getType() {
        return this.type;
    }

    /**
     * @return 加密套件
     */
    public Cipher getCipher() {
        return this.cipher;
    }
    
    /**
     * @return 文件列表
     */
    public List<String> getLinks() {
        return this.links;
    }
    
    /**
     * @return 码率最大的链接
     */
    public String getMaxRateLink() {
        final String maxRateLink = this.links.get(this.links.size() - 1);
        return maxRateLink.replace(
            SymbolConfig.Symbol.BACKSLASH.toChar(),
            SymbolConfig.Symbol.SLASH.toChar()
        );
    }

    @Override
    public String toString() {
        return BeanUtils.toString(this, this.type, this.cipher, this.links);
    }
    
}