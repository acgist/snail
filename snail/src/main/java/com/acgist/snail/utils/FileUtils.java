package com.acgist.snail.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.pojo.ITaskSession.FileType;

/**
 * <p>文件工具</p>
 * 
 * @author acgist
 */
public final class FileUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);
	
	/**
	 * <p>文件大小单位</p>
	 */
	private static final String[] FILE_LENGTH_UNIT = {"B", "KB", "M", "G", "T"};
	/**
	 * <p>文件名禁用字符正则表达式：{@value}</p>
	 */
	private static final String FILENAME_REPLACE_REGEX = "[:/\\\\\\*\\?\\<\\>\\|]";
	/**
	 * <p>文件名禁用字符替换字符：{@value}</p>
	 */
	private static final String FILENAME_REPLACE_TARGET = "";
	/**
	 * <p>文件类型和文件后缀（扩展名）</p>
	 * <p>类型=后缀</p>
	 */
	private static final Map<FileType, List<String>> FILE_TYPE_EXT = new EnumMap<>(FileType.class);
	
	static {
		// 图片文件
		FILE_TYPE_EXT.put(FileType.IMAGE, List.of(
			"bmp", "cdr", "gif", "ico", "jpeg", "jpg", "png", "psd", "svg", "webp"
		));
		// 视频文件
		FILE_TYPE_EXT.put(FileType.VIDEO, List.of(
			"3gp", "avi", "flv", "m3u", "m3u8", "mkv", "mov", "mp4", "mpeg", "mvb", "rm", "rmvb", "wmv"
		));
		// 音频文件
		FILE_TYPE_EXT.put(FileType.AUDIO, List.of(
			"aac", "flac", "m4a", "mp3", "ogg", "wav", "wma"
		));
		// 脚本文件
		FILE_TYPE_EXT.put(FileType.SCRIPT, List.of(
			"asp", "bat", "c", "cmd", "cpp", "h", "java", "js", "jsp", "php", "py", "sh", "sql"
		));
		// 种子文件
		FILE_TYPE_EXT.put(FileType.TORRENT, List.of(
			"torrent"
		));
		// 压缩文件
		FILE_TYPE_EXT.put(FileType.COMPRESS, List.of(
			"7z", "bz2", "dmg", "gz", "iso", "jar", "rar", "tar", "z", "zip"
		));
		// 文档文件
		FILE_TYPE_EXT.put(FileType.DOCUMENT, List.of(
			"css", "csv", "doc", "docx", "htm", "html", "json", "log", "md", "pdf", "ppt", "pptx", "txt", "wps", "xls", "xlsx", "xml"
		));
		// 安装程序
		FILE_TYPE_EXT.put(FileType.INSTALL, List.of(
			"apk", "com", "deb", "exe", "pkg", "rpm"
		));
		// 未知文件
		FILE_TYPE_EXT.put(FileType.UNKNOWN, List.of(
		));
	}
	
	/**
	 * <p>工具类禁止实例化</p>
	 */
	private FileUtils() {
	}
	
	/**
	 * <p>统一路径分隔符为系统分隔符</p>
	 * 
	 * @param path 原始路径
	 * 
	 * @return 系统分隔符路径
	 */
	public static final String systemSeparator(String path) {
		if(path == null) {
			return path;
		}
		return path
			.replace('/', File.separatorChar)
			.replace('\\', File.separatorChar);
	}
	
	/**
	 * <p>删除文件</p>
	 * <p>支持目录</p>
	 * 
	 * @param filePath 文件路径
	 */
	public static final void delete(final String filePath) {
		if(StringUtils.isEmpty(filePath)) {
			LOGGER.warn("删除文件为空：{}", filePath);
			return;
		}
		delete(new File(filePath));
	}
	
	/**
	 * <p>递归删除文件</p>
	 * 
	 * @param file 文件
	 */
	public static final void delete(final File file) {
		Objects.requireNonNull(file);
		if(!file.exists()) {
			LOGGER.debug("删除文件不存在：{}", file.getAbsolutePath());
			return;
		}
		// 删除目录
		if(file.isDirectory()) {
			// 删除目录子文件
			final File[] files = file.listFiles();
			for (File children : files) {
				delete(children);
			}
		}
		// 删除当前文件或目录
		LOGGER.info("删除文件：{}", file.getAbsolutePath());
		try {
			Files.delete(file.toPath());
		} catch (IOException e) {
			LOGGER.error("删除文件失败：{}", file.getAbsoluteFile(), e);
		}
	}
	
	/**
	 * <p>从URL中获取文件名称</p>
	 * <p>过滤：协议、域名、路径、参数</p>
	 * 
	 * @param url URL
	 * 
	 * @return 文件名称
	 */
	public static final String fileName(final String url) {
		if(StringUtils.isEmpty(url)) {
			return url;
		}
		// URL解码
		String fileName = UrlUtils.decode(url);
		// 斜杠转换
		if(fileName.contains("\\")) {
			fileName = fileName.replace('\\', '/');
		}
		// 过滤：协议、域名、路径
		int index = fileName.lastIndexOf('/');
		if(index != -1) {
			fileName = fileName.substring(index + 1);
		}
		// 过滤：参数
		index = fileName.indexOf('?');
		if(index != -1) {
			fileName = fileName.substring(0, index);
		}
		return fileName.trim();
	}
	
	/**
	 * <p>文件名称过滤</p>
	 * <p>过滤：{@value #FILENAME_REPLACE_REGEX}</p>
	 * 
	 * @param name 文件名称
	 * 
	 * @return 文件名称
	 */
	public static final String fileNameFormat(String name) {
		// 过滤文件名禁用字符
		if(StringUtils.isNotEmpty(name)) {
			name = name.replaceAll(FILENAME_REPLACE_REGEX, FILENAME_REPLACE_TARGET);
		}
		// 过滤后名称为空：随机名称
		if(StringUtils.isEmpty(name)) {
			name = NumberUtils.build().toString();
		}
		return name.trim();
	}

	/**
	 * <p>获取文件类型</p>
	 * 
	 * @param fileName 文件名称
	 * 
	 * @return 文件类型
	 */
	public static final FileType fileType(String fileName) {
		final String ext = fileExt(fileName);
		if(ext == null) {
			return FileType.UNKNOWN;
		}
		// 小写
		final String extLower = ext.toLowerCase();
		return FILE_TYPE_EXT.entrySet().stream()
			.filter(entry -> entry.getValue().contains(extLower))
			.map(Entry::getKey)
//			.filter(Objects::nonNull) // 绝对有值
			.findFirst()
			.orElse(FileType.UNKNOWN);
	}
	
	/**
	 * <p>获取文件后缀</p>
	 * 
	 * @param fileName 文件名称
	 * 
	 * @return 文件后缀
	 */
	public static final String fileExt(String fileName) {
		if(StringUtils.isEmpty(fileName)) {
			return null;
		}
		final int index = fileName.lastIndexOf('.');
		if(index != -1) {
			return fileName.substring(index + 1);
		}
		return null;
	}
	
	/**
	 * <p>文件写入</p>
	 * 
	 * @param filePath 文件路径
	 * @param bytes 文件数据
	 */
	public static final void write(String filePath, byte[] bytes) {
		buildFolder(filePath, true); // 创建目录
		try(final var output = new FileOutputStream(filePath)) {
			output.write(bytes);
		} catch (IOException e) {
			LOGGER.error("文件写入异常：{}", filePath, e);
		}
	}
	
	/**
	 * <p>文件移动</p>
	 * 
	 * @param source 原始文件
	 * @param target 目标文件
	 */
	public static final void move(String source, String target) {
		final File sourceFile = new File(source);
		final File targetFile = new File(target);
		if(!sourceFile.renameTo(targetFile)) {
			LOGGER.warn("文件移动失败，原始文件：{}，目标文件：{}", source, target);
		}
	}
	
	/**
	 * <p>文件拷贝</p>
	 * 
	 * @param source 原始文件
	 * @param target 目标文件
	 */
	public static final void copy(String source, String target) {
		try(
			final var input = new FileInputStream(source);
			final var output = new FileOutputStream(target);
		) {
			input.transferTo(output);
		} catch (IOException e) {
			LOGGER.error("文件拷贝异常，原始文件：{}，目标文件：{}", source, target, e);
		}
	}
	
	/**
	 * <p>获取文件路径</p>
	 * 
	 * @param folder 文件目录
	 * @param fileName 文件名称
	 * 
	 * @return 文件路径
	 */
	public static final String file(String folder, String fileName) {
		Objects.requireNonNull(folder, "文件目录格式错误");
		Objects.requireNonNull(fileName, "文件名称格式错误");
		return Paths.get(folder, fileName).toString();
	}

	/**
	 * <p>文件大小格式化</p>
	 * 
	 * @param size 文件大小
	 * 
	 * @return 文件大小字符串
	 */
	public static final String formatSize(Long size) {
		if(size == null || size == 0L) {
			return "0B";
		}
		int index = 0;
		BigDecimal decimal = new BigDecimal(size);
		while(decimal.longValue() >= SystemConfig.DATA_SCALE) {
			if(++index >= FILE_LENGTH_UNIT.length) {
				index = FILE_LENGTH_UNIT.length - 1;
				break;
			}
			decimal = decimal.divide(new BigDecimal(SystemConfig.DATA_SCALE));
		}
		return decimal.setScale(2, RoundingMode.HALF_UP) + FILE_LENGTH_UNIT[index];
	}
	
	/**
	 * <p>文件大小格式化（MB）</p>
	 * 
	 * @param size 文件大小
	 * 
	 * @return 文件大小（MB）
	 */
	public static final double formatSizeMB(Long size) {
		if(size == null || size == 0L) {
			return 0D;
		}
		BigDecimal decimal = new BigDecimal(size);
		decimal = decimal.divide(BigDecimal.valueOf(SystemConfig.ONE_MB));
		return decimal.setScale(2, RoundingMode.HALF_UP).doubleValue();
	}
	
	/**
	 * <p>获取文件大小</p>
	 * <p>如果文件是目录，递归统计目录中所有文件大小。</p>
	 * 
	 * @param path 文件路径
	 * 
	 * @return 文件大小
	 */
	public static final long fileSize(String path) {
		final File file = new File(path);
		if(!file.exists()) {
			return 0L;
		}
		long size = 0L;
		if(file.isFile()) { // 文件
			try {
				size = Files.size(file.toPath());
			} catch (IOException e) {
				LOGGER.error("获取文件大小异常：{}", file.getAbsolutePath(), e);
			}
		} else { // 目录
			final File[] files = file.listFiles();
			for (File children : files) {
				size += fileSize(children.getAbsolutePath());
			}
		}
		return size;
	}
	
	/**
	 * <p>创建目录</p>
	 * 
	 * @param path 文件路径或目录路径
	 * @param isFile 路径是否是文件：true-文件；false-目录；
	 * 
	 * @see #buildFolder(File, boolean)
	 */
	public static final void buildFolder(String path, boolean isFile) {
		final File file = new File(path);
		buildFolder(file, isFile);
	}
	
	/**
	 * <p>创建目录</p>
	 * <p>如果路径是文件：创建上级目录</p>
	 * <p>如果路径是目录：创建目录</p>
	 * 
	 * @param file 文件路径或目录路径
	 * @param isFile 路径是否是文件：true-文件；false-目录；
	 */
	public static final void buildFolder(File file, boolean isFile) {
		if(file == null || file.exists()) {
			return;
		}
		if(isFile) {
			file = file.getParentFile();
		}
		if(!file.exists()) {
			file.mkdirs();
		}
	}
	
	/**
	 * <p>计算文件MD5值</p>
	 * 
	 * @param path 文件路径
	 * 
	 * @return 文件MD5值
	 * 
	 * @see #hash(String, String)
	 */
	public static final String md5(String path) {
		return hash(path, DigestUtils.ALGO_MD5);
	}

	/**
	 * <p>计算文件SHA-1值</p>
	 * 
	 * @param path 文件路径
	 * 
	 * @return 文件SHA-1值
	 * 
	 * @see #hash(String, String)
	 */
	public static final String sha1(String path) {
		return hash(path, DigestUtils.ALGO_SHA1);
	}
	
	/**
	 * <p>计算文件散列值</p>
	 * 
	 * @param path 文件路径
	 * @param algo 算法名称
	 * 
	 * @return 文件散列值
	 */
	private static final String hash(String path, String algo) {
		Objects.requireNonNull(path);
		Objects.requireNonNull(algo);
		final File file = new File(path);
		if(!file.exists() || !file.isFile()) {
			return null;
		}
		int length;
		final byte[] bytes = new byte[SystemConfig.DEFAULT_EXCHANGE_BYTES_LENGTH];
		final MessageDigest digest = DigestUtils.digest(algo);
		try (final var input = new BufferedInputStream(new FileInputStream(file))) {
			while ((length = input.read(bytes)) != -1) {
				digest.update(bytes, 0, length);
			}
		} catch (IOException e) {
			LOGGER.error("计算文件散列值异常：{}-{}", algo, path, e);
		}
		return StringUtils.hex(digest.digest());
	}
	
	/**
	 * <p>获取用户工作目录中的文件</p>
	 * 
	 * @param path 文件相对路径
	 * 
	 * @return 文件
	 */
	public static final File userDirFile(String path) {
		return new File(SystemConfig.userDir(path));
	}

}
