package com.acgist.snail.system.recycle.windows;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.system.recycle.Recycle;
import com.acgist.snail.utils.DateUtils;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.NumberUtils;

/**
 * <p>Windows回收站</p>
 * <p>支持系统：Win10</p>
 * 
 * TODO：测试win7、win xp
 * 
 * @author acgist
 * @since 1.1.0
 */
public final class WindowsRecycle extends Recycle {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(WindowsRecycle.class);

	/**
	 * <p>回收站路径：{@value}</p>
	 */
	private static final String RECYCLE_FOLDER = "$RECYCLE.BIN";
	/**
	 * <p>删除文件前缀：{@value}</p>
	 */
	private static final String FILE_PREFIX = "$R";
	/**
	 * <p>删除文件信息文件前缀：{@value}</p>
	 */
	private static final String INFO_PREFIX = "$I";
	
	/**
	 * <p>回收站路径</p>
	 */
	private String recyclePath;
	/**
	 * <p>删除文件路径</p>
	 */
	private String deleteFile;
	/**
	 * <p>删除文件信息文件路径</p>
	 */
	private String deleteInfoFile;

	public WindowsRecycle(String path) {
		super(path);
		this.buildRecycle();
		this.buildRecycleName();
	}

	/**
	 * <p>设置回收站路径</p>
	 */
	private void buildRecycle() {
		final String disk = this.path.substring(0, 1).toUpperCase(); // 盘符
		final String recycleFolder = FileUtils.file(disk + ":", RECYCLE_FOLDER);
		final File recycleFile = new File(recycleFolder);
		if(!recycleFile.exists()) {
			throw new IllegalArgumentException("回收站文件不存在：" + recycleFolder);
		}
		// 获取当前用户回收站文件目录
		final File[] files = recycleFile.listFiles();
		for (File file : files) {
			// 其他用户回收站没有权限查看：获取不到文件列表
			if(file.listFiles() != null) {
				this.recyclePath = file.getAbsolutePath();
				LOGGER.debug("回收站路径：{}", this.recyclePath);
				break;
			}
		}
	}
	
	/**
	 * <p>设置回收文件名称</p>
	 */
	private void buildRecycleName() {
		String ext = null;
		String name = NumberUtils.build().toString();
		if(this.file.isFile()) {
			ext = FileUtils.fileExt(this.path);
		}
		if(ext != null) {
			name = name + "." + ext;
		}
		this.deleteFile = FileUtils.file(this.recyclePath, FILE_PREFIX + name);
		this.deleteInfoFile = FileUtils.file(this.recyclePath, INFO_PREFIX + name);
		LOGGER.debug("删除文件路径：{}，删除文件信息文件路径：{}", this.deleteFile, this.deleteInfoFile);
	}
	
	/**
	 * <p>删除文件</p>
	 * <p>文件移动成为删除文件</p>
	 * 
	 * @return 是否删除成功
	 */
	private boolean buildFile() {
		return this.file.renameTo(new File(this.deleteFile));
	}

	/**
	 * <p>创建删除文件信息文件</p>
	 */
	private void buildInfoFile() {
		FileUtils.write(this.deleteInfoFile, this.buildInfo());
	}
	
	/**
	 * <p>创建删除文件信息</p>
	 * 
	 * @return 删除文件信息
	 */
	private byte[] buildInfo() {
		final String path = this.buildPath();
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		// 固定值
		out.write(2);
		// 固定值
		for (int index = 0; index < 15; index++) {
			out.write(0);
		}
		// 时间戳
		final long timestamp = DateUtils.windowsTimestamp();
		final ByteBuffer buffer = ByteBuffer.allocate(8);
		// 设置CPU默认大小端模式
		buffer.order(ByteOrder.nativeOrder());
		buffer.putLong(timestamp);
		for (byte value : buffer.array()) {
			out.write(value);
		}
		// 文件路径长度：固定值 + path.length();
		final char length = (char) (1 + path.length());
		this.putChar(out, length);
		// 固定值
		for (int index = 0; index < 2; index++) {
			out.write(0);
		}
		// 文件路径
		char value;
		for (int index = 0; index < path.length(); index++) {
			value = path.charAt(index);
			this.putChar(out, value);
		}
		// 固定值
		for (int index = 0; index < 2; index++) {
			out.write(0);
		}
		return out.toByteArray();
	}
	
	/**
	 * <p>写入{@code char}数据</p>
	 * <p>注意：CPU大小端</p>
	 * 
	 * @param out 字符流
	 * @param value 数据
	 */
	private void putChar(ByteArrayOutputStream out, char value) {
		if(value > 0xFF) {
			if(ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) { // 小端
				out.write(value & 0xFF);
				out.write(value >> 8 & 0xFF);
			} else { // 大端
				out.write(value >> 8 & 0xFF);
				out.write(value & 0xFF);
			}
		} else {
			if(ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) { // 小端
				out.write(value);
				out.write(0);
			} else { // 大端
				out.write(0);
				out.write(value);
			}
		}
	}

	/**
	 * <p>删除文件信息需要将斜杠转换成系统斜杠</p>
	 * 
	 * @return 删除文件路径
	 */
	private String buildPath() {
		String path = this.path;
		if(path.contains("/")) {
			path = path.replace("/", File.separator);
		}
		return path;
	}
	
	@Override
	public boolean delete() {
		LOGGER.debug("删除文件：{}", this.path);
		if(this.buildFile()) {
			this.buildInfoFile();
			return true;
		}
		return false;
	}
	
}
