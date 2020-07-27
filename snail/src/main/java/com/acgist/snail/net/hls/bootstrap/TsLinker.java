package com.acgist.snail.net.hls.bootstrap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.utils.IoUtils;

/**
 * <p>TL文件连接器</p>
 * 
 * @author acgist
 * @since 1.4.1
 */
public final class TsLinker {

	private final Logger LOGGER = LoggerFactory.getLogger(TsLinker.class);
	
	/**
	 * <p>任务名称</p>
	 */
	private final String name;
	/**
	 * <p>文件路径</p>
	 */
	private final String path;
	/**
	 * <p>文件链接列表</p>
	 */
	private final List<String> links;
	
	private TsLinker(String name, String path, List<String> links) {
		this.name = name;
		this.path = path;
		this.links = links;
	}
	
	/**
	 * <p>创建TS连接器</p>
	 * 
	 * @param name 任务名称
	 * @param path 文件路径
	 * @param links 文件链接列表
	 * 
	 * @return TS连接器
	 */
	public static final TsLinker newInstance(String name, String path, List<String> links) {
		return new TsLinker(name, path, links);
	}
	
	/**
	 * <p>连接文件</p>
	 */
	public void link() {
		final var files = this.links.stream()
			.map(link -> {
				final int index = link.lastIndexOf('/');
				if(index >= 0) {
					link = link.substring(index);
				}
				return Paths.get(this.path, link.substring(index)).toFile();
			})
			.collect(Collectors.toList());
		OutputStream output = null;
		final File target = Paths.get(this.path, name).toFile();
		try {
			output = new FileOutputStream(target);
			for (File file : files) {
				this.link(file, output);
			}
		} catch (FileNotFoundException e) {
			LOGGER.error("文件连接异常", e);
		} finally {
			IoUtils.close(output);
		}
	}
	
	/**
	 * <p>连接文件</p>
	 * 
	 * @param file 文件
	 * @param output 输出流
	 */
	private void link(File file, OutputStream output) {
		InputStream input = null;
		try {
			int length = 0;
			input = new FileInputStream(file);
			final byte[] bytes = new byte[16 * SystemConfig.ONE_KB];
			while((length = input.read(bytes)) >= 0) {
				output.write(bytes, 0, length);
			}
		} catch (IOException e) {
			LOGGER.error("文件连接异常", e);
		} finally {
			IoUtils.close(input);
		}
	}
	
}
