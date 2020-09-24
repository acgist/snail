package com.acgist.snail;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import com.acgist.snail.recycle.RecycleManager;
import com.acgist.snail.utils.StringUtils;

public class RecycleTest extends BaseTest {

//	02000000000000000000000000000000c03276c89237d5010d00000045003a005c004b6dd58b5c00440044002e007400780074000000
//	0200000000000000000000000000000040660eeb9237d5010d00000045003a005c004b6dd58b5c00440044002e007400780074000000
	@Test
	public void testReadInfo() throws IOException {
		byte[] bytes = Files.readAllBytes(Paths.get(URI.create("file:///E:/$RECYCLE.BIN/S-1-5-21-1082702080-4186364021-1016170526-1001/$I8VSPPW.txt")));
		this.log(bytes.length);
		System.err.println(StringUtils.hex(bytes));
	}
	
	@Test
	public void testRecycleWindow() {
		File file = new File("E:/$RECYCLE.BIN/");
		File[] files = file.listFiles();
		for (File recycle : files) {
			if(recycle.listFiles() == null) {
				this.log("非当前用户：" + recycle);
			} else {
				this.log("当前用户：" + recycle);
			}
		}
	}
	
	@Test
	public void testDelete() {
		RecycleManager.newInstance("E:/DD").delete();
//		RecycleManager.newInstance("E:/DD.txt").delete();
//		RecycleManager.newInstance("E:/测试/DD.txt").delete();
	}
	
}
