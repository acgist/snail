package com.acgist.snail.utils;

import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.config.FileTypeConfig;
import com.acgist.snail.system.config.FileTypeConfig.FileType;

/**
 * utils - 文件
 */
public class FileUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);
	
	private static final int SIZE_SCALE = 1024;
	private static final String[] SIZE_UNIT = {"B", "KB", "M", "G", "T"};
	private static final String FILENAME_REPLACE_CHAR = "";
	private static final String FILENAME_REPLACE_REGEX = "[\\\\/:\\*\\?\\<\\>\\|]"; // 替换的字符：\、/、:、*、?、<、>、|
	
	/**
	 * 删除文件
	 */
	public static final void delete(String path) {
		if(StringUtils.isEmpty(path)) {
			return;
		}
		File file = new File(path);
		if(!file.exists()) {
			LOGGER.warn("删除文件不存在：{}", path);
			return;
		}
		LOGGER.info("删除文件：{}", path);
		delete(file);
	}

	/**
	 * 递归删除文件
	 */
	private static final void delete(File file) {
		if(file.isDirectory()) {
			File[] files = file.listFiles();
			for (File children : files) {
				delete(children);
			}
			file.delete();
		} else {
			file.delete();
		}
	}
	
	/**
	 * 获取文件路径：如果文件路径存在返回文件路径，如果不存在获取user.dir路径+文件路径
	 */
	public static final String folderPath(String path) {
		File file = new File(path);
		if(file.exists()) {
			return path;
		}
		path = System.getProperty("user.dir") + path;
		file = new File(path);
		if(file.exists()) {
			return path;
		}
		file.mkdirs();
		return path;
	}
	
	/**
	 * 获取文件名称：URL
	 */
	public static final String fileNameFromUrl(final String url) {
		if(StringUtils.isEmpty(url)) {
			return url;
		}
		String fileName = UrlUtils.decode(url);
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
	 * 文件名称获取后缀
	 */
	public static final FileType fileType(String name) {
		if(StringUtils.isEmpty(name)) {
			return FileType.unknown;
		}
		String ext = name;
		int index = name.lastIndexOf(".");
		if(index != -1) {
			ext = name.substring(index + 1);
		}
		return FileTypeConfig.type(ext);
	}
	
	/**
	 * 获取正确的文件下载名称：去掉不支持的字符串
	 */
	public static final String fileName(String name) {
		if(StringUtils.isNotEmpty(name)) { // 去掉不支持的字符
			name = name.replaceAll(FILENAME_REPLACE_REGEX, FILENAME_REPLACE_CHAR);
		}
		if(StringUtils.isEmpty(name)) { // 默认使用日期
			name = UniqueCodeUtils.build();
		}
		return name.trim();
	}
	
	/**
	 * 资源管理器中打开文件
	 */
	public static final void openInDesktop(File file) {
		try {
			Desktop.getDesktop().open(file);
		} catch (IOException e) {
			LOGGER.error("打开系统目录异常", e);
		}
	}

	/**
	 * 文件写入
	 */
	public static final void write(String filePath, byte[] bytes) {
		try(OutputStream output = new FileOutputStream(filePath)) {
			output.write(bytes);
		} catch (IOException e) {
			LOGGER.error("文件写入异常", e);
		}
	}
	
	/**
	 * 文件拷贝
	 */
	public static final void copy(String src, String target) {
		try(
			InputStream input = new BufferedInputStream(new FileInputStream(src));
			OutputStream output = new BufferedOutputStream(new FileOutputStream(target));
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
			LOGGER.error("不正确的文件路径，目录：{}，文件：{}", folder, fileName);
			throw new IllegalArgumentException("不正确的文件路径");
		}
		return folder + File.separator + fileName;
	}

	/**
	 * 大小计算
	 */
	public static final String formatSize(Long size) {
		if(size == null || size == 0L) {
			return "0B";
		}
		int index = 0;
		BigDecimal decimal = new BigDecimal(size);
		while(decimal.longValue() > SIZE_SCALE) {
			if(++index == SIZE_UNIT.length) {
				index = SIZE_UNIT.length - 1;
				break;
			}
			decimal = decimal.divide(new BigDecimal(SIZE_SCALE));
		}
		return decimal.setScale(2, RoundingMode.HALF_UP) + SIZE_UNIT[index];
	}
	
	/**
	 * 获取文件大小
	 */
	public static final long fileSize(String path) {
		File file = new File(path);
		if(!file.exists()) {
			return 0L;
		}
		try {
			return Files.size(Paths.get(path));
		} catch (IOException e) {
			LOGGER.error("获取文件大小异常", e);
		}
		return 0L;
	}
	
}
