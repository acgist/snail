package com.acgist.snail.module.coder.torrent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.utils.StringUtils;

/**
 * 种子信息
 */
public class TorrentInfo {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentInfo.class);
	
	/**
	 * 属性：https://www.cnblogs.com/EasonJim/p/6601047.html
	 */
	private static final List<String> INFO_KEYS;
	
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

	private String nodes; // DHT协议：暂时不处理
	private String comment; // 注释
	private String commentUtf8; // 注释UTF8
	private String encoding; // 编码
	private String createBy; // 创建者
	private String announce; // Tracker主服务器
	private Long creationDate; // 创建时间
	private List<String> announceList = new ArrayList<>(); // Tracker服务器列表
	private TorrentFiles info; // 文件信息

	public static final List<String> infoKeys() {
		return INFO_KEYS;
	}

	public String getNodes() {
		return nodes;
	}

	public void setNodes(String nodes) {
		this.nodes = nodes;
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

	public List<String> getAnnounceList() {
		return announceList;
	}

	public void setAnnounceList(List<String> announceList) {
		this.announceList = announceList;
	}

	public TorrentFiles getInfo() {
		return info;
	}

	public void setInfo(TorrentFiles info) {
		this.info = info;
	}

	public boolean hashFiles() {
		return !this.info.getFiles().isEmpty();
	}
	
	public TorrentFile lastTorrentFile() {
		return this.info.lastTorrentFile();
	}
	
	public void setValue(String key, Object value) {
		if (!INFO_KEYS.contains(key)) {
			LOGGER.error("不存在的种子KEY：{}", key);
		} else {
			switch (key) {
				case "name":
					this.getInfo().setName(value.toString());
					break;
				case "name.utf-8":
					this.getInfo().setNameUtf8(value.toString());
					break;
				case "path":
					if(this.hashFiles()) {
						this.lastTorrentFile().getPath().add(value.toString());
					}
					break;
				case "path.utf-8":
					if(this.hashFiles()) {
						this.lastTorrentFile().getPathUtf8().add(value.toString());
					}
					break;
				case "ed2k":
					if(this.hashFiles()) {
						this.lastTorrentFile().setEd2k((byte[]) value);
					} else {
						this.getInfo().setEd2k((byte[]) value);
					}
					break;
				case "pieces":
					this.getInfo().setPieces((byte[]) value);
					break;
				case "length":
					if (this.hashFiles()) {
						this.lastTorrentFile().setLength(StringUtils.toLong(value.toString()));
					} else {
						this.getInfo().setLength(StringUtils.toLong(value.toString()));
					}
					break;
				case "comment":
					this.setComment(value.toString());
					break;
				case "comment.utf-8":
					this.setCommentUtf8(value.toString());
					break;
				case "filehash":
					if(this.hashFiles()) {
						this.lastTorrentFile().setFilehash((byte[]) value);
					} else {
						this.getInfo().setFilehash((byte[]) value);
					}
					break;
				case "encoding":
					this.setEncoding(value.toString());
					break;
				case "announce":
					this.setAnnounce(value.toString());
					break;
				case "publisher":
					this.getInfo().setPublisher(value.toString());
					break;
				case "publisher.utf-8":
					this.getInfo().setPublisherUtf8(value.toString());
					break;
				case "publisher-url":
					this.getInfo().setPublisherUrl(value.toString());
					break;
				case "publisher-url.utf-8":
					this.getInfo().setPublisherUrlUtf8(value.toString());
					break;
				case "created by":
					this.setCreateBy(value.toString());
					break;
				case "piece length":
					this.getInfo().setPieceLength(StringUtils.toLong(value.toString()));
					break;
				case "creation date":
					this.setCreationDate(StringUtils.toLong(value.toString()));
					break;
				case "announce-list":
					this.getAnnounceList().add(value.toString());
					break;
			}
		}
	}
	
}
