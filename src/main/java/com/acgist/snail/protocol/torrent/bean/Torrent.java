package com.acgist.snail.protocol.torrent.bean;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.acgist.snail.system.bencode.BEncodeDecoder;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>种子信息</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class Torrent {

	/**
	 * 注释
	 */
	private String comment;
	/**
	 * 注释UTF8
	 */
	private String commentUtf8;
	/**
	 * 编码
	 */
	private String encoding;
	/**
	 * 创建者
	 */
	private String createdBy;
	/**
	 * Tracker主服务器
	 */
	private String announce;
	/**
	 * 创建时间
	 */
	private Long creationDate;
	/**
	 * 文件信息
	 */
	private TorrentInfo info;
	/**
	 * Tracker服务器列表
	 */
	private List<String> announceList;
	/**
	 * DHT节点
	 */
	private Map<String, Long> nodes;
	/**
	 * infoHash
	 */
	private InfoHash infoHash;

	protected Torrent() {
	}
	
	public static final Torrent valueOf(Map<String, Object> map) {
		final Torrent torrent = new Torrent();
		torrent.setComment(BEncodeDecoder.getString(map, "comment"));
		torrent.setCommentUtf8(BEncodeDecoder.getString(map, "comment.utf-8"));
		torrent.setEncoding(BEncodeDecoder.getString(map, "encoding"));
		torrent.setCreatedBy(BEncodeDecoder.getString(map, "created by"));
		torrent.setAnnounce(BEncodeDecoder.getString(map, "announce"));
		torrent.setCreationDate(BEncodeDecoder.getLong(map, "creation date"));
		final List<Object> announceList = BEncodeDecoder.getList(map, "announce-list");
		if(announceList != null) {
			torrent.setAnnounceList(
				announceList.stream()
				.flatMap(value -> {
					final List<?> values = (List<?>) value;
					return values.stream();
				})
				.map(value -> BEncodeDecoder.getString(value))
				.collect(Collectors.toList())
			);
		} else {
			torrent.setAnnounceList(new ArrayList<>(0));
		}
		final List<Object> nodes = BEncodeDecoder.getList(map, "nodes");
		if(nodes != null) {
			torrent.setNodes(
				nodes.stream()
				.map(value -> {
					// IP、Port
					final List<?> values = (List<?>) value;
					if(values.size() == 2) {
						final String host = BEncodeDecoder.getString(values.get(0));
						final Long port = (Long) values.get(1);
						if(StringUtils.isNumeric(host)) {
							return Map.entry(
								NetUtils.decodeIntToIp(Integer.parseInt(host)),
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
		final Map<String, Object> info = BEncodeDecoder.getMap(map, "info");
		if(info != null) {
			final TorrentInfo torrentInfo = TorrentInfo.valueOf(info);
			torrent.setInfo(torrentInfo);
		}
		return torrent;
	}
	
	/**
	 * 任务名称，优先使用nameUtf8，如果不存在使用name。
	 */
	public String name() {
		String name = this.info.getNameUtf8();
		if(StringUtils.isEmpty(name)) {
			name = StringUtils.charset(this.info.getName(), this.getEncoding());
		}
		return name;
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

	public InfoHash getInfoHash() {
		return infoHash;
	}

	public void setInfoHash(InfoHash infoHash) {
		this.infoHash = infoHash;
	}

}
