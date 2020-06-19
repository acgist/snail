package com.acgist.snail.pojo.bean;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.acgist.snail.system.format.BEncodeDecoder;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>种子信息</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class Torrent {

	/**
	 * <p>注释：{@value}</p>
	 */
	public static final String ATTR_COMMENT = "comment";
	/**
	 * <p>注释UTF8：{@value}</p>
	 */
	public static final String ATTR_COMMENT_UTF8 = "comment.utf-8";
	/**
	 * <p>编码：{@value}</p>
	 */
	public static final String ATTR_ENCODING = "encoding";
	/**
	 * <p>创建者：{@value}</p>
	 */
	public static final String ATTR_CREATED_BY = "created by";
	/**
	 * <p>创建时间：{@value}</p>
	 */
	public static final String ATTR_CREATION_DATE = "creation date";
	/**
	 * <p>Tracker服务器：{@value}</p>
	 */
	public static final String ATTR_ANNOUNCE = "announce";
	/**
	 * <p>Tracker服务器列表：{@value}</p>
	 */
	public static final String ATTR_ANNOUNCE_LIST = "announce-list";
	/**
	 * <p>文件信息：{@value}</p>
	 */
	public static final String ATTR_INFO = "info";
	/**
	 * <p>DHT节点：{@value}</p>
	 */
	public static final String ATTR_NODES = "nodes";
	
	// ============== 种子文件自带信息 ============== //
	/**
	 * <p>注释</p>
	 */
	private String comment;
	/**
	 * <p>注释UTF8</p>
	 */
	private String commentUtf8;
	/**
	 * <p>编码</p>
	 */
	private String encoding;
	/**
	 * <p>创建者</p>
	 */
	private String createdBy;
	/**
	 * <p>创建时间</p>
	 */
	private Long creationDate;
	/**
	 * <p>Tracker服务器</p>
	 */
	private String announce;
	/**
	 * <p>Tracker服务器列表</p>
	 */
	private List<String> announceList;
	/**
	 * <p>文件信息</p>
	 */
	private TorrentInfo info;
	/**
	 * <p>DHT节点</p>
	 */
	private Map<String, Integer> nodes;
	
	// ============== 种子文件临时信息 ============== //
	/**
	 * <p>InfoHash</p>
	 * <p>种子文件加载完成时保存InfoHash，防止计算种子Hash不一致。</p>
	 */
	private transient InfoHash infoHash;

	protected Torrent() {
	}
	
	/**
	 * <p>读取种子信息</p>
	 * 
	 * @param decoder 种子编码
	 * 
	 * @return 种子信息
	 */
	public static final Torrent valueOf(BEncodeDecoder decoder) {
		Objects.requireNonNull(decoder, "种子信息为空");
		final Torrent torrent = new Torrent();
		// 原始编码
		final String encoding = decoder.getString(ATTR_ENCODING);
		torrent.setEncoding(encoding);
		torrent.setComment(decoder.getString(ATTR_COMMENT, encoding));
		torrent.setCommentUtf8(decoder.getString(ATTR_COMMENT_UTF8));
		torrent.setCreatedBy(decoder.getString(ATTR_CREATED_BY));
		torrent.setCreationDate(decoder.getLong(ATTR_CREATION_DATE));
		// 读取Tracker服务器
		torrent.setAnnounce(decoder.getString(ATTR_ANNOUNCE));
		// 读取Tracker服务器列表
		final List<Object> announceList = decoder.getList(ATTR_ANNOUNCE_LIST);
		if(announceList != null) {
			torrent.setAnnounceList(readAnnounceList(announceList));
		} else {
			torrent.setAnnounceList(new ArrayList<>(0));
		}
		// 读取种子信息
		final Map<String, Object> info = decoder.getMap(ATTR_INFO);
		if(info != null) {
			final TorrentInfo torrentInfo = TorrentInfo.valueOf(info, encoding);
			torrent.setInfo(torrentInfo);
		}
		// 读取DHT节点
		final List<Object> nodes = decoder.getList(ATTR_NODES);
		if(nodes != null) {
			torrent.setNodes(readNodes(nodes));
		} else {
			torrent.setNodes(new LinkedHashMap<>());
		}
		return torrent;
	}
	
	/**
	 * <p>获取任务名称</p>
	 * <p>优先使用{@link TorrentInfo#getNameUtf8()}然后使用{@link TorrentInfo#getName()}</p>
	 * 
	 * @return 任务名称
	 */
	public String name() {
		String name = this.info.getNameUtf8();
		if(StringUtils.isEmpty(name)) {
			name = this.info.getName();
		}
		return name;
	}
	
	/**
	 * <p>获取InfoHash</p>
	 * 
	 * @return InfoHash
	 */
	public InfoHash infoHash() {
		return this.infoHash;
	}

	/**
	 * <p>设置InfoHash</p>
	 * 
	 * @param infoHash InfoHash
	 */
	public void infoHash(InfoHash infoHash) {
		this.infoHash = infoHash;
	}
	
	/**
	 * <p>获取Tracker服务器列表</p>
	 * <p>每个元素都是一个list，每个list里面包含一个Tracker服务器地址。</p>
	 * 
	 * @param announceList Tracker服务器数据
	 * 
	 * @return Tracker服务器列表
	 */
	private static final List<String> readAnnounceList(List<Object> announceList) {
		return announceList.stream()
			.flatMap(value -> {
				final List<?> values = (List<?>) value;
				return values.stream();
			})
			.map(value -> StringUtils.getString(value))
			.collect(Collectors.toList());
	}
	
	/**
	 * <p>获取DHT节点</p>
	 * <p>每个元素都是一个list，每个list里面包含节点的IP和端口。</p>
	 * 
	 * @param nodes DHT节点数据
	 * 
	 * @return DHT节点
	 */
	private static final Map<String, Integer> readNodes(List<Object> nodes) {
		return nodes.stream()
			.map(value -> {
				final List<?> values = (List<?>) value;
				if(values.size() == 2) {
					final String host = StringUtils.getString(values.get(0));
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

	// ============== GETTER SETTER ============== //
	
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

}
