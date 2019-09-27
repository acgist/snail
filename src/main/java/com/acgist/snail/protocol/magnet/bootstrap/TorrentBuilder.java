package com.acgist.snail.protocol.magnet.bootstrap;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.torrent.dht.bootstrap.NodeManager;
import com.acgist.snail.protocol.torrent.TorrentProtocol;
import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.acgist.snail.system.bencode.BEncodeDecoder;
import com.acgist.snail.system.bencode.BEncodeEncoder;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.CollectionUtils;
import com.acgist.snail.utils.DateUtils;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.NetUtils;

/**
 * 种子文件创建
 * 
 * @author acgist
 * @since 1.0.0
 */
public class TorrentBuilder {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentBuilder.class);
	
	/**
	 * InfoHash
	 */
	private final InfoHash infoHash;
	/**
	 * Tracker服务器
	 */
	private final List<String> trackers;
	
	private TorrentBuilder(InfoHash infoHash, List<String> trackers) {
		this.infoHash = infoHash;
		this.trackers = trackers;
	}
	
	public static final TorrentBuilder newInstance(InfoHash infoHash, List<String> trackers) {
		return new TorrentBuilder(infoHash, trackers);
	}
	
	/**
	 * 创建文件
	 * 
	 * @param path 文件路径
	 */
	public String buildFile(String path) {
		final String filePath = FileUtils.file(path, fileName());
		final Map<String, Object> fileData = fileData();
		this.createFile(filePath, fileData);
		return filePath;
	}

	/**
	 * 种子信息
	 */
	private Map<String, Object> fileData() {
		final Map<String, Object> data = new LinkedHashMap<>();
		data.put("comment", SystemConfig.getSource());
		data.put("comment.utf-8", SystemConfig.getSource());
		data.put("encoding", SystemConfig.DEFAULT_CHARSET);
		data.put("created by", SystemConfig.getNameEnAndVersion());
		data.put("creation date", DateUtils.unixTimestamp());
		this.announce(data);
		this.infoHash(data);
		this.node(data);
		return data;
	}

	/**
	 * 设置announce url
	 */
	private void announce(Map<String, Object> data) {
		if(CollectionUtils.isEmpty(this.trackers)) {
			return;
		}
		if(this.trackers.size() > 0) {
			data.put("announce", this.trackers.get(0));
		}
		if(this.trackers.size() > 1) {
			data.put(
				"announce-list",
				this.trackers.subList(1, this.trackers.size()).stream()
					.map(value -> List.of(value))
					.collect(Collectors.toList())
			);
		}
	}
	
	/**
	 * 设置InfoHash
	 */
	private void infoHash(Map<String, Object> data) {
		try (final var decoder = BEncodeDecoder.newInstance(this.infoHash.info())) {
			data.put("info", decoder.nextMap());
		} catch (NetException e) {
			LOGGER.error("InfoHash设置异常", e);
		}
	}

	/**
	 * 设置DHT节点
	 */
	private void node(Map<String, Object> data) {
		final var sessions = NodeManager.getInstance().findNode(this.infoHash.infoHash());
		if(CollectionUtils.isNotEmpty(sessions)) {
			final var nodes = sessions.stream()
				.filter(session -> NetUtils.verifyIp(session.getHost()))
				.map(session -> List.of(session.getHost(), session.getPort()))
				.collect(Collectors.toList());
			if(CollectionUtils.isNotEmpty(nodes)) {
				data.put("nodes", nodes);
			}
		}
	}
	
	/**
	 * 文件名称
	 */
	private String fileName() {
		return this.infoHash.infoHashHex() + TorrentProtocol.TORRENT_SUFFIX;
	}

	/**
	 * 保存种子文件
	 * 
	 * @param filePath 文件路径
	 * @param data 数据
	 */
	private void createFile(String filePath, Map<String, Object> data) {
		final File file = new File(filePath);
		// 文件已存在时不创建
		if(file.exists()) {
			return;
		}
		final byte[] bytes = BEncodeEncoder.encodeMap(data);
		FileUtils.write(filePath, bytes);
	}

}
