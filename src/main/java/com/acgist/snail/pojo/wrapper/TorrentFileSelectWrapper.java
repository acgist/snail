package com.acgist.snail.pojo.wrapper;

import java.util.List;
import java.util.stream.Collectors;

import com.acgist.snail.system.bcode.BCodeDecoder;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.StringUtils;

public class TorrentFileSelectWrapper {

	private BCodeEncoderWrapper encoder;
	private BCodeDecoder decoder;

	private TorrentFileSelectWrapper() {
	}

	public static final TorrentFileSelectWrapper newEncoder(List<String> list) {
		final TorrentFileSelectWrapper wrapper = new TorrentFileSelectWrapper();
		if(CollectionUtils.isNotEmpty(list)) {
			wrapper.encoder = BCodeEncoderWrapper.newListInstance();
			list.forEach(value -> {
				wrapper.encoder.put(value);
			});
		}
		return wrapper;
	}
	
	public static final TorrentFileSelectWrapper newDecoder(String value) {
		final TorrentFileSelectWrapper wrapper = new TorrentFileSelectWrapper();
		if(StringUtils.isNotEmpty(value)) {
			wrapper.decoder = BCodeDecoder.newInstance(value.getBytes());
		}
		return wrapper;
	}
	
	public String toString() {
		if(this.encoder == null) {
			return null;
		}
		return encoder.toString();
	}
	
	public List<String> list() {
		if(this.decoder == null) {
			return null;
		}
		final List<Object> list = this.decoder.nextList();
		var x = list.stream()
		.filter(object -> object != null)
		.map(object -> {
			byte[] value = (byte[]) object;
			return new String(value);
		})
		.collect(Collectors.toList());
		return x;
	}
	
}
