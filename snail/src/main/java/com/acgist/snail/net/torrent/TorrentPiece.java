package com.acgist.snail.net.torrent;

import java.util.Arrays;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.utils.BeanUtils;
import com.acgist.snail.utils.DigestUtils;

/**
 * Piece下载信息
 * Piece一般大小设置为512KB、256KB、1MB，目前已知最大16MB。
 * BT任务基于文件下载，当某个Piece处于两个文件交接处时会被分为两次下载。
 * 
 * @author acgist
 */
public final class TorrentPiece {

    /**
     * 默认下载长度：{@value}
     */
    public static final int SLICE_LENGTH = 16 * SystemConfig.ONE_KB;

    /**
     * Piece大小
     */
    private final long pieceLength;
    /**
     * Piece索引
     */
    private final int index;
    /**
     * Piece开始偏移
     */
    private final int begin;
    /**
     * Piece结束偏移
     */
    private final int end;
    /**
     * 数据长度
     */
    private final int length;
    /**
     * 数据
     * 
     * TODO：使用直接内存文件读写使用NIO优化（没太大必要毕竟下载最重要的问题是网络IO）
     */
    private final byte[] data;
    /**
     * 校验数据
     */
    private final byte[] hash;
    /**
     * 是否校验
     */
    private final boolean verify;
    /**
     * 已经下载数据大小
     */
    private int size;
    /**
     * Piece数据内偏移
     */
    private int position;
    
    /**
     * @param pieceLength Piece大小
     * @param index Piece索引
     * @param begin Piece开始偏移
     * @param end Piece结束偏移
     * @param hash 校验数据
     * @param verify 是否校验
     */
    private TorrentPiece(long pieceLength, int index, int begin, int end, byte[] hash, boolean verify) {
        this.pieceLength = pieceLength;
        this.index = index;
        this.begin = begin;
        this.end = end;
        this.hash = hash;
        this.verify = verify;
        this.length = end - begin;
        this.data = new byte[this.length];
        this.size = 0;
        this.position = 0;
    }

    /**
     * 新建Piece下载信息
     * 
     * @param pieceLength Piece大小
     * @param index       Piece索引
     * @param begin       Piece开始偏移
     * @param end         Piece结束偏移
     * @param hash        校验数据
     * @param verify      是否校验
     * 
     * @return Piece下载信息
     */
    public static final TorrentPiece newInstance(long pieceLength, int index, int begin, int end, byte[] hash, boolean verify) {
        return new TorrentPiece(pieceLength, index, begin, end, hash, verify);
    }
    
    /**
     * 获取Piece在BT任务中的开始偏移
     * 
     * @return 开始偏移
     */
    public long beginPos() {
        return this.pieceLength * this.index + this.begin;
    }
    
    /**
     * 获取Piece在BT任务中的结束偏移
     * 
     * @return 结束偏移
     */
    public long endPos() {
        return this.beginPos() + this.length;
    }
    
    /**
     * 判断文件是否包含当前Piece
     * 包含开始和不包含结束（两边判断条件一样）：判断时都使用等于
     * 
     * @param fileBeginPos 文件开始偏移
     * @param fileEndPos   文件结束偏移
     * 
     * @return 是否包含
     */
    public boolean contain(long fileBeginPos, long fileEndPos) {
        final long endPos = this.endPos();
        if(endPos <= fileBeginPos) {
            return false;
        }
        final long beginPos = this.beginPos();
        if(beginPos >= fileEndPos) {
            return false;
        }
        return true;
    }
    
    /**
     * 判断是否还有更多的数据请求
     * 
     * @return 是否还有更多的数据请求
     */
    public boolean hasMoreSlice() {
        return this.position < this.length;
    }
    
    /**
     * 判断是否下载完成
     * 
     * @return 是否下载完成
     */
    public boolean completed() {
        return this.size >= this.length;
    }
    
    /**
     * 获取整个Piece内偏移
     * 
     * @return 整个Piece内偏移
     */
    public int position() {
        return this.begin + this.position;
    }
    
    /**
     * 获取本次请求数据大小
     * 注意：会重新计算内偏移
     * 
     * @return 本次请求数据大小
     */
    public int length() {
        if(this.position >= this.length) {
            return 0;
        }
        final int remaining = this.length - this.position;
        if(SLICE_LENGTH > remaining) {
            this.position = this.length;
            return remaining;
        } else {
            this.position += SLICE_LENGTH;
            return SLICE_LENGTH;
        }
    }
    
    /**
     * 写入Slice数据
     * 
     * @param begin Piece内开始偏移
     * @param bytes Slice数据
     * 
     * @return 是否下载完成
     */
    public boolean write(final int begin, final byte[] bytes) {
        synchronized (this) {
            System.arraycopy(bytes, 0, this.data, begin - this.begin, bytes.length);
            this.size += bytes.length;
            return this.completed();
        }
    }
    
    /**
     * 读取Slice数据
     * 
     * @param begin Piece内开始偏移
     * @param size  长度
     * 
     * @return Slice数据
     */
    public byte[] read(final int begin, final int size) {
        if(begin >= this.end) {
            return null;
        }
        final int end = begin + size;
        if(end <= this.begin) {
            return null;
        }
        // 当前数据开始偏移
        int beginPos = 0;
        if(begin > this.begin) {
            beginPos = begin - this.begin;
        }
        // 当前数据结束偏移
        int endPos = end - this.begin;
        if (endPos > this.data.length) {
            endPos = this.data.length;
        }
        // 读取数据真实长度
        final int length = endPos - beginPos;
        final byte[] bytes = new byte[length];
        System.arraycopy(this.data, beginPos, bytes, 0, length);
        return bytes;
    }
    
    /**
     * 校验数据
     * 
     * @return 是否校验成功
     */
    public boolean verify() {
        if(this.verify) {
            return Arrays.equals(DigestUtils.sha1(this.data), this.hash);
        }
        return true;
    }
    
    /**
     * 判断是否下载完成并且校验成功
     * 
     * @return 是否下载完成并且校验成功
     * 
     * @see #completed()
     * @see #verify()
     */
    public boolean completedAndVerify() {
        return this.completed() && this.verify();
    }
    
    /**
     * 获取Piece索引
     * 
     * @return Piece索引
     */
    public int getIndex() {
        return this.index;
    }

    /**
     * 获取Piece开始偏移
     * 
     * @return Piece开始偏移
     */
    public int getBegin() {
        return this.begin;
    }

    /**
     * 获取Piece结束偏移
     * 
     * @return Piece结束偏移
     */
    public int getEnd() {
        return this.end;
    }

    /**
     * 获取数据长度
     * 
     * @return 数据长度
     */
    public int getLength() {
        return this.length;
    }

    /**
     * 获取数据
     * 
     * @return 数据
     */
    public byte[] getData() {
        return this.data;
    }
    
    @Override
    public String toString() {
        return BeanUtils.toString(this, this.index, this.begin, this.end);
    }
    
}
