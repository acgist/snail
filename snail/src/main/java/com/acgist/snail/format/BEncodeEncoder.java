package com.acgist.snail.format;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.acgist.snail.config.SymbolConfig;
import com.acgist.snail.format.BEncodeDecoder.Type;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;

/**
 * B编码编码器
 * 支持数据类型：Number、String、byte[]、Map、List
 * 
 * @author acgist
 */
public final class BEncodeEncoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(BEncodeEncoder.class);
    
    /**
     * List
     */
    private List<Object> list;
    /**
     * Map
     */
    private Map<String, Object> map;
    /**
     * 数据类型
     */
    private BEncodeDecoder.Type type;
    /**
     * 输出数据
     */
    private final ByteArrayOutputStream outputStream;
    
    private BEncodeEncoder() {
        this.outputStream = new ByteArrayOutputStream();
    }
    
    /**
     * 新建B编码编码器
     * 
     * @return {@link BEncodeEncoder}
     */
    public static final BEncodeEncoder newInstance() {
        return new BEncodeEncoder();
    }

    /**
     * 新建List
     * 
     * @return {@link BEncodeEncoder}
     */
    public BEncodeEncoder newList() {
        this.type = Type.LIST;
        this.list = new ArrayList<>();
        return this;
    }
    
    /**
     * 新建Map
     * 
     * @return {@link BEncodeEncoder}
     */
    public BEncodeEncoder newMap() {
        this.type = Type.MAP;
        this.map  = new LinkedHashMap<>();
        return this;
    }
    
    /**
     * 向List中添加数据
     * 
     * @param value 数据
     * 
     * @return {@link BEncodeEncoder}
     */
    public BEncodeEncoder put(Object value) {
        if(this.type == Type.LIST) {
            this.list.add(value);
        }
        return this;
    }
    
    /**
     * 向List中添加数据
     * 
     * @param list 数据
     * 
     * @return {@link BEncodeEncoder}
     */
    public BEncodeEncoder put(List<?> list) {
        if(this.type == Type.LIST) {
            this.list.addAll(list);
        }
        return this;
    }
    
    /**
     * 向Map中添加数据
     * 
     * @param key   键
     * @param value 值
     * 
     * @return {@link BEncodeEncoder}
     */
    public BEncodeEncoder put(String key, Object value) {
        if(this.type == Type.MAP) {
            this.map.put(key, value);
        }
        return this;
    }
    
    /**
     * 向Map中添加数据
     * 
     * @param map 数据
     * 
     * @return {@link BEncodeEncoder}
     */
    public BEncodeEncoder put(Map<String, ?> map) {
        if(this.type == Type.MAP) {
            this.map.putAll(map);
        }
        return this;
    }

    /**
     * 将数据写入字符流
     * 
     * @return {@link BEncodeEncoder}
     */
    public BEncodeEncoder flush() {
        if(this.type == Type.MAP) {
            this.writeMap(this.map);
        } else if(this.type == Type.LIST) {
            this.writeList(this.list);
        } else {
            LOGGER.warn("B编码错误（未知类型）：{}", this.type);
        }
        return this;
    }
    
    /**
     * 写入字节数组
     * 
     * @param bytes 数据
     * 
     * @return {@link BEncodeEncoder}
     */
    public BEncodeEncoder write(byte[] bytes) {
        try {
            if(bytes != null) {
                this.outputStream.write(bytes);
            }
        } catch (IOException e) {
            LOGGER.error("B编码输出异常", e);
        }
        return this;
    }
    
    /**
     * 写入字符
     * 
     * @param value 数据
     */
    private void write(char value) {
        this.outputStream.write(value);
    }
    
    /**
     * 写入B编码List
     * 
     * @param list 数据
     * 
     * @return {@link BEncodeEncoder}
     */
    private BEncodeEncoder writeList(List<?> list) {
        if(list == null) {
            return this;
        }
        this.write(SymbolConfig.BEncode.TYPE_L);
        list.forEach(this::writeObject);
        this.write(SymbolConfig.BEncode.TYPE_E);
        return this;
    }
    
    /**
     * 写入B编码Map
     * 
     * @param map 数据
     * 
     * @return {@link BEncodeEncoder}
     */
    private BEncodeEncoder writeMap(Map<?, ?> map) {
        if(map == null) {
            return this;
        }
        this.write(SymbolConfig.BEncode.TYPE_D);
        map.forEach((key, value) -> {
            this.writeObject(key);
            this.writeObject(value);
        });
        this.write(SymbolConfig.BEncode.TYPE_E);
        return this;
    }
    
    /**
     * 写入B编码数据
     * 
     * @param value 数据
     */
    private void writeObject(Object value) {
        if(value instanceof String string) {
            this.writeBytes(string.getBytes());
        } else if(value instanceof Number number) {
            this.writeNumber(number);
        } else if(value instanceof byte[] bytes) {
            this.writeBytes(bytes);
        } else if(value instanceof Map<?, ?> map) {
            this.writeMap(map);
        } else if(value instanceof List<?> list) {
            this.writeList(list);
        } else if(value == null) {
            this.writeBytes(new byte[0]);
        } else {
            this.writeBytes(value.toString().getBytes());
        }
    }
    
    /**
     * 写入B编码数值
     * 
     * @param number 数据
     */
    private void writeNumber(Number number) {
        this.write(SymbolConfig.BEncode.TYPE_I);
        this.write(number.toString().getBytes());
        this.write(SymbolConfig.BEncode.TYPE_E);
    }
    
    /**
     * 写入B编码字节数组
     * 
     * @param bytes 数据
     */
    private void writeBytes(byte[] bytes) {
        this.write(String.valueOf(bytes.length).getBytes());
        this.write(SymbolConfig.BEncode.SEPARATOR);
        this.write(bytes);
    }
    
    /**
     * @return 字节数组
     */
    public byte[] bytes() {
        return this.outputStream.toByteArray();
    }

    /**
     * @return 字符串
     * 
     * @see #bytes()
     */
    @Override
    public String toString() {
        return new String(this.bytes());
    }
    
    /**
     * List转为B编码字节数组
     * 
     * @param list List
     * 
     * @return B编码字节数组
     */
    public static final byte[] encodeList(List<?> list) {
        return newInstance().writeList(list).bytes();
    }
    
    /**
     * List转为B编码字符串
     * 
     * @param list List
     * 
     * @return B编码字符串
     */
    public static final String encodeListString(List<?> list) {
        return new String(encodeList(list));
    }
    
    /**
     * Map转为B编码字节数组
     * 
     * @param map Map
     * 
     * @return B编码字节数组
     */
    public static final byte[] encodeMap(Map<?, ?> map) {
        return newInstance().writeMap(map).bytes();
    }
    
    /**
     * Map转为B编码字符串
     * 
     * @param map Map
     * 
     * @return B编码字符串
     */
    public static final String encodeMapString(Map<?, ?> map) {
        return new String(encodeMap(map));
    }
    
}
