package com.acgist.snail.net.quick;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.QuickConfig;
import com.acgist.snail.net.NetException;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.Performance;

class QuickClientTest extends Performance {

	@Test
	void testTransport() throws NetException {
		final String file = "D:/tmp/snail/video.mp4";
		final String download = "D:/download/video.mp4";
//		final String file = "D:/软件/graalvm-ce-java17-windows-amd64-22.3.0.zip";
//		final String download = "D:/download/graalvm-ce-java17-windows-amd64-22.3.0.zip";
		final QuickClient client = new QuickClient();
		client.connect("host:localhost:18888", v -> {});
		client.quick(new File(file), progress -> {});
		client.close();
		assertEquals(FileUtils.md5(file), FileUtils.md5(download));
	}

	@Test
	void testCopy() throws IOException {
		final String file = "D:/软件/graalvm-ce-java17-windows-amd64-22.3.0.zip";
		final String download = "D:/download/graalvm-ce-java17-windows-amd64-22.3.0.zip";
//		Files.copy(Paths.get(file), Paths.get(download));
		final FileInputStream input = new FileInputStream(file);
		final FileOutputStream output = new FileOutputStream(download);
		final byte[] bytes = new byte[QuickConfig.PACKET_MAX_LENGTH];
		int index;
		while((index = input.read(bytes)) >= 0) {
			output.write(bytes, 0, index);
		}
		input.close();
		output.close();
	}
	
}
