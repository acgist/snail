package com.acgist.snail.pojo.wrapper;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.bencode.BEncodeDecoder;
import com.acgist.snail.system.bencode.BEncodeEncoder;
import com.acgist.snail.system.exception.OversizePacketException;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * 种子文件选择包装器
 * 
 * @author acgist
 * @since 1.0.0
 */
public class TorrentSelectorWrapper {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentSelectorWrapper.class);

	private BEncodeEncoder encoder;
	private BEncodeDecoder decoder;

	private TorrentSelectorWrapper() {
	}

	/**
	 * 编码器
	 */
	public static final TorrentSelectorWrapper newEncoder(List<String> list) {
		final TorrentSelectorWrapper wrapper = new TorrentSelectorWrapper();
		if(CollectionUtils.isNotEmpty(list)) {
			wrapper.encoder = BEncodeEncoder.newInstance();
			wrapper.encoder.buildList().put(list);
		}
		return wrapper;
	}
	
	/**
	 * 解析器
	 */
	public static final TorrentSelectorWrapper newDecoder(String value) {
		final TorrentSelectorWrapper wrapper = new TorrentSelectorWrapper();
		if(StringUtils.isNotEmpty(value)) {
			wrapper.decoder = BEncodeDecoder.newInstance(value);
		}
		return wrapper;
	}
	
	/**
	 * 编码选择文件
	 */
	public String serialize() {
		if(this.encoder == null) {
			return null;
		}
		return encoder.flush().toString();
	}

	/**
	 * 解析选择文件
	 */
	public List<String> deserialize() {
		if(this.decoder == null) {
			return List.of();
		}
		try {
			return this.decoder.nextList().stream()
				.filter(object -> object != null)
				.map(object -> {
					return BEncodeDecoder.getString(object);
				})
				.collect(Collectors.toList());
		} catch (OversizePacketException e) {
			LOGGER.error("解析选择文件异常", e);
		}
		return List.of();
	}

}
