package com.acgist.snail.net;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.utils.Performance;

class PacketSizeExceptionTest extends Performance {

    @Test
    void testPacketSizeException() {
        PacketSizeException exception = assertThrows(PacketSizeException.class, () -> {throw new PacketSizeException();});
        this.log(exception.getMessage());
        exception = assertThrows(PacketSizeException.class, () -> {throw new PacketSizeException("网络包大小异常");});
        this.log(exception.getMessage());
        exception = assertThrows(PacketSizeException.class, () -> {throw new PacketSizeException(new NullPointerException());});
        this.log(exception.getMessage());
        exception = assertThrows(PacketSizeException.class, () -> {throw new PacketSizeException("网络包大小异常", new NullPointerException());});
        this.log(exception.getMessage());
        exception = assertThrows(PacketSizeException.class, () -> {PacketSizeException.verify(Integer.MAX_VALUE);});
        this.log(exception.getMessage());
        assertDoesNotThrow(() -> {PacketSizeException.verify(Short.MAX_VALUE);});
        assertDoesNotThrow(() -> {PacketSizeException.verify(SystemConfig.MAX_NET_BUFFER_LENGTH);});
    }
    
}
