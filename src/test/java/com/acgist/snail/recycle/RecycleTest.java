package com.acgist.snail.recycle;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

import com.acgist.snail.system.recycle.RecycleManager;
import com.acgist.snail.system.recycle.window.WindowRecycle;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.StringUtils;

public class RecycleTest {

	@Test
	public void deleteOld() {
		File file = Paths.get(URI.create("file:///E:/$RECYCLE.BIN/S-1-5-21-1082702080-4186364021-1016170526-1001/$IDIQDFV.txt")).toFile();
		file.delete();
	}

	@Test
	public void recycle() throws IOException {
		File da = new File("E:/$RECYCLE.BIN/S-1-5-21-1082702080-4186364021-1016170526-1001/$Itest1234.txt");
		File db = new File("E:/$RECYCLE.BIN/S-1-5-21-1082702080-4186364021-1016170526-1001/$Rtest1234.txt");
		da.delete();
		db.delete();
		WindowRecycle recycle = new WindowRecycle("E:\\AA.txt");
		byte[] bytes = recycle.buildInfo();
		System.out.println(StringUtils.hex(bytes));
		File file = new File("E://AA.txt");
		File info = new File("E:/$RECYCLE.BIN/S-1-5-21-1082702080-4186364021-1016170526-1001/$Itest1234.txt");
		if(file.exists()) {
			FileUtils.write(info.getPath(), bytes);
			boolean ok = file.renameTo(new File("E:/$RECYCLE.BIN/S-1-5-21-1082702080-4186364021-1016170526-1001/$Rtest1234.txt"));
			System.out.println("移动结果：" + ok);
		} else {
			System.out.println("文件不存在");
		}
	}
	
//	02000000000000000000000000000000c03276c89237d5010d00000045003a005c004b6dd58b5c00440044002e007400780074000000
//	0200000000000000000000000000000040660eeb9237d5010d00000045003a005c004b6dd58b5c00440044002e007400780074000000
	@Test
	public void readInfo() throws IOException {
		byte[] bytes = Files.readAllBytes(Paths.get(URI.create("file:///E:/$RECYCLE.BIN/S-1-5-21-1082702080-4186364021-1016170526-1001/$I8VSPPW.txt")));
		System.out.println(bytes.length);
		System.err.println(StringUtils.hex(bytes));
	}
	
	@Test
	public void changeDate() {
		WindowRecycle recycle = new WindowRecycle("E:\\AA.txt");
		byte[] bytes = recycle.buildInfo();
		System.out.println(StringUtils.hex(bytes));
		File info = new File("E:/$RECYCLE.BIN/S-1-5-21-1082702080-4186364021-1016170526-1001/$I0W1UDC.txt");
		FileUtils.write(info.getPath(), bytes);
	}
	
	@Test
	public void recycleWindow() {
		File file = new File("E:/$RECYCLE.BIN/");
		File[] files = file.listFiles();
		for (File f : files) {
			System.out.println(f);
		}
	}
	
	@Test
	public void delete() {
		RecycleManager.newInstance("E:/DD").delete();
//		RecycleManager.newInstance("E:/DD.txt").delete();
//		RecycleManager.newInstance("E:/测试/DD.txt").delete();
	}
	
}
