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
	private Map<String, Integer> nodes;
	/**
	 * infoHash
	 */
	private InfoHash infoHash;

	protected Torrent() {
	}
	
	public static final Torrent valueOf(BEncodeDecoder decoder) {
		final Torrent torrent = new Torrent();
		torrent.setComment(decoder.getString("comment"));
		torrent.setCommentUtf8(decoder.getString("comment.utf-8"));
		torrent.setEncoding(decoder.getString("encoding"));
		torrent.setCreatedBy(decoder.getString("created by"));
		torrent.setAnnounce(decoder.getString("announce"));
		torrent.setCreationDate(decoder.getLong("creation date"));
		final List<Object> announceList = decoder.getList("announce-list");
		if(announceList != null) {
			torrent.setAnnounceList(announceList(announceList));
		} else {
			torrent.setAnnounceList(new ArrayList<>(0));
		}
		final List<Object> nodes = decoder.getList("nodes");
		if(nodes != null) {
			torrent.setNodes(nodes(nodes));
		} else {
			torrent.setNodes(new LinkedHashMap<>());
		}
		final Map<String, Object> info = decoder.getMap("info");
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
