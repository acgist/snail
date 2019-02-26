package com.acgist.snail.module.decoder.torrent;

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
	private TorrentFileInfo info;

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

	public TorrentFileInfo getInfo() {
		return info;
	}

	public void setInfo(TorrentFileInfo info) {
		this.info = info;
	}

	public void setValue(String key, Object value) throws Exception {
		if (!INFO_KEYS.contains(key)) {
			LOGGER.error("不存在的种子KEY：{}", key);
		} else {
			TorrentFile file = null;
			List<TorrentFile> files = null;
			if(this.getInfo() != null) {
				files = this.getInfo().getFiles();
			}
			switch (key) {
				case "name":
					this.getInfo().setName(value.toString());
					break;
				case "path":
					file = files.get(files.size() - 1);
					file.getPath().add(value.toString());
					break;
				case "pieces":
					if (StringUtils.isNumeric(value.toString())) {
						this.getInfo().setPieces(null);
					} else {
						this.getInfo().setPieces((byte[]) value);
					}
					break;
				case "length":
					if (files != null) {
						file = files.get(files.size() - 1);
						file.setLength(Long.parseLong(value.toString()));
					} else {
						this.getInfo().setLength(Long.parseLong(value.toString()));
					}
					break;
				case "md5sum":
					if (files != null) {
						file = files.get(files.size() - 1);
						file.setMd5sum(value.toString());
					} else {
						this.getInfo().setMd5sum(value.toString());
					}
					break;
				case "comment":
					this.setComment(value.toString());
					break;
				case "announce":
					this.setAnnounce(value.toString());
					break;
				case "created by":
					this.setCreateBy(value.toString());
					break;
				case "piece length":
					this.getInfo().setPiecesLength(Long.parseLong(value.toString()));
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
			}
		}
	}
}
