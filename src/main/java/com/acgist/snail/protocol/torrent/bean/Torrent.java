package com.acgist.snail.protocol.torrent.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.acgist.snail.system.bcode.BCodeDecoder;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * 种子信息
 * 
 * @author acgist
 * @since 1.0.0
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
	private String createdBy; // 创建者
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
		torrent.setCreatedBy(BCodeDecoder.getString(map, "created by"));
		torrent.setAnnounce(BCodeDecoder.getString(map, "announce"));
		torrent.setCreationDate(BCodeDecoder.getLong(map, "creation date"));
		final List<Object> announceList = BCodeDecoder.getList(map, "announce-list");
		if(announceList != null) {
			torrent.setAnnounceList(
				announceList.stream()
				.flatMap(value -> {
					final List<?> values = (List<?>) value;
					return values.stream();
				})
				.map(value -> BCodeDecoder.getString(value))
				.collect(Collectors.toList())
			);
		} else {
			torrent.setAnnounceList(new ArrayList<>(0));
		}
		final List<Object> nodes = BCodeDecoder.getList(map, "nodes");
		if(nodes != null) {
			torrent.setNodes(
				nodes.stream()
				.map(value -> {
					final List<?> values = (List<?>) value;
					if(values.size() == 2) {
						final String host = BCodeDecoder.getString(values.get(0));
						final Long port = (Long) values.get(1);
						if(StringUtils.isNumeric(host)) { // TODO：紧凑型IP和端口
							return Map.entry(
								NetUtils.decodeIntToIp(Integer.valueOf(host)),
								Long.valueOf(NetUtils.decodePort(port.shortValue()))
							);
						} else {
							return Map.entry(host, port);
						}
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
		final Map<String, Object> info = BCodeDecoder.getMap(map, "info");
		if(info != null) {
			final TorrentInfo torrentInfo = TorrentInfo.valueOf(info);
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

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
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
