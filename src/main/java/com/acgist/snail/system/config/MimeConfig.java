package com.acgist.snail.system.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.acgist.snail.utils.FileUtils;

/**
 * <p>MIME类型配置</p>
 * 
 * @author acgist
 * @since 1.3.0
 */
public final class MimeConfig {

	public enum MimeType {
		
		TEXT_HTML("text/html"),
		// 图片
		IMAGE_BMP("image/bmp"),
		IMAGE_GIF("image/gif"),
		IMAGE_ICON("image/x-icon"),
		IMAGE_JPEG("image/jpeg"),
		IMAGE_PNG("image/png"),
		IMAGE_SVG("image/svg+xml"),
		// 音频
		AUDIO_ACC("audio/x-aac"),
		AUDIO_FLAC("audio/x-flac"),
		AUDIO_MP3("audio/mpeg"),
		AUDIO_M4A("audio/x-m4a"),
		AUDIO_OGG("audio/ogg"),
		AUDIO_WAV("audio/x-wav"),
		AUDIO_WMA("audio/x-ms-wma"),
		// 视频
		VIDEO_3GP("video/3gpp"),
		VIDEO_AVI("video/x-msvideo"),
		VIDEO_FLV("video/x-flv"),
		VIDEO_MKV("video/x-matroska"),
		VIDEO_MOV("video/quicktime"),
		VIDEO_MP4("video/mp4"),
		VIDEO_MVB("application/x-msmediaview"),
		VIDEO_RM("application/vnd.rn-realmedia"),
		VIDEO_RMVB("application/vnd.rn-realmedia-vbr"),
		VIDEO_WMV("video/x-ms-wmv"),
		// 未知二进制流
		APPLICATION_OCTET_STREAM("application/octet-stream");
		
		/**
		 * <p>MIME</p>
		 */
		private final String value;
		
		private MimeType(String value) {
			this.value = value;
		}
		
		public String value() {
			return this.value;
		}
		
	}
	
	private final static Map<MimeType, List<String>> MIMES = new HashMap<>();
	
	static {
		MIMES.put(MimeType.TEXT_HTML, List.of("html"));
		// 图片
		MIMES.put(MimeType.IMAGE_BMP, List.of("bmp"));
		MIMES.put(MimeType.IMAGE_GIF, List.of("gif"));
		MIMES.put(MimeType.IMAGE_ICON, List.of("ico"));
		MIMES.put(MimeType.IMAGE_JPEG, List.of("jpeg", "jpg"));
		MIMES.put(MimeType.IMAGE_PNG, List.of("png"));
		MIMES.put(MimeType.IMAGE_SVG, List.of("svg"));
		// 音频
		MIMES.put(MimeType.AUDIO_ACC, List.of("acc"));
		MIMES.put(MimeType.AUDIO_FLAC, List.of("flac"));
		MIMES.put(MimeType.AUDIO_MP3, List.of("mp3"));
		MIMES.put(MimeType.AUDIO_M4A, List.of("m4a"));
		MIMES.put(MimeType.AUDIO_OGG, List.of("ogg"));
		MIMES.put(MimeType.AUDIO_WAV, List.of("wav"));
		MIMES.put(MimeType.AUDIO_WMA, List.of("wma"));
		// 视频
		MIMES.put(MimeType.VIDEO_3GP, List.of("3gp"));
		MIMES.put(MimeType.VIDEO_AVI, List.of("avi"));
		MIMES.put(MimeType.VIDEO_FLV, List.of("flv"));
		MIMES.put(MimeType.VIDEO_MKV, List.of("flv"));
		MIMES.put(MimeType.VIDEO_MOV, List.of("mov"));
		MIMES.put(MimeType.VIDEO_MP4, List.of("mp4"));
		MIMES.put(MimeType.VIDEO_MVB, List.of("mvb"));
		MIMES.put(MimeType.VIDEO_RM, List.of("rm"));
		MIMES.put(MimeType.VIDEO_RMVB, List.of("rmvb"));
		MIMES.put(MimeType.VIDEO_WMV, List.of("wmv"));
	}
	
	/**
	 * <p>获取文件MIME类型</p>
	 * 
	 * @param file 文件
	 * 
	 * @return MIME类型：默认-{@link MimeType#APPLICATION_OCTET_STREAM}
	 */
	public static final MimeType mimeType(String file) {
		final String ext = FileUtils.fileExt(file);
		if(ext == null) {
			return MimeType.APPLICATION_OCTET_STREAM;
		}
		final var optional = MIMES.entrySet().stream()
			.filter(entry -> {
				return entry.getValue().contains(ext);
			})
			.map(Entry::getKey)
			.findFirst();
		if(optional.isEmpty()) {
			return MimeType.APPLICATION_OCTET_STREAM;
		}
		return optional.get();
	}
	
}
