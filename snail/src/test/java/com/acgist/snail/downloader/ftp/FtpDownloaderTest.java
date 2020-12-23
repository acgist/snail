package com.acgist.snail.downloader.ftp;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.protocol.ftp.FtpProtocol;
import com.acgist.snail.utils.FileUtils;
import com.acgist.snail.utils.Performance;

public class FtpDownloaderTest extends Performance {

	@Test
	public void testFtpDownloader() throws DownloadException {
		final String url = "ftp://localhost/ftp/FTPserver.exe";
//		final String url = "ftp://demo:password@test.rebex.net/readme.txt";
		final var taskSession = FtpProtocol.getInstance().buildTaskSession(url);
		final var downloader = FtpDownloader.newInstance(taskSession);
		downloader.run();
		final var file = new File(taskSession.getFile());
		assertTrue(file.exists());
		FileUtils.delete(taskSession.getFile());
	}
	
}
