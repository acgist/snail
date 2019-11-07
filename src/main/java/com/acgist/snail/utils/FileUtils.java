package com.acgist.snail.utils;

import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.ITaskSession.FileType;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.exception.ArgumentException;
import com.acgist.snail.system.recycle.RecycleManager;

/**
 * <p>文件工具</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class FileUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);
	
	/**
	 * 文件大小单位
	 */
	private static final String[] FILE_LENGTH_UNIT = {"B", "KB", "M", "G", "T"};
	/**
	 * <p>文件名禁用字符正则表达式</p>
	 * <p>文件名禁用字符：\、/、:、*、?、<、>、|</p>
	 */
	private static final String FILENAME_REPLACE_REGEX = "[\\\\/:\\*\\?\\<\\>\\|]";
	/**
	 * 文件名禁用字符替换字符
	 */
	private static final String FILENAME_REPLACE_CHAR = "";
	/**
	 * 文件类型和文件后缀（扩展名）：类型=后缀
	 */
	private static final Map<FileType, List<String>> FILE_TYPE_EXT = new HashMap<>();
	
	static {
		FILE_TYPE_EXT.put(FileType.IMAGE, List.of(
			"bmp", "cdr", "gif", "ico", "jpeg", "jpg", "png", "psd", "svg"
		));
		FILE_TYPE_EXT.put(FileType.VIDEO, List.of(
			"3gp", "avi", "flv", "mkv", "mov", "mp4", "mvb", "rm", "rmvb"
		));
		FILE_TYPE_EXT.put(FileType.AUDIO, List.of(
			"aac", "flac", "mp3", "ogg", "wav", "wma", "wmv"
		));
		FILE_TYPE_EXT.put(FileType.SCRIPT, List.of(
			"asp", "bat", "c", "cmd", "cpp", "h", "java", "js", "jsp", "php", "py", "sh"
		));
		FILE_TYPE_EXT.put(FileType.TORRENT, List.of(
			"torrent"
		));
		FILE_TYPE_EXT.put(FileType.COMPRESS, List.of(
			"7z", "bz2", "gz", "iso", "jar", "rar", "tar", "z", "zip"
		));
		FILE_TYPE_EXT.put(FileType.DOCUMENT, List.of(
			"css", "doc", "docx", "htm", "html", "pdf", "ppt", "pptx", "txt", "wps", "xls", "xlsx", "xml"
		));
		FILE_TYPE_EXT.put(FileType.INSTALL, List.of(
			"apk", "com", "deb", "exe", "rpm"
		));
		FILE_TYPE_EXT.put(FileType.UNKNOWN, List.of(
		));
	}
	
	/**
	 * 删除文件
	 */
	public static final void delete(final String filePath) {
		if(StringUtils.isEmpty(filePath)) {
			return;
		}
		final File file = new File(filePath);
		if(!file.exists()) {
			LOGGER.warn("删除文件不存在：{}", filePath);
			return;
		}
		LOGGER.info("删除文件：{}", filePath);
		delete(file);
	}

	/**
	 * 递归删除文件
	 */
	private static final void delete(final File file) {
		if(file.isDirectory()) { // 目录
			final File[] files = file.listFiles();
			for (File children : files) {
				delete(children); // 删除子文件
			}
		}
		final var ok = file.delete(); // 删除当前文件或目录
		if(!ok) {
			LOGGER.warn("删除文件失败：{}", file.getAbsolutePath());
		}
	}
	
	/**
	 * 删除文件（回收站）
	 */
	public static final boolean recycle(final String filePath) {
		if(StringUtils.isEmpty(filePath)) {
			return false;
		}
		final var recycle = RecycleManager.newInstance(filePath);
		if(recycle == null) {
			return false;
		}
		return recycle.delete();
	}
	
	/**
	 * <p>通过URL获取文件名称</p>
	 * <p>去掉：协议、域名、路径、参数。</p>
	 */
	public static final String fileNameFromUrl(final String url) {
		if(StringUtils.isEmpty(url)) {
			return url;
		}
		String fileName = UrlUtils.decode(url);
		// 反斜杠转换
		if(fileName.contains("\\")) {
			fileName = fileName.replace("\\", "/");
		}
		int index = fileName.lastIndexOf("/");
		if(index != -1) {
			fileName = fileName.substring(index + 1);
		}
		index = fileName.indexOf("?");
		if(index != -1) {
			fileName = fileName.substring(0, index);
		}
		return fileName;
	}
	
	/**
	 * 获取正确的文件名称：去掉{@linkplain #FILENAME_REPLACE_REGEX 文件名禁用字符}
	 */
	public static final String fileName(String name) {
		if(StringUtils.isNotEmpty(name)) { // 去掉不支持的字符
			name = name.replaceAll(FILENAME_REPLACE_REGEX, FILENAME_REPLACE_CHAR);
		}
		if(StringUtils.isEmpty(name)) { // 随机序号
			name = NumberUtils.build().toString();
		}
		return name.trim();
	}

	/**
	 * 根据文件名称获取文件类型
	 */
	public static final FileType fileType(String fileName) {
		final String ext = fileExt(fileName);
		if(ext == null) {
			return FileType.UNKNOWN;
		}
		final String extLower = ext.toLowerCase();
		final Optional<FileType> optional = FILE_TYPE_EXT.entrySet().stream()
			.filter(entry -> entry.getValue().contains(extLower))
			.map(Entry::getKey)
			.findFirst();
		if(optional.isPresent()) {
			return optional.get();
		}
		return FileType.UNKNOWN;
	}
	
	/**
	 * 获取文件后缀
	 */
	public static final String fileExt(String fileName) {
		if(StringUtils.isEmpty(fileName)) {
			return null;
		}
		final int index = fileName.lastIndexOf(".");
		if(index != -1) {
			return fileName.substring(index + 1);
		}
		return null;
	}
	
	/**
	 * 资源管理器打开文件
	 */
	public static final void openInDesktop(File file) {
		try {
			Desktop.getDesktop().open(file);
		} catch (IOException e) {
			LOGGER.error("资源管理器打开文件异常", e);
		}
	}

	/**
	 * 文件写入
	 */
	public static final void write(String filePath, byte[] bytes) {
		buildFolder(filePath, true);
		try(final var output = new FileOutputStream(filePath)) {
			output.write(bytes);
		} catch (IOException e) {
			LOGGER.error("文件写入异常", e);
		}
	}
	
	/**
	 * 文件移动
	 */
	public static final void move(String src, String target) {
		final File srcFile = new File(src);
		final File targetFile = new File(target);
		if(!srcFile.renameTo(targetFile)) {
			LOGGER.warn("文件移动失败，原始文件：{}，目标文件：{}", src, target);
		}
	}
	
	/**
	 * 文件拷贝
	 */
	public static final void copy(String src, String target) {
		try(
			final var input = new BufferedInputStream(new FileInputStream(src));
			final var output = new BufferedOutputStream(new FileOutputStream(target));
		) {
			input.transferTo(output);
		} catch (IOException e) {
			LOGGER.error("文件拷贝异常", e);
		}
	}
	
	/**
	 * 文件路径
	 */
	public static final String file(String folder, String fileName) {
		if(folder == null || fileName == null) {
			throw new ArgumentException("文件路径和文件名称格式错误");
		}
		return Paths.get(folder, fileName).toString();
	}

	/**
	 * 文件大小格式化
	 */
	public static final String formatSize(Long size) {
		if(size == null || size == 0L) {
			return "0B";
		}
		int index = 0;
		BigDecimal decimal = new BigDecimal(size);
		while(decimal.longValue() >= SystemConfig.DATA_SCALE) {
			if(++index == FILE_LENGTH_UNIT.length) {
				index = FILE_LENGTH_UNIT.length - 1;
				break;
			}
			decimal = decimal.divide(new BigDecimal(SystemConfig.DATA_SCALE));
		}
		return decimal.setScale(2, RoundingMode.HALF_UP) + FILE_LENGTH_UNIT[index];
	}
	
	/**
	 * 文件大小格式化（MB）
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
	 * 获取文件大小
	 */
	public static final long fileSize(String path) {
		long size = 0L;
		final File file = new File(path);
		if(!file.exists()) {
			return 0L;
		}
		if(file.isFile()) {
			try {
				size = Files.size(Paths.get(path));
			} catch (IOException e) {
				LOGGER.error("获取文件大小异常", e);
			}
		} else {
			final File[] files = file.listFiles();
			for (File children : files) {
				size += fileSize(children.getPath());
			}
		}
		return size;
	}
	
	/**
	 * <p>创建文件夹</p>
	 * <p>如果路径是文件，则创建父文件目录，否者直接创建文件目录。</p>
	 * 
	 * @param path 路径
	 * @param file 是否是文件：true-文件；false-文件夹；
	 */
	public static final void buildFolder(String path, boolean file) {
		final File opt = new File(path);
		buildFolder(opt, file);
	}
	
	/**
	 * <p>创建文件夹</p>
	 * <p>如果路径是文件，则创建父文件目录，否者直接创建文件目录。</p>
	 * 
	 * @param opt 文件
	 * @param file 是否是文件：true-文件；false-文件夹；
	 */
	public static final void buildFolder(File opt, boolean file) {
		if(opt.exists()) {
			return;
		}
		if(file) {
			opt = opt.getParentFile();
		}
		if(!opt.exists()) {
			opt.mkdirs();
		}
	}
	
	/**
	 * MD5散列算法
	 */
	public static final Map<String, String> md5(String path) {
		return hash(path, DigestUtils.ALGO_MD5);
	}

	/**
	 * SHA-1散列算法
	 */
	public static final Map<String, String> sha1(String path) {
		return hash(path, DigestUtils.ALGO_SHA1);
	}
	
	/**
	 * <p>散列计算</p>
	 * <p>如果是单个文件计算文件散列值，如果是文件夹则计算每一个文件的散列值。</p>
	 * 
	 * @param path 文件路径
	 * @param algo 算法：MD5/SHA-1
	 * 
	 * @return 文件散列值（key：文件路径；value：散列值；）
	 */
	private static final Map<String, String> hash(String path, String algo) {
		final File file = new File(path);
		if(!file.exists()) {
			return null;
		}
		final Map<String, String> data = new HashMap<>();
		if (!file.isFile()) {
			final File[] files = file.listFiles();
			for (File children : files) {
				data.putAll(hash(children.getPath(), algo));
			}
			return data;
		} else {
			int length;
			final byte bytes[] = new byte[16 * 1024];
			final MessageDigest digest = DigestUtils.digest(algo);
			try (final var input = new BufferedInputStream(new FileInputStream(file))) {
				while ((length = input.read(bytes)) != -1) {
					digest.update(bytes, 0, length);
				}
			} catch (IOException e) {
				LOGGER.error("文件散列计算异常", e);
				return data;
			}
			data.put(path, StringUtils.hex(digest.digest()));
			return data;
		}
	}
	
	/**
	 * 在用户工作目录中获取文件
	 * 
	 * @param path 文件相对路径：以“/”开头
	 */
	public static final File userDirFile(String path) {
		return new File(SystemConfig.userDir(path));
	}

}
