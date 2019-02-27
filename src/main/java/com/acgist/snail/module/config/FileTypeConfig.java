package com.acgist.snail.module.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * 文件类型配置
 */
public class FileTypeConfig {

	public enum FileType {
		
		image("图片", "image.png"),
		video("视频", "video.png"),
		audio("音频", "audio.png"),
		script("脚本", "script.png"),
		torrent("BT", "torrent.png"),
		compress("压缩", "compress.png"),
		document("文档", "document.png"),
		install("安装包", "install.png"),
		unknown("未知", "unknown.png");
		
		private String value;
		private String icon;

		private FileType(String value, String icon) {
			this.value = value;
			this.icon = icon;
		}

		public String getValue() {
			return value;
		}

		public String getIcon() {
			return icon;
		}

	}
	
	private static final Map<FileType, List<String>> TYPES = new HashMap<>();
	
	static {
		TYPES.put(FileType.image, List.of(
			"png", "jpg", "gif", "bmp"
		));
		TYPES.put(FileType.video, List.of(
			"rm", "flv", "mp4", "mvb", "avi", "3gp", "rmvb"
		));
		TYPES.put(FileType.audio, List.of(
			"mp3", "wmv", "wav", "aac", "flac"
		));
		TYPES.put(FileType.script, List.of(
			"c", "h", "sh", "js", "py", "bat", "cmd", "cpp", "php", "asp", "jsp", "java"
		));
		TYPES.put(FileType.torrent, List.of(
			"torrent"
		));
		TYPES.put(FileType.compress, List.of(
			"z", "gz", "7z", "rar", "zip", "iso", "tar", "bz2", "jar"
		));
		TYPES.put(FileType.document, List.of(
			"xml", "txt", "wps", "pdf", "css", "htm", "doc", "xls", "ppt", "docx", "xlsx", "pptx", "html"
		));
		TYPES.put(FileType.install, List.of(
			"exe", "com", "rpm", "apk", "deb"
		));
		TYPES.put(FileType.unknown, List.of(
		));
	}

	/**
	 * 获取文件类型
	 */
	public static final FileType type(String ext) {
		Optional<Entry<FileType, List<String>>> optional = TYPES
			.entrySet()
			.stream()
			.filter(entity -> {
				return entity.getValue().stream().filter(value -> value.equals(ext)).count() > 0;
			})
			.findFirst();
		if(optional.isPresent()) {
			return optional.get().getKey();
		}
		return FileType.unknown;
	}

}
