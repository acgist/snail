package com.acgist.snail.protocol.torrent.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.acgist.snail.net.bcode.BCodeDecoder;

/**
 * 种子信息
 */
public class Torrent {

	public static final List<String> INFO_KEYS;
	
	static {
		String[] keys = {
			"info",
			"name",
			"name.utf-8",
			"path",
			"path.utf-8",
			"ed2k",
			"files",
			"nodes",
			"pieces",
			"length",
			"md5sum", // 未遇到到过这个KEY
			"comment",
			"comment.utf-8",
			"filehash",
			"encoding",
			"announce",
			"publisher",
			"publisher.utf-8",
			"publisher-url",
			"publisher-url.utf-8",
			"created by",
			"piece length",
			"creation date",
			"announce-list"
		};
		INFO_KEYS = Arrays.asList(keys);
	}

	private String comment; // 注释
	private String commentUtf8; // 注释UTF8
	private String encoding; // 编码
	private String createBy; // 创建者
	private String announce; // Tracker主服务器
	private Long creationDate; // 创建时间
	private TorrentInfo info; // 文件信息
	private List<String> announceList; // Tracker服务器列表
	private Map<String, Long> nodes; // DHT协议：暂时不处理

	protected Torrent() {
	}
	
	public static final Torrent valueOf(Map<String, Object> map) {
		final Torrent torrent = new Torrent();
		torrent.setComment(BCodeDecoder.getString(map, "comment"));
		torrent.setCommentUtf8(BCodeDecoder.getString(map, "comment.utf-8"));
		torrent.setEncoding(BCodeDecoder.getString(map, "encoding"));
		torrent.setCreateBy(BCodeDecoder.getString(map, "created by"));
		torrent.setAnnounce(BCodeDecoder.getString(map, "announce"));
		torrent.setCreationDate(BCodeDecoder.getLong(map, "creation date"));
		List<?> announceList = (List<?>) map.get("announce-list");
		if(announceList != null) {
			torrent.setAnnounceList(
				announceList.stream()
				.flatMap(value -> {
					List<?> values = (List<?>) value;
					return values.stream();
				})
				.map(value -> BCodeDecoder.getString(value))
				.collect(Collectors.toList())
			);
		} else {
			torrent.setAnnounceList(new ArrayList<>(0));
		}
		List<?> nodes = (List<?>) map.get("nodes");
		if(nodes != null) {
			torrent.setNodes(
				nodes.stream()
				.map(value -> {
					List<?> values = (List<?>) value;
					if(values.size() == 2) {
						String host = BCodeDecoder.getString(values.get(0));
						Long port = (Long) values.get(1);
						return Map.entry(host, port);
					} else {
						return null;
					}
				})
				.filter(value -> value != null)
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, LinkedHashMap::new))
			);
		} else {
			torrent.setNodes(new LinkedHashMap<>());
		}
		Map<?, ?> info = (Map<?, ?>) map.get("info");
		if(info != null) {
			TorrentInfo torrentInfo = TorrentInfo.valueOf(info);
			torrent.setInfo(torrentInfo);
		}
		return torrent;
	}
	
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getCommentUtf8() {
		return commentUtf8;
	}

	public void setCommentUtf8(String commentUtf8) {
		this.commentUtf8 = commentUtf8;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getCreateBy() {
		return createBy;
	}

	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}

	public String getAnnounce() {
		return announce;
	}

	public void setAnnounce(String announce) {
		this.announce = announce;
	}

	public Long getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Long creationDate) {
		this.creationDate = creationDate;
	}

	public TorrentInfo getInfo() {
		return info;
	}

	public void setInfo(TorrentInfo info) {
		this.info = info;
	}

	public List<String> getAnnounceList() {
		return announceList;
	}

	public void setAnnounceList(List<String> announceList) {
		this.announceList = announceList;
	}

	public Map<String, Long> getNodes() {
		return nodes;
	}

	public void setNodes(Map<String, Long> nodes) {
		this.nodes = nodes;
	}
	
}
