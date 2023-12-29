package com.acgist.snail.net.torrent.utp;

import com.acgist.snail.config.UtpConfig;
import com.acgist.snail.utils.BeanUtils;
import com.acgist.snail.utils.DateUtils;

/**
 * UTP窗口数据
 * 
 * @author acgist
 */
public final class UtpWindowData {

    /**
     * 请求编号
     */
    private final short seqnr;
    /**
     * 负载数据
     * 握手消息没有负载数据
     */
    private final byte[] data;
    /**
     * 数据长度
     */
    private final int length;
    /**
     * 时间戳（微秒）
     */
    private volatile int timestamp;
    /**
     * 发送次数
     */
    private volatile byte pushTimes;
    
    /**
     * @param seqnr     请求编号
     * @param timestamp 时间戳（微秒）
     * @param data      负载数据
     */
    private UtpWindowData(final short seqnr, final int timestamp, final byte[] data) {
        this.seqnr = seqnr;
        if(data == null) {
            this.data = new byte[0];
        } else {
            this.data = data;
        }
        this.length = this.data.length;
        this.timestamp = timestamp;
        this.pushTimes = 0;
    }
    
    /**
     * 新建窗口数据
     * 
     * @param seqnr     请求编号
     * @param timestamp 时间戳（微秒）
     * @param data      负载数据
     * 
     * @return {@link UtpWindowData}
     */
    public static final UtpWindowData newInstance(final short seqnr, final int timestamp, final byte[] data) {
        return new UtpWindowData(seqnr, timestamp, data);
    }

    /**
     * @return 请求编号
     */
    public short getSeqnr() {
        return this.seqnr;
    }
    
    /**
     * @return 负载数据
     */
    public byte[] getData() {
        return this.data;
    }
    
    /**
     * @return 数据长度
     */
    public int getLength() {
        return this.length;
    }
    
    /**
     * @return 时间戳（微秒）
     */
    public int getTimestamp() {
        return this.timestamp;
    }
    
    /**
     * @return 发送次数
     */
    public byte getPushTimes() {
        return this.pushTimes;
    }
    
    /**
     * 判断是否废弃
     * 
     * @return 是否废弃
     */
    public boolean discard() {
        return this.pushTimes > UtpConfig.MAX_PUSH_TIMES;
    }
    
    /**
     * 更新数据并返回时间戳
     * 
     * @return 时间戳（微秒）
     */
    public int updateGetTimestamp() {
        this.pushTimes++;
        this.timestamp = DateUtils.timestampUs();
        return this.timestamp;
    }
    
    @Override
    public String toString() {
        return BeanUtils.toString(this, this.seqnr, this.pushTimes);
    }

}
