package com.acgist.snail.pojo.wrapper;

import java.util.List;
import java.util.stream.Collectors;

import com.acgist.snail.system.bcode.BCodeDecoder;
import com.acgist.snail.system.bcode.BCodeEncoder;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * 种子文件选择包装器
 * 
 * @author acgist
 * @since 1.0.0
 */
public class TorrentFileSelectWrapper {

	private BCodeEncoder encoder;
	private BCodeDecoder decoder;

	private TorrentFileSelectWrapper() {
	}

	/**
	 * 编码器
	 */
	public static final TorrentFileSelectWrapper newEncoder(List<String> list) {
		final TorrentFileSelectWrapper wrapper = new TorrentFileSelectWrapper();
		if(CollectionUtils.isNotEmpty(list)) {
			wrapper.encoder = BCodeEncoder.newInstance().newList();
			wrapper.encoder.put(list);
		}
		return wrapper;
	}
	
	/**
	 * 解析器
	 */
	public static final TorrentFileSelectWrapper newDecoder(String value) {
		final TorrentFileSelectWrapper wrapper = new TorrentFileSelectWrapper();
		if(StringUtils.isNotEmpty(value)) {
			wrapper.decoder = BCodeDecoder.newInstance(value);
		}
		return wrapper;
	}
	
	/**
	 * 编码选择文件
	 */
	public String description() {
		if(this.encoder == null) {
			return null;
		}
		return encoder.flush().toString();
	}

	/**
	 * 解析选择文件
	 */
	public List<String> list() {
		if(this.decoder == null) {
			return List.of();
		}
		final List<Object> list = this.decoder.nextList();
		if(list == null) {
			return List.of();
		}
		return list.stream()
		.filter(object -> object != null)
		.map(object -> {
			return BCodeDecoder.getString(object);
		})
		.collect(Collectors.toList());
	}
	
}
