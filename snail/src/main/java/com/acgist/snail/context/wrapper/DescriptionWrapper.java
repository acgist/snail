package com.acgist.snail.context.wrapper;

import java.util.List;
import java.util.Objects;

import com.acgist.snail.context.ITaskSession;
import com.acgist.snail.format.BEncodeDecoder;
import com.acgist.snail.format.BEncodeEncoder;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.PacketSizeException;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * 下载描述包装器
 * 
 * @see ITaskSession#getDescription()
 * 
 * @author acgist
 */
public final class DescriptionWrapper {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DescriptionWrapper.class);

    /**
     * 编码器
     */
    private final BEncodeEncoder encoder;
    /**
     * 解码器
     */
    private final BEncodeDecoder decoder;

    /**
     * @param encoder 编码器
     * @param decoder 解码器
     */
    private DescriptionWrapper(BEncodeEncoder encoder, BEncodeDecoder decoder) {
        this.encoder = encoder;
        this.decoder = decoder;
    }

    /**
     * @param list 选择文件列表
     * 
     * @return {@link DescriptionWrapper}
     */
    public static final DescriptionWrapper newEncoder(List<String> list) {
        final BEncodeEncoder encoder;
        if(CollectionUtils.isNotEmpty(list)) {
            encoder = BEncodeEncoder.newInstance()
                .newList()
                .put(list);
        } else {
            encoder = null;
        }
        return new DescriptionWrapper(encoder, null);
    }
    
    /**
     * @param value 选择文件列表（B编码）
     * 
     * @return {@link DescriptionWrapper}
     */
    public static final DescriptionWrapper newDecoder(String value) {
        final BEncodeDecoder decoder;
        if(StringUtils.isNotEmpty(value)) {
            decoder = BEncodeDecoder.newInstance(value);
        } else {
            decoder = null;
        }
        return new DescriptionWrapper(null, decoder);
    }
    
    /**
     * 编码选择文件
     * 
     * @return 选择文件列表（B编码）
     */
    public String serialize() {
        if(this.encoder == null) {
            return null;
        }
        return this.encoder.flush().toString();
    }

    /**
     * 解析选择文件
     * 
     * @return 选择文件列表
     */
    public List<String> deserialize() {
        if(this.decoder == null) {
            return List.of();
        }
        try {
            return this.decoder.nextList().stream()
                .filter(Objects::nonNull)
                .map(StringUtils::getString)
                .toList();
        } catch (PacketSizeException e) {
            LOGGER.error("解析选择文件异常", e);
        }
        return List.of();
    }

}
