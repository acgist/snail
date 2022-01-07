package com.acgist.snail.pojo.wrapper;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.acgist.snail.context.exception.PacketSizeException;
import com.acgist.snail.format.BEncodeDecoder;
import com.acgist.snail.format.BEncodeEncoder;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>下载描述包装器</p>
 * 
 * @author acgist
 */
public final class DescriptionWrapper {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DescriptionWrapper.class);

	/**
	 * <p>编码器</p>
	 */
	private final BEncodeEncoder encoder;
	/**
	 * <p>解码器</p>
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
	 * <p>新建下载描述包装器</p>
	 * 
	 * @param list 选择文件列表
	 * 
	 * @return {@link DescriptionWrapper}
	 */
	public static final DescriptionWrapper newEncoder(List<String> list) {
		BEncodeEncoder encoder;
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
	 * <p>新建下载描述包装器</p>
	 * 
	 * @param value 选择文件列表（B编码）
	 * 
	 * @return {@link DescriptionWrapper}
	 */
	public static final DescriptionWrapper newDecoder(String value) {
		BEncodeDecoder decoder;
		if(StringUtils.isNotEmpty(value)) {
			decoder = BEncodeDecoder.newInstance(value);
		} else {
			decoder = null;
		}
		return new DescriptionWrapper(null, decoder);
	}
	
	/**
	 * <p>编码选择文件</p>
	 * 
	 * @return 选择文件列表（B编码）
	 */
	public String serialize() {
		if(this.encoder == null) {
			return null;
		}
		return encoder.flush().toString();
	}

	/**
	 * <p>解析选择文件</p>
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
				.collect(Collectors.toList());
		} catch (PacketSizeException e) {
			LOGGER.error("解析选择文件异常", e);
		}
		return List.of();
	}

}
