package com.acgist.snail.system.file;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;

import com.acgist.snail.utils.DateUtils;

/**
 * 回收站
 * 
 * @author acgist
 * @since 1.1.0
 */
public class FileRecycle {

	/**
	 * 开始时间
	 */
	private static final LocalDateTime BEGIN_TIME = LocalDateTime.of(1770, 01, 01, 00, 00, 00);
	
	/**
	 * 生成文件信息
	 */
	public void buildName() {
	}
	
	/**
	 * 创建删除文件
	 */
	public void buildFile() {
	}

	/**
	 * 创建删除文件信息文件
	 */
	public void buildInfoFile() {
	}
	
	/**
	 * 创建删除文件信息：小端
	 */
	public byte[] buildInfo(String path) {
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		out.write(2); // 固定值
		for (int index = 0; index < 15; index++) { // 固定值
			out.write(0);
		}
		final long diff = DateUtils.diff(BEGIN_TIME, LocalDateTime.now());
		final long time = diff * 0x100 * 0x10000; // 换算：秒 * 0x100 * 0x1000
//		final ByteBuffer buffer = ByteBuffer.allocate(8);
//		buffer.order(ByteOrder.LITTLE_ENDIAN);
//		buffer.putLong(time);
		out.write((byte) (time & 0xFF));
		out.write((byte) (time >> 8 & 0xFF));
		out.write((byte) (time >> 16 & 0xFF));
		out.write((byte) (time >> 24 & 0xFF));
		out.write((byte) (time >> 32 & 0xFF));
		out.write((byte) (time >> 40 & 0xFF));
		out.write((byte) (time >> 48 & 0xFF));
		out.write((byte) (time >> 56 & 0xFF));
		final char length = (char) (1 + path.length()); // 固定值 + path.length();
		if(length > 0xFF) {
			out.write(length & 0xFF);
			out.write(length >> 8 & 0xFF);
		} else {
			out.write(length);
			out.write(0);
		}
		for (int index = 0; index < 2; index++) { // 固定值
			out.write(0);
		}
		char value;
		for (int index = 0; index < path.length(); index++) {
			value = path.charAt(index);
			if(value > 0xFF) {
				out.write(value & 0xFF);
				out.write(value >> 8 & 0xFF);
			} else {
				out.write(value);
				out.write(0);
			}
		}
		for (int index = 0; index < 2; index++) { // 固定值
			out.write(0);
		}
		return out.toByteArray();
	}
	
}
