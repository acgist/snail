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
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.config.FileTypeConfig;
import com.acgist.snail.system.config.FileTypeConfig.FileType;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.exception.ArgumentException;
import com.acgist.snail.system.recycle.RecycleManager;

/**
 * <p>文件工具</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public class FileUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);
	
	/**
	 * 文件进制
	 */
	private static final int FILE_SIZE_SCALE = 1024;
	/**
	 * 文件大小单位
	 */
	private static final String[] FILE_SIZE_UNIT = {"B", "KB", "M", "G", "T"};
	/**
	 * 文件名禁用的字符：\、/、:、*、?、<、>、|
	 */
	private static final String FILENAME_REPLACE_REGEX = "[\\\\/:\\*\\?\\<\\>\\|]";
	/**
	 * 文件禁用字符替换字符
	 */
	private static final String FILENAME_REPLACE_CHAR = "";
	
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
			LOGGER.warn("文件删除失败：{}", file.getAbsolutePath());
		}
	}
	
	/**
	 * 删除文件至回收站
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
	 * <p>去掉协议、域名、路径、参数。</p>
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
	 * 获取正确的文件下载名称：去掉{@linkplain #FILENAME_REPLACE_REGEX 不支持的字符串}。
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
	 * 根据文件名称获取文件类型。
	 */
	public static final FileType fileType(String fileName) {
		final String ext = ext(fileName);
		return FileTypeConfig.type(ext);
	}
	
	/**
	 * 获取文件后缀
	 */
	public static final String ext(String fileName) {
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
	 * 资源管理器中打开文件。
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
			LOGGER.warn("文件移动失败，源文件：{}，目标文件：{}", src, target);
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
			LOGGER.error("文件路径和文件名称格式错误，目录：{}，文件：{}", folder, fileName);
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
		while(decimal.longValue() >= FILE_SIZE_SCALE) {
			if(++index == FILE_SIZE_UNIT.length) {
				index = FILE_SIZE_UNIT.length - 1;
				break;
			}
			decimal = decimal.divide(new BigDecimal(FILE_SIZE_SCALE));
		}
		return decimal.setScale(2, RoundingMode.HALF_UP) + FILE_SIZE_UNIT[index];
	}
	
	/**
	 * 文件大小
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
				LOGGER.error("文件大小获取异常", e);
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
	 * 创建文件夹，如果路径是文件，则创建父目录，否者直接创建路径目录。
	 * 
	 * @param path 路径
	 * @param file 是否是文件：true-文件；false-文件夹
	 */
	public static final void buildFolder(String path, boolean file) {
		final File opt = new File(path);
		buildFolder(opt, file);
	}
	
	/**
	 * 创建文件夹，如果路径是文件，则创建父目录，否者直接创建路径目录。
	 * 
	 * @param opt 文件
	 * @param file 是否是文件：true-文件；false-文件夹
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
	 * 散列计算
	 * 
	 * @param path 文件地址，如果是目录计算里面每一个文件
	 * @param algo 算法：MD5/SHA-1
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
				LOGGER.error("文件HASH计算异常", e);
				return data;
			}
			data.put(path, StringUtils.hex(digest.digest()));
			return data;
		}
	}
	
	/**
	 * 在用户工作目录中获取文件
	 * 
	 * @param path 文件相对路径：以/开头
	 */
	public static final File userDirFile(String path) {
		return new File(SystemConfig.userDir(path));
	}

	/**
	 * MD5散列算法
	 */
	public static final Map<String, String> md5(String path) {
		return hash(path, "MD5");
	}

	/**
	 * SHA-1散列算法
	 */
	public static final Map<String, String> sha1(String path) {
		return hash(path, "SHA-1");
	}
	
}
