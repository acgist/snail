package com.acgist.snail.protocol.torrent;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.acgist.snail.system.bcode.BCodeDecoder;
import com.acgist.snail.system.bcode.BCodeEncoder;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.config.TrackerConfig;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.DataUtils;
import com.acgist.snail.utils.FileUtils;

/**
 * 种子文件创建
 */
public class TorrentBuilder {

	private final InfoHash infoHash;
	
	private TorrentBuilder(InfoHash infoHash) {
		this.infoHash = infoHash;
	}
	
	public static final TorrentBuilder newInstance(InfoHash infoHash) {
		return new TorrentBuilder(infoHash);
	}
	
	/**
	 * 创建文件
	 * @param path 文件路径
	 */
	public void buildFile(String path) {
		final String file = FileUtils.file(path, fileName());
		final Map<String, Object> fileData = fileData();
		this.createFile(file, fileData);
	}

	/**
	 * 获取种子信息
	 */
	private Map<String, Object> fileData() {
		final Map<String, Object> data = new LinkedHashMap<>();
		data.put("comment", "ACGIST Snail通过磁力链接下载创建");
		data.put("comment.utf-8", "ACGIST Snail通过磁力链接下载创建");
		data.put("encoding", SystemConfig.DEFAULT_CHARSET);
		data.put("created by", SystemConfig.getNameAndVersion());
		data.put("creation date", DataUtils.unixTimestamp());
		announce(data);
		infoHash(data);
//		data.put("nodes", value); // DHT
		return data;
	}

	/**
	 * 设置announce
	 */
	private void announce(Map<String, Object> data) {
		final List<String> list = TrackerConfig.list();
		if(CollectionUtils.isEmpty(list)) {
			return;
		}
		if(list.size() > 0) {
			data.put("announce", list.get(0));
		}
		if(list.size() > 1) { // 每个映射为一个list
			data.put("announce-list", list.subList(1, list.size()).stream().map(value -> List.of(value)).collect(Collectors.toList()));
		}
	}
	
	/**
	 * 设置infoHash
	 */
	private void infoHash(Map<String, Object> data) {
		final BCodeDecoder decoder = BCodeDecoder.newInstance(infoHash.info());
		data.put("info", decoder.mustMap());
	}

	/**
	 * 获取文件名称
	 */
	private String fileName() {
		return this.infoHash.infoHashHex() + TorrentProtocol.TORRENT_SUFFIX;
	}

	/**
	 * 保存文件
	 * @param filePath 文件路径
	 * @param data 数据
	 */
	private void createFile(String filePath, Map<String, Object> data) {
		final File file = new File(filePath);
		if(file.exists()) { // 文件已存在
			return;
		}
		final BCodeEncoder encoder = BCodeEncoder.newInstance();
		final byte[] bytes = encoder.build(data).bytes();
		FileUtils.write(filePath, bytes);
	}

}
