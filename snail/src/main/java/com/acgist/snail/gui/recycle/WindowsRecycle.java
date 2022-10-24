package com.acgist.snail.gui.recycle;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Paths;

import com.acgist.snail.config.SymbolConfig;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.utils.DateUtils;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.NumberUtils;

/**
 * <p>Windows回收站</p>
 * <p>支持系统：Win10</p>
 * 
 * @author acgist
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
	 * <p>删除信息文件前缀：{@value}</p>
	 */
	private static final String INFO_PREFIX = "$I";
	
	/**
	 * <p>回收站路径</p>
	 */
	private String recyclePath;
	/**
	 * <p>删除文件路径</p>
	 */
	private String deleteFilePath;
	/**
	 * <p>删除信息文件路径</p>
	 */
	private String deleteInfoFilePath;

	/**
	 * <p>Windows回收站</p>
	 * 
	 * @param path 文件路径
	 */
	public WindowsRecycle(String path) {
		super(path);
		this.buildRecycle();
		this.buildRecycleInfo();
	}

	/**
	 * <p>设置回收站路径</p>
	 */
	private void buildRecycle() {
		// 获取盘符
		final String disk = this.disk(this.path);
		// 获取回收站上级目录
		final String recycleFolder = FileUtils.file(disk, RECYCLE_FOLDER);
		final File recycleFile = new File(recycleFolder);
		if(!recycleFile.exists()) {
			throw new IllegalArgumentException("回收站上级目录不存在：" + recycleFolder);
		}
		// 获取当前用户回收站文件目录
		final File[] files = recycleFile.listFiles();
		for (File file : files) {
			// 获取不到回收站文件列表：其他用户没有权限查看
			if(file.listFiles() != null) {
				this.recyclePath = file.getAbsolutePath();
				LOGGER.debug("回收站路径：{}", this.recyclePath);
				break;
			}
		}
	}
	
	/**
	 * <p>获取盘符</p>
	 * 
	 * @param filePath 文件路径
	 * 
	 * @return 盘符
	 */
	private String disk(String filePath) {
		final int diskIndex = filePath.indexOf(SymbolConfig.Symbol.COLON.toChar());
		if(diskIndex < 0) {
			// 相对路径
			final String absolutePath = Paths.get(filePath).toFile().getAbsolutePath();
			return this.disk(absolutePath);
		}
		return filePath.substring(0, diskIndex + 1).toUpperCase();
	}
	
	/**
	 * <p>设置删除信息</p>
	 */
	private void buildRecycleInfo() {
		String name = NumberUtils.build().toString();
		if(this.file.isFile()) {
			final String ext = FileUtils.fileExt(this.path);
			if(ext != null) {
				name = SymbolConfig.Symbol.DOT.join(name, ext);
			}
		}
		this.deleteFilePath = FileUtils.file(this.recyclePath, FILE_PREFIX + name);
		this.deleteInfoFilePath = FileUtils.file(this.recyclePath, INFO_PREFIX + name);
		LOGGER.debug("""
			删除文件路径：{}
			删除信息文件路径：{}""",
			this.deleteFilePath,
			this.deleteInfoFilePath
		);
	}
	
	/**
	 * <p>删除文件</p>
	 * <p>文件移动成为删除文件</p>
	 * 
	 * @return 是否删除成功
	 */
	private boolean buildFile() {
		return this.file.renameTo(new File(this.deleteFilePath));
	}

	/**
	 * <p>新建删除信息文件</p>
	 */
	private void buildDeleteInfoFile() {
		try {
			FileUtils.write(this.deleteInfoFilePath, this.buildDeleteInfo());
		} catch (IOException e) {
			LOGGER.error("新建删除信息文件异常", e);
		}
	}
	
	/**
	 * <p>新建删除信息</p>
	 * 
	 * @return 删除信息
	 * 
	 * @throws IOException IO异常
	 */
	private byte[] buildDeleteInfo() throws IOException {
		final ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		final String path = FileUtils.systemSeparator(this.path);
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		// 设置大小端：默认CPU
		buffer.order(ByteOrder.nativeOrder());
		// 固定值
		out.write(2);
		// 固定值
		out.write(new byte[7]);
		// 设置文件大小
		buffer.putLong(FileUtils.fileSize(this.deleteFilePath));
		out.write(buffer.array());
		buffer.clear();
		// 设置删除时间戳
		buffer.putLong(DateUtils.windowsTimestamp());
		out.write(buffer.array());
		buffer.clear();
		// 固定值 + 文件路径长度
		final char length = (char) (1 + path.length());
		this.buildInfoChar(out, length);
		// 固定值
		out.write(new byte[2]);
		// 设置文件路径
		for (int index = 0; index < path.length(); index++) {
			this.buildInfoChar(out, path.charAt(index));
		}
		// 固定值
		out.write(new byte[2]);
		return out.toByteArray();
	}
	
	/**
	 * <p>写入删除信息数据</p>
	 * 
	 * @param out 字符流
	 * @param value 数据
	 */
	private void buildInfoChar(ByteArrayOutputStream out, char value) {
//		Character.reverseBytes(value);
		if(ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
			// 小端
			out.write(value & 0xFF);
			out.write(value >> 8 & 0xFF);
		} else {
			// 大端
			out.write(value >> 8 & 0xFF);
			out.write(value & 0xFF);
		}
	}
	
	@Override
	public boolean delete() {
		LOGGER.info("删除文件：{}", this.path);
		if(this.buildFile()) {
			this.buildDeleteInfoFile();
			return true;
		}
		return false;
	}
	
}
