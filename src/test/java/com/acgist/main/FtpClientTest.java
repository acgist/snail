package com.acgist.main;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Test;

import com.acgist.snail.net.ftp.FtpManager;
import com.acgist.snail.system.exception.NetException;

public class FtpClientTest {

	@Test
	public void download() throws FileNotFoundException, IOException, NetException {
		var client = FtpManager.buildClient("ftp://localhost/FTPserver.exe");
		var ok = client.connect();
		if(!ok) {
			System.out.println("FTP服务器连接失败");
			return;
		}
		var input = client.download();
		if(input == null) {
			System.out.println(client.failMessage());
		} else {
			input.transferTo(new FileOutputStream("e://ftp"));
		}
		client.close();
		System.out.println("OK");
	}
	
}
