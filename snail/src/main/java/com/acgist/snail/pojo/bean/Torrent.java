package com.acgist.snail.pojo.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.format.BEncodeDecoder;
import com.acgist.snail.utils.NetUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>种子信息</p>
 * 
 * @author acgist
 */
public final class Torrent implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Torrent.class);
	
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
	/**
	 * <p>InfoHash</p>
	 * <p>种子文件加载完成时保存InfoHash，防止重复计算导致错误。</p>
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
		torrent.setAnnounce(decoder.getString(ATTR_ANNOUNCE));
		// 读取Tracker服务器列表
		final List<Object> announceList = decoder.getList(ATTR_ANNOUNCE_LIST);
		torrent.setAnnounceList(readAnnounceList(announceList));
		// 读取文件信息
		final Map<String, Object> info = decoder.getMap(ATTR_INFO);
		torrent.setInfo(TorrentInfo.valueOf(info, encoding));
		// 读取DHT节点
		final List<Object> nodes = decoder.getList(ATTR_NODES);
		torrent.setNodes(readNodes(nodes));
		return torrent;
	}
	
	/**
	 * <p>获取任务名称</p>
	 * <p>优先使用{@link TorrentInfo#getNameUtf8()}，如果获取失败，然后使用{@link TorrentInfo#getName()}。</p>
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
		if(announceList == null) {
			return new ArrayList<>(0);
		}
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
		if(nodes == null) {
			return new LinkedHashMap<>();
		}
		return nodes.stream()
			.map(value -> {
				final List<?> values = (List<?>) value;
				if(values.size() == 2) {
					final String host = StringUtils.getString(values.get(0));
					final Long port = (Long) values.get(1);
					if(StringUtils.isNumeric(host)) {
						// 紧凑型
						return Map.entry(
							NetUtils.intToIP(Integer.parseInt(host)),
							NetUtils.portToInt(port.shortValue())
						);
					} else {
						// 字符串
						return Map.entry(host, port.intValue());
					}
				} else {
					LOGGER.warn("DHT节点错误：{}", value);
					return null;
				}
			})
			.filter(Objects::nonNull)
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, LinkedHashMap::new));
	}

	/**
	 * <p>获取注释</p>
	 * 
	 * @return 注释
	 */
	public String getComment() {
		return this.comment;
	}

	/**
	 * <p>设置注释</p>
	 * 
	 * @param comment 注释
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * <p>获取注释UTF8</p>
	 * 
	 * @return 注释UTF8
	 */
	public String getCommentUtf8() {
		return this.commentUtf8;
	}

	/**
	 * <p>设置注释UTF8</p>
	 * 
	 * @param commentUtf8 注释UTF8
	 */
	public void setCommentUtf8(String commentUtf8) {
		this.commentUtf8 = commentUtf8;
	}

	/**
	 * <p>获取编码</p>
	 * 
	 * @return 编码
	 */
	public String getEncoding() {
		return this.encoding;
	}

	/**
	 * <p>设置编码</p>
	 * 
	 * @param encoding 编码
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	/**
	 * <p>获取创建者</p>
	 * 
	 * @return 创建者
	 */
	public String getCreatedBy() {
		return this.createdBy;
	}

	/**
	 * <p>设置创建者</p>
	 * 
	 * @param createdBy 创建者
	 */
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	/**
	 * <p>获取创建时间</p>
	 * 
	 * @return 创建时间
	 */
	public Long getCreationDate() {
		return this.creationDate;
	}

	/**
	 * <p>设置创建时间</p>
	 * 
	 * @param creationDate 创建时间
	 */
	public void setCreationDate(Long creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * <p>获取Tracker服务器</p>
	 * 
	 * @return Tracker服务器
	 */
	public String getAnnounce() {
		return this.announce;
	}

	/**
	 * <p>设置Tracker服务器</p>
	 * 
	 * @param announce Tracker服务器
	 */
	public void setAnnounce(String announce) {
		this.announce = announce;
	}

	/**
	 * <p>获取Tracker服务器列表</p>
	 * 
	 * @return Tracker服务器列表
	 */
	public List<String> getAnnounceList() {
		return this.announceList;
	}

	/**
	 * <p>设置Tracker服务器列表</p>
	 * 
	 * @param announceList Tracker服务器列表
	 */
	public void setAnnounceList(List<String> announceList) {
		this.announceList = announceList;
	}

	/**
	 * <p>获取文件信息</p>
	 * 
	 * @return 文件信息
	 */
	public TorrentInfo getInfo() {
		return this.info;
	}

	/**
	 * <p>设置文件信息</p>
	 * 
	 * @param info 文件信息
	 */
	public void setInfo(TorrentInfo info) {
		this.info = info;
	}

	/**
	 * <p>获取DHT节点</p>
	 * 
	 * @return DHT节点
	 */
	public Map<String, Integer> getNodes() {
		return this.nodes;
	}

	/**
	 * <p>设置DHT节点</p>
	 * 
	 * @param nodes DHT节点
	 */
	public void setNodes(Map<String, Integer> nodes) {
		this.nodes = nodes;
	}

}
