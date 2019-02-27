package com.acgist.snail.utils;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.module.config.FileTypeConfig;
import com.acgist.snail.module.config.FileTypeConfig.FileType;

/**
 * 文件工具
 */
public class FileUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);
	
	private static final String FILENAME_REPLACE_CHAR = "";
	private static final String FILENAME_REPLACE_REGEX = "[\\\\/:\\*\\?\\<\\>\\|]"; // 替换的字符：\、/、:、*、?、<、>、|
	private static final String CONTENT_DISPOSITION = "Content-Disposition";
	
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
		boolean ok = file.delete();
		if(!ok) {
			LOGGER.warn("删除文件失败：{}", path);
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
	public static final String fileNameFromUrl(String url) {
		if(StringUtils.isEmpty(url)) {
			return url;
		}
		url = UrlUtils.decode(url);
		if(url.contains("\\")) {
			url = url.replace("\\", "/");
		}
		int index = url.lastIndexOf("/");
		if(index != -1) {
			url = url.substring(index + 1);
		}
		index = url.indexOf("?");
		if(index != -1) {
			url = url.substring(0, index);
		}
		return url;
	}
	
	/**
	 * 获取文件名称：HTTP
	 */
	public static final String fileNameFromHttp(String url) {
		HttpClient client = HttpUtils.newClient();
		HttpRequest request = HttpUtils.newRequest(url)
			.method("HEAD", BodyPublishers.noBody())
			.build();
		Optional<String> header = null;
		HttpResponse<String> response = HttpUtils.request(client, request, BodyHandlers.ofString());
		if(HttpUtils.ok(response)) {
			header = response.headers().firstValue(CONTENT_DISPOSITION);
		}
		if(header != null && header.isPresent()) {
			String fileName = header.get();
			final String fileNameLower = fileName.toLowerCase();
			if(fileNameLower.contains("filename")) {
				int index = fileName.indexOf("=");
				if(index != -1) {
					fileName = fileName.substring(index + 1);
					index = fileName.indexOf("?");
					if(index != -1) {
						fileName = fileName.substring(0, index);
					}
					return fileName;
				}
			}
		}
		return FileUtils.fileNameFromUrl(url);
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
		try(OutputStream output = new FileOutputStream(new File(filePath))) {
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
			InputStream input = new FileInputStream(src);
			OutputStream output = new FileOutputStream(target);
		) {
			int index;
			byte[] bytes = new byte[1024];
			while((index = input.read(bytes)) != -1) {
				output.write(bytes, 0, index);
			}
		} catch (IOException e) {
			LOGGER.error("文件拷贝异常", e);
		}
	}
	
}
