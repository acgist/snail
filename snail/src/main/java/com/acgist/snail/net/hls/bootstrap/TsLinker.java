package com.acgist.snail.net.hls.bootstrap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.Cipher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.utils.FileUtils;

/**
 * <p>TL文件连接器</p>
 * 
 * @author acgist
 * @since 1.4.1
 */
public final class TsLinker {

	private static final Logger LOGGER = LoggerFactory.getLogger(TsLinker.class);
	
	/**
	 * <p>文件后缀</p>
	 */
	private static final String SUFFIX = ".ts";
	
	/**
	 * <p>任务名称</p>
	 */
	private final String name;
	/**
	 * <p>文件路径</p>
	 */
	private final String path;
	/**
	 * <p>加密套件</p>
	 */
	private final Cipher cipher;
	/**
	 * <p>文件链接列表</p>
	 */
	private final List<String> links;
	
	private TsLinker(String name, String path, Cipher cipher, List<String> links) {
		this.name = name;
		this.path = path;
		this.cipher = cipher;
		this.links = links;
	}
	
	/**
	 * <p>创建TS连接器</p>
	 * 
	 * @param name 任务名称
	 * @param path 文件路径
	 * @param cipher 加密套件
	 * @param links 文件链接列表
	 * 
	 * @return TS连接器
	 */
	public static final TsLinker newInstance(String name, String path, Cipher cipher, List<String> links) {
		return new TsLinker(name, path, cipher, links);
	}
	
	/**
	 * <p>连接文件</p>
	 * 
	 * @return 文件大小
	 */
	public long link() {
		final var files = this.links.stream()
			.map(link -> {
				final String fileName = FileUtils.fileName(link);
				return Paths.get(this.path, fileName).toFile();
			})
			.collect(Collectors.toList());
		final File target = Paths.get(this.path, this.name + SUFFIX).toFile();
		try(final var output = new FileOutputStream(target)) {
			for (File file : files) {
				this.link(file, output);
			}
		} catch (IOException e) {
			LOGGER.error("文件连接异常", e);
		}
		return FileUtils.fileSize(target.getAbsolutePath());
	}
	
	/**
	 * <p>连接文件</p>
	 * 
	 * @param file 文件
	 * @param output 输出流
	 */
	private void link(File file, OutputStream output) {
		int length = 0;
		final byte[] bytes = new byte[SystemConfig.DEFAULT_EXCHANGE_BYTES_LENGTH];
		final boolean crypt = this.cipher != null;
		try(final var input = new FileInputStream(file)) {
			while((length = input.read(bytes)) >= 0) {
				if(crypt) {
					final byte[] decrypt = this.cipher.update(bytes);
					output.write(decrypt, 0, decrypt.length);
				} else {
					output.write(bytes, 0, length);
				}
			}
		} catch (IOException e) {
			LOGGER.error("文件连接异常", e);
		}
	}
	
}
