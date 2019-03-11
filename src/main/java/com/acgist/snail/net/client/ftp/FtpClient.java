package com.acgist.snail.net.client.ftp;

import com.acgist.snail.net.client.AbstractClient;
import com.acgist.snail.net.message.impl.FtpMessageHandler;

public class FtpClient extends AbstractClient {
	
	public FtpClient() {
		super(FtpMessageHandler.SPLIT);
	}

	@Override
	public void connect() {
	}
	
	@Override
	public void connect(String host, int port) {
	}

	/**
	 * 发送命令
	 */
	private void command(String command) {
		send(command);
	}
	
	public static void main(String[] args) throws InterruptedException {
		FtpClient c = new FtpClient();
		c.connect("192.168.43.10", 21, new FtpMessageHandler());
		c.command("USER anonymous");
		c.command("PASS anonymous");
		c.command("cwd /elk");
		c.command("cwd /elk/fds");
		c.command("cwd /");
		c.command("PASV");
		Thread.sleep(1000000);
	}

}
