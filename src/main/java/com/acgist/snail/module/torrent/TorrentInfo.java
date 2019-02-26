package com.acgist.snail.module.torrent;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 磁力链接信息
 */
public class TorrentInfo {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TorrentInfo.class);
	
	private static final List<String> INFO_KEYS;
	
	static {
		String[] keys = {
			"path",
			"info",
			"name",
			"files",
			"pieces",
			"length",
			"md5sum",
			"comment",
			"announce",
			"created by",
			"piece length",
			"creation date",
			"announce-list"
		};
		INFO_KEYS = Arrays.asList(keys);
	}

	private String comment;
	private String createBy;
	private String announce;
	private Long creationDate;
	private List<String> announceList;
	private Info info;

	public TorrentInfo() {
	}

	public static final List<String> infoKeys() {
		return INFO_KEYS;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
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

	public Info getInfo() {
		return info;
	}

	public void setInfo(Info info) {
		this.info = info;
	}

	public void setValue(String key, Object value) throws Exception {
		if (!INFO_KEYS.contains(key)) {
			LOGGER.error("不存在的种子KEY：{}", key);
		} else {
			switch (key) {
				case "announce":
					this.setAnnounce(value.toString());
					break;
				case "announce-list":
					this.getAnnounceList().add(value.toString());
					break;
				case "creation date":
					if (StringUtils.isNumeric(value.toString())) {
						this.setCreationDate(Long.parseLong(value.toString()));
					} else {
						this.setCreationDate(0L);
					}
					break;
				case "comment":
					this.setComment(value.toString());
					break;
				case "created by":
					this.setCreateBy(value.toString());
					break;
				case "length":
					List<Files> filesList1 = this.getInfo().getFiles();
					if (filesList1 != null) {
						Files files = this.getInfo().getFiles().get(filesList1.size() - 1);
						files.setLength(Long.parseLong(value.toString()));
					} else {
						this.getInfo().setLength(Long.parseLong(value.toString()));
					}
					break;
				case "md5sum":
					List<Files> filesList2 = this.getInfo().getFiles();
					if (filesList2 != null) {
						Files files = this.getInfo().getFiles().get(filesList2.size() - 1);
						files.setMd5sum(value.toString());
					} else {
						this.getInfo().setMd5sum(value.toString());
					}
					break;
				case "name":
					this.getInfo().setName(value.toString());
					break;
				case "piece length":
					this.getInfo().setPiecesLength(Long.parseLong(value.toString()));
					break;
				case "pieces":
					if (StringUtils.isNumeric(value.toString())) {
						this.getInfo().setPieces(null);
					} else {
						this.getInfo().setPieces((byte[]) value);
					}
					break;
				case "path":
					List<Files> filesList3 = this.getInfo().getFiles();
					Files files3 = filesList3.get(filesList3.size() - 1);
					files3.getPath().add(value.toString());
					break;
			}
		}
	}
}
