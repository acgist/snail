package com.acgist.main;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Test;

import com.acgist.snail.net.ftp.FtpClientFactory;
import com.acgist.snail.system.exception.NetException;

public class FtpClientTest {

	@Test
	public void download() throws FileNotFoundException, IOException, NetException {
//		var client = FtpClientFactory.buildClient("ftp://localhost/elk/elasticsearch-6.4.1.zip");
//		var client = FtpClientFactory.buildClient("ftp://localhost/VS2012中文旗舰版/vs_ultimate.exe");
		var client = FtpClientFactory.buildClient("ftp://localhost/FTPserver.exe");
		var ok = client.connect();
		if(!ok) {
			System.out.println("FTP服务器连接失败");
			return;
		}
		System.out.println(client.size());
		var input = client.download();
		if(input == null) {
			System.out.println(client.failMessage());
		} else {
			var output = new FileOutputStream("e://ftp");
			input.transferTo(output);
			output.flush();
			output.close();
		}
		client.close();
		System.out.println("OK");
	}
	
}
