package com.acgist.snail.ftp;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Test;

import com.acgist.snail.net.ftp.bootstrap.FtpClientBuilder;
import com.acgist.snail.system.exception.NetException;

public class FtpClientTest {

	@Test
	public void download() throws FileNotFoundException, IOException, NetException {
//		var client = FtpClientBuilder.newInstance("ftp://localhost/elk/elasticsearch-6.4.1.zip").build();
//		var client = FtpClientBuilder.newInstance("ftp://localhost/VS2012中文旗舰版/vs_ultimate.exe").build();
		var client = FtpClientBuilder.newInstance("ftp://localhost/FTPserver.exe").build();
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
