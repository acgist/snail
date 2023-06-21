package com.acgist.snail.format;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.PacketSizeException;
import com.acgist.snail.utils.ByteUtils;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.ListUtils;
import com.acgist.snail.utils.MapUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * B编码解码器
 * 除了Long其他类型均为byte[]
 * 
 * @author acgist
 */
public final class BEncodeDecoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(BEncodeDecoder.class);
    
    /**
     * B编码数据类型
     * 
     * @author acgist
     */
    public enum Type {
        
        /**
         * Map
         */
        MAP,
        /**
         * List
         */
        LIST,
        /**
         * 未知
         */
        NONE;
        
    }
    
    /**
     * 结束
     */
    public static final char TYPE_E = 'e';
    /**
     * 数值
     */
    public static final char TYPE_I = 'i';
    /**
     * List
     */
    public static final char TYPE_L = 'l';
    /**
     * Map
     */
    public static final char TYPE_D = 'd';
    /**
     * 分隔符
     */
    public static final char SEPARATOR = ':';
    /**
     * B编码最短数据长度：开始结束符号
     */
    private static final int MIN_CONTENT_LENGTH = 2;
    
    /**
     * 数据类型
     */
    private Type type;
    /**
     * List
     */
    private List<Object> list;
    /**
     * Map
     */
    private Map<String, Object> map;
    /**
     * 原始数据
     */
    private final ByteArrayInputStream inputStream;
    
    /**
     * @param bytes 数据
     */
    private BEncodeDecoder(byte[] bytes) {
        Objects.requireNonNull(bytes, "B编码内容错误");
        if(bytes.length < BEncodeDecoder.MIN_CONTENT_LENGTH) {
            throw new IllegalArgumentException("B编码内容错误");
        }
        this.inputStream = new ByteArrayInputStream(bytes);
    }
    
    /**
     * 新建B编码解码器
     * 
     * @param bytes 数据
     * 
     * @return {@link BEncodeDecoder}
     */
    public static final BEncodeDecoder newInstance(byte[] bytes) {
        Objects.requireNonNull(bytes, "B编码内容错误");
        return new BEncodeDecoder(bytes);
    }
    
    /**
     * 新建B编码解码器
     * 
     * @param content 数据
     * 
     * @return {@link BEncodeDecoder}
     */
    public static final BEncodeDecoder newInstance(String content) {
        Objects.requireNonNull(content, "B编码内容错误");
        return new BEncodeDecoder(content.getBytes());
    }
    
    /**
     * 新建B编码解码器
     * 
     * @param buffer 数据
     * 
     * @return {@link BEncodeDecoder}
     */
    public static final BEncodeDecoder newInstance(ByteBuffer buffer) {
        Objects.requireNonNull(buffer, "B编码内容错误");
        return new BEncodeDecoder(ByteUtils.remainingToBytes(buffer));
    }
    
    /**
     * 判断是否没有数据
     * 
     * @return 是否没有数据
     */
    public boolean isEmpty() {
        if(this.type == null) {
            return true;
        }
        return switch (this.type) {
            case MAP  -> MapUtils.isEmpty(this.map);
            case LIST -> CollectionUtils.isEmpty(this.list);
            default -> true;
        };
    }
    
    /**
     * 判断是否含有数据
     * 
     * @return 是否含有数据
     */
    public boolean isNotEmpty() {
        return !this.isEmpty();
    }
    
    /**
     * 解析数据
     * 
     * @return this
     * 
     * @throws PacketSizeException 网络包大小异常
     * 
     * @see #nextType()
     */
    public BEncodeDecoder next() throws PacketSizeException {
        this.nextType();
        return this;
    }
    
    /**
     * 解析数据返回数据类型
     * 
     * @return 数据类型
     * 
     * @throws PacketSizeException 网络包大小异常
     */
    public Type nextType() throws PacketSizeException {
        // 是否含有数据
        final boolean none = this.inputStream == null || this.inputStream.available() <= 0;
        if(none) {
            this.type = Type.NONE;
            return this.type;
        }
        final char charType = (char) this.inputStream.read();
        switch (charType) {
        case TYPE_D:
            this.map  = BEncodeDecoder.readMap(this.inputStream);
            this.type = Type.MAP;
            break;
        case TYPE_L:
            this.list = BEncodeDecoder.readList(this.inputStream);
            this.type = Type.LIST;
            break;
        default:
            LOGGER.warn("B编码错误（未知类型）：{}", charType);
            this.type = Type.NONE;
            break;
        }
        return this.type;
    }
    
    /**
     * 解析数据List
     * 
     * @return List
     * 
     * @throws PacketSizeException 网络包大小异常
     * 
     * @see #nextType()
     */
    public List<Object> nextList() throws PacketSizeException {
        final Type nextType = this.nextType();
        if(nextType == Type.LIST) {
            return this.list;
        } else {
            LOGGER.warn("B编码解析List类型错误：{}", nextType);
        }
        return List.of();
    }
    
    /**
     * 解析数据Map
     * 
     * @return Map
     * 
     * @throws PacketSizeException 网络包大小异常
     * 
     * @see #nextType()
     */
    public Map<String, Object> nextMap() throws PacketSizeException {
        final Type nextType = this.nextType();
        if(nextType == Type.MAP) {
            return this.map;
        } else {
            LOGGER.warn("B编码解析Map类型错误：{}", nextType);
        }
        return Map.of();
    }
    
    /**
     * 读取剩余所有字节数组
     * 
     * @return 剩余所有字节数组
     */
    public byte[] oddBytes() {
        if(this.inputStream == null) {
            return new byte[0];
        }
        return this.inputStream.readAllBytes();
    }

    /**
     * 读取数值
     * 
     * @param inputStream 数据
     * 
     * @return 数值
     * 
     * @see #TYPE_I
     */
    private static final Long readLong(ByteArrayInputStream inputStream) {
        int index;
        char indexChar;
        final StringBuilder valueBuilder = new StringBuilder();
        while((index = inputStream.read()) >= 0) {
            indexChar = (char) index;
            if(indexChar == TYPE_E) {
                final String number = valueBuilder.toString();
                if(!StringUtils.isNumeric(number)) {
                    throw new IllegalArgumentException("B编码错误（数值）：" + number);
                }
                return Long.valueOf(number);
            } else {
                valueBuilder.append(indexChar);
            }
        }
        return 0L;
    }
    
    /**
     * 读取List
     * 
     * @param inputStream 数据
     * 
     * @return List
     * 
     * @throws PacketSizeException 网络包大小异常
     * 
     * @see #TYPE_L
     */
    private static final List<Object> readList(ByteArrayInputStream inputStream) throws PacketSizeException {
        int index;
        char indexChar;
        final List<Object> list = new ArrayList<>();
        final StringBuilder lengthBuilder = new StringBuilder();
        while ((index = inputStream.read()) >= 0) {
            indexChar = (char) index;
            switch (indexChar) {
                case TYPE_E -> {
                    return list;
                }
                case TYPE_I -> list.add(BEncodeDecoder.readLong(inputStream));
                case TYPE_L -> list.add(BEncodeDecoder.readList(inputStream));
                case TYPE_D -> list.add(BEncodeDecoder.readMap(inputStream));
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> lengthBuilder.append(indexChar);
                case SEPARATOR -> {
                    if(lengthBuilder.length() > 0) {
                        list.add(BEncodeDecoder.readBytes(lengthBuilder, inputStream));
                    } else {
                        LOGGER.warn("B编码错误（长度）：{}", lengthBuilder);
                    }
                }
                default -> LOGGER.warn("B编码错误（未知类型）：{}", indexChar);
            }
        }
        return list;
    }
    
    /**
     * 读取Map
     * 
     * @param inputStream 数据
     * 
     * @return Map
     * 
     * @throws PacketSizeException 网络包大小异常
     * 
     * @see #TYPE_D
     */
    private static final Map<String, Object> readMap(ByteArrayInputStream inputStream) throws PacketSizeException {
        int index;
        char indexChar;
        String key = null;
        // 使用LinkedHashMap防止乱序
        final Map<String, Object> map = new LinkedHashMap<>();
        final StringBuilder lengthBuilder = new StringBuilder();
        while ((index = inputStream.read()) >= 0) {
            indexChar = (char) index;
            switch (indexChar) {
                case TYPE_E -> {
                    return map;
                }
                case TYPE_I -> {
                    if(key != null) {
                        map.put(key, BEncodeDecoder.readLong(inputStream));
                        key = null;
                    } else {
                        LOGGER.warn("B编码key为空跳过（I）");
                    }
                }
                case TYPE_L -> {
                    if(key != null) {
                        map.put(key, BEncodeDecoder.readList(inputStream));
                        key = null;
                    } else {
                        LOGGER.warn("B编码key为空跳过（L）");
                    }
                }
                case TYPE_D -> {
                    if(key != null) {
                        map.put(key, BEncodeDecoder.readMap(inputStream));
                        key = null;
                    } else {
                        LOGGER.warn("B编码key为空跳过（D）");
                    }
                }
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> lengthBuilder.append(indexChar);
                case SEPARATOR -> {
                    if(lengthBuilder.length() > 0) {
                        final byte[] bytes = BEncodeDecoder.readBytes(lengthBuilder, inputStream);
                        if (key == null) {
                            key = new String(bytes);
                        } else {
                            map.put(key, bytes);
                            key = null;
                        }
                    } else {
                        LOGGER.warn("B编码错误（长度）：{}", lengthBuilder);
                    }
                }
                default -> LOGGER.warn("B编码错误（未知类型）：{}", indexChar);
            }
        }
        return map;
    }
    
    /**
     * 读取符合长度的字节数组
     * 
     * @param lengthBuilder 字节数组长度
     * @param inputStream   数据
     * 
     * @return 字节数组
     * 
     * @throws PacketSizeException 网络包大小异常
     */
    private static final byte[] readBytes(StringBuilder lengthBuilder, ByteArrayInputStream inputStream) throws PacketSizeException {
        final String number = lengthBuilder.toString();
        if(!StringUtils.isNumeric(number)) {
            throw new IllegalArgumentException("B编码错误（数值）：" + number);
        }
        final int length = Integer.parseInt(number);
        PacketSizeException.verify(length);
        // 循环使用设置归零
        lengthBuilder.setLength(0);
        final byte[] bytes = new byte[length];
        try {
            final int readLength = inputStream.read(bytes);
            if(readLength != length) {
                LOGGER.warn("B编码错误（读取长度和实际长度不符）：{} - {}", length, readLength);
            }
        } catch (IOException e) {
            LOGGER.error("B编码读取异常", e);
        }
        return bytes;
    }
    
    /**
     * @param index 索引
     * 
     * @return 对象
     */
    public Object get(int index) {
        return ListUtils.get(this.list, index);
    }
    
    /**
     * @param index 索引
     * 
     * @return 字节
     */
    public Byte getByte(int index) {
        return ListUtils.getByte(this.list, index);
    }
    
    /**
     * @param index 索引
     * 
     * @return 数值
     */
    public Integer getInteger(int index) {
        return ListUtils.getInteger(this.list, index);
    }
    
    /**
     * @param index 索引
     * 
     * @return 数值
     */
    public Long getLong(int index) {
        return ListUtils.getLong(this.list, index);
    }
    
    /**
     * @param index 索引
     * 
     * @return 字符串
     */
    public String getString(int index) {
        return ListUtils.getString(this.list, index);
    }

    /**
     * @param index    索引
     * @param encoding 编码
     * 
     * @return 字符串
     */
    public String getString(int index, String encoding) {
        return ListUtils.getString(this.list, index, encoding);
    }
    
    /**
     * @param index 索引
     * 
     * @return 字节数组
     */
    public byte[] getBytes(int index) {
        return ListUtils.getBytes(this.list, index);
    }
    
    /**
     * @param index 索引
     * 
     * @return List
     */
    public List<Object> getList(int index) {
        return ListUtils.getList(this.list, index);
    }
    
    /**
     * @param index 索引
     * 
     * @return Map
     */
    public Map<String, Object> getMap(int index) {
        return ListUtils.getMap(this.list, index);
    }
    
    /**
     * @param key 键
     * 
     * @return 对象
     */
    public Object get(String key) {
        return MapUtils.get(this.map, key);
    }
    
    /**
     * @param key 键
     * 
     * @return 字节
     */
    public Byte getByte(String key) {
        return MapUtils.getByte(this.map, key);
    }
    
    /**
     * @param key 键
     * 
     * @return 数值
     */
    public Integer getInteger(String key) {
        return MapUtils.getInteger(this.map, key);
    }
    
    /**
     * @param key 键
     * 
     * @return 数值
     */
    public Long getLong(String key) {
        return MapUtils.getLong(this.map, key);
    }
    
    /**
     * @param key 键
     * 
     * @return 字符串
     */
    public String getString(String key) {
        return MapUtils.getString(this.map, key);
    }
    
    /**
     * @param key      键
     * @param encoding 编码
     * 
     * @return 字符串
     */
    public String getString(String key, String encoding) {
        return MapUtils.getString(this.map, key, encoding);
    }
    
    /**
     * @param key 键
     * 
     * @return 字节数组
     */
    public byte[] getBytes(String key) {
        return MapUtils.getBytes(this.map, key);
    }
    
    /**
     * @param key 键
     * 
     * @return List
     */
    public List<Object> getList(String key) {
        return MapUtils.getList(this.map, key);
    }
    
    /**
     * @param key 键
     * 
     * @return Map
     */
    public Map<String, Object> getMap(String key) {
        return MapUtils.getMap(this.map, key);
    }
    
    @Override
    public String toString() {
        return new String(this.oddBytes());
    }
    
}
