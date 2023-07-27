package com.acgist.snail.net.torrent.codec;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

class MSECryptHandshakeHandlerTest extends Performance {

    private final ByteBuffer buffer = ByteBuffer.allocate(1024);
    
    @Test
    void testMatch() {
        this.buffer.put("0".repeat(10).getBytes());
        this.buffer.put("1".repeat(10).getBytes());
        this.buffer.put("2".repeat(10).getBytes());
        this.log(this.buffer);
        assertTrue(match("0".repeat(10).getBytes()));
        this.log(this.buffer);
        this.buffer.clear();
        this.buffer.put("0".repeat(10).getBytes());
        this.buffer.put("1".repeat(10).getBytes());
        this.buffer.put("2".repeat(10).getBytes());
        this.log(this.buffer);
        assertTrue(match("1".repeat(10).getBytes()));
        this.log(this.buffer);
        this.buffer.clear();
        this.buffer.put("0".repeat(10).getBytes());
        this.buffer.put("1".repeat(10).getBytes());
        this.buffer.put("2".repeat(10).getBytes());
        this.log(this.buffer);
        assertTrue(match("2".repeat(10).getBytes()));
        this.log(this.buffer);
        this.buffer.clear();
        this.buffer.put("0".repeat(10).getBytes());
        this.buffer.put("1".repeat(10).getBytes());
        this.buffer.put("2".repeat(10).getBytes());
        this.log(this.buffer);
        assertFalse(match("2".repeat(12).getBytes()));
        this.log(this.buffer);
        this.buffer.put("2".repeat(2).getBytes());
        this.log(this.buffer);
        assertTrue(match("2".repeat(12).getBytes()));
        this.log(this.buffer);
    }

    // 完全复制方法
    boolean match(byte[] bytes) {
        final int length = bytes.length;
        this.buffer.flip();
        if(this.buffer.remaining() < length) {
            this.buffer.compact();
            return false;
        }
        int index = 0;
        while(length > index) {
            if(this.buffer.get() != bytes[index]) {
                // 最开始的位置移动一位继续匹配
                this.buffer.position(this.buffer.position() - index);
                // 注意重置索引位置
                index = 0;
                if(this.buffer.remaining() < length) {
                    // 剩余数据不足跳出：防止丢弃匹配数据
                    break;
                }
            } else {
                index++;
            }
        }
        if(index == length) {
            // 丢弃填充数据
            this.buffer.position(this.buffer.position() - length);
            this.buffer.compact();
            return true;
        } else {
            // 丢弃填充数据
            this.buffer.compact();
            return false;
        }
    }
    
}
