package com.acgist.snail.net.hls.bootstrap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.exception.ArgumentException;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>TL文件连接器</p>
 * 
 * TODO：忽略输出文件，防止重复保存
 * TODO：加密
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
		try(final var input = new FileInputStream(file)) {
			while((length = input.read(bytes)) >= 0) {
				byte[] x = aesDecry(bytes);
				output.write(x, 0, x.length);
//				output.write(bytes, 0, length);
			}
		} catch (IOException e) {
			LOGGER.error("文件连接异常", e);
		}
	}
	
	private final static String AES = "AES";
	
	static SecretKeySpec keySpec;
	static IvParameterSpec ivSpec;
	static Cipher cipher ;
	public static byte[] aesDecry(byte[] source) {
		try {
		if(keySpec == null)
		keySpec = new SecretKeySpec(Files.readAllBytes(Paths.get("E:\\tmp\\ts\\kye0")), AES);
		if(ivSpec == null)
		ivSpec = new IvParameterSpec(StringUtils.unhex("670d6f57da3d1e54a0942326147cf280"));
		if(cipher == null) {
			cipher = Cipher.getInstance("AES/CBC/NoPadding");
			cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
		}
		byte[] result = cipher.update(source);
//		byte[] result = cipher.doFinal(source);
		return result;
		} catch (Exception e) {
			throw new ArgumentException("机密失败", e);
		}
	}
	
}
