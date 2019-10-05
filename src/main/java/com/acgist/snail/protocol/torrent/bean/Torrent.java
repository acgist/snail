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

	public static final String COMMENT = "comment";
	public static final String COMMENT_UTF8 = "comment.utf-8";
	public static final String ENCODING = "encoding";
	public static final String CREATED_BY = "created by";
	public static final String CREATION_DATE = "creation date";
	public static final String ANNOUNCE = "announce";
	public static final String ANNOUNCE_LIST = "announce-list";
	public static final String NODES = "nodes";
	public static final String INFO = "info";
	
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
	 * 创建时间
	 */
	private Long creationDate;
	/**
	 * Tracker主服务器
	 */
	private String announce;
	/**
	 * Tracker服务器列表
	 */
	private List<String> announceList;
	/**
	 * 文件信息
	 */
	private TorrentInfo info;
	/**
	 * DHT节点
	 */
	private Map<String, Integer> nodes;
	/**
	 * InfoHash
	 */
	private InfoHash infoHash;

	protected Torrent() {
	}
	
	public static final Torrent valueOf(BEncodeDecoder decoder) {
		final Torrent torrent = new Torrent();
		torrent.setComment(decoder.getString(COMMENT));
		torrent.setCommentUtf8(decoder.getString(COMMENT_UTF8));
		torrent.setEncoding(decoder.getString(ENCODING));
		torrent.setCreatedBy(decoder.getString(CREATED_BY));
		torrent.setCreationDate(decoder.getLong(CREATION_DATE));
		torrent.setAnnounce(decoder.getString(ANNOUNCE));
		final List<Object> announceList = decoder.getList(ANNOUNCE_LIST);
		if(announceList != null) {
			torrent.setAnnounceList(announceList(announceList));
		} else {
			torrent.setAnnounceList(new ArrayList<>(0));
		}
		final Map<String, Object> info = decoder.getMap(INFO);
		if(info != null) {
			final TorrentInfo torrentInfo = TorrentInfo.valueOf(info);
			torrent.setInfo(torrentInfo);
		}
		final List<Object> nodes = decoder.getList(NODES);
		if(nodes != null) {
			torrent.setNodes(nodes(nodes));
		} else {
			torrent.setNodes(new LinkedHashMap<>());
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
	
	/**
	 * <p>读取tracker服务器列表</p>
	 * <p>每个元素都是一个list，每个list里面有一个tracker服务器地址。</p>
	 */
	private static final List<String> announceList(List<Object> announceList) {
		return announceList.stream()
			.flatMap(value -> {
				final List<?> values = (List<?>) value;
				return values.stream();
			})
			.map(value -> BEncodeDecoder.getString(value))
			.collect(Collectors.toList());
	}
	
	/**
	 * <p>读取DHT节点</p>
	 * <p>每个元素都是一个list，每个list里面包含节点的IP和端口。</p>
	 */
	private static final Map<String, Integer> nodes(List<Object> nodes) {
		return nodes.stream()
			.map(value -> {
				final List<?> values = (List<?>) value;
				if(values.size() == 2) {
					final String host = BEncodeDecoder.getString(values.get(0));
					final Long port = (Long) values.get(1);
					if(StringUtils.isNumeric(host)) { // 紧凑型
						return Map.entry(
							NetUtils.decodeIntToIp(Integer.parseInt(host)),
							NetUtils.decodePort(port.shortValue())
						);
					} else { // 字符串
						return Map.entry(host, port.intValue());
					}
				} else {
					return null;
				}
			})
			.filter(value -> value != null)
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, LinkedHashMap::new));
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

	public Long getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Long creationDate) {
		this.creationDate = creationDate;
	}
	
	public String getAnnounce() {
		return announce;
	}
	
	public void setAnnounce(String announce) {
		this.announce = announce;
	}

	public List<String> getAnnounceList() {
		return announceList;
	}

	public void setAnnounceList(List<String> announceList) {
		this.announceList = announceList;
	}
	
	public TorrentInfo getInfo() {
		return info;
	}
	
	public void setInfo(TorrentInfo info) {
		this.info = info;
	}

	public Map<String, Integer> getNodes() {
		return nodes;
	}

	public void setNodes(Map<String, Integer> nodes) {
		this.nodes = nodes;
	}

	public InfoHash getInfoHash() {
		return infoHash;
	}

	public void setInfoHash(InfoHash infoHash) {
		this.infoHash = infoHash;
	}

}
