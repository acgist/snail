package com.acgist.snail.net.ftp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

/**
 * FTP客户端，主要实现下载功能，不实现其他功能
 */
public class FtpClient {

	private Socket socket = null;
	private BufferedReader reader = null;
	private BufferedWriter writer = null;

	public void connect(String host) throws IOException {
		connect(host, 21);
	}

	public void connect(String host, int port) throws IOException {
		connect(host, port, "anonymous", "anonymous");
	}

	public void connect(String host, int port, String user, String pass) {
		try {
			socket = new Socket(host, port);
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			String line = reader.readLine();
			System.out.println("step1 ----- " + line);

			// input user
			sendCommand("USER " + user);
//			line = reader.readLine();
			System.out.println("step2 -----" + line);

			// input pwd
			sendCommand("PASS " + pass);
//			line = reader.readLine();
			System.out.println("step3 -----" + line);

		} catch (UnknownHostException ex) {
			System.out.println("Couldn't find the Ftp Server");
		} catch (IOException ex) {
			System.out.println("IOException");
		}
	}
	
	public void disconnect() throws IOException {

		try {
			sendCommand("QUIT");
			System.out.println("last step ----- " + reader.readLine());
		} finally {
			socket = null;
		}
	}

	public void listFiles(String serverPath) throws IOException {

		writer.write("cwd  " + serverPath + "/r/n"); // 若要指定某一位置就修改 caches
		writer.flush();
		System.out.println(reader.readLine());

		sendCommand("PASV");

		String response = reader.readLine();
		String ip = null;
		int port1 = -1;
		int opening = response.indexOf('(');
		int closing = response.indexOf(')', opening + 1);

		if (closing > 0) {
			String dataLink = response.substring(opening + 1, closing);
			StringTokenizer tokenizer = new StringTokenizer(dataLink, ",");
			try {
				ip = tokenizer.nextToken() + "." + tokenizer.nextToken() + "." + tokenizer.nextToken() + "."
					+ tokenizer.nextToken();
				port1 = Integer.parseInt(tokenizer.nextToken()) * 256 + Integer.parseInt(tokenizer.nextToken());
			} catch (Exception e) {
				throw new IOException("SimpleFTP received bad data link information: " + response);
			}
		}

		System.out.println(ip + "  " + port1);

		writer.write("LIST  " + "/r/n");
		writer.flush();
		Socket dataSocket = new Socket(ip, port1);
		reader.readLine();

		DataInputStream dis = new DataInputStream(dataSocket.getInputStream());
		String s = "";
		while ((s = dis.readLine()) != null) {
			String l = new String(s.getBytes("ISO-8859-1"), "utf-8");
			System.out.println(l);
		}
		dis.close();
		dataSocket.close();

		reader.readLine();
	}

	public boolean upload(String lfilepath, String serverPath) throws IOException {
		File file = new File(lfilepath);

		if (file.isDirectory()) {
			throw new IOException("SimpleFTP cannot upload a directory.");
		}

		String filename = file.getName();
		BufferedInputStream input = new BufferedInputStream(new FileInputStream(file));

		writer.write("cwd  " + serverPath + "/r/n"); // 若要指定某一位置就修改 caches
		writer.flush();
		System.out.println(reader.readLine());

		sendCommand("PASV");
		String response = reader.readLine();
		if (!response.startsWith("227 ")) {
			throw new IOException("SimpleFTP could not request passive mode: " + response);
		}

		String ip = null;
		int port = -1;
		int opening = response.indexOf('(');
		int closing = response.indexOf(')', opening + 1);
		if (closing > 0) {
			String dataLink = response.substring(opening + 1, closing);
			StringTokenizer tokenizer = new StringTokenizer(dataLink, ",");
			try {
				ip = tokenizer.nextToken() + "." + tokenizer.nextToken() + "." + tokenizer.nextToken() + "."
					+ tokenizer.nextToken();
				port = Integer.parseInt(tokenizer.nextToken()) * 256 + Integer.parseInt(tokenizer.nextToken());
			} catch (Exception e) {
				throw new IOException("SimpleFTP received bad data link information: " + response);
			}
		}

		System.out.println(ip + "  " + port);

		sendCommand("STOR " + filename);

		Socket dataSocket = new Socket(ip, port);

		response = reader.readLine();

		BufferedOutputStream output = new BufferedOutputStream(dataSocket.getOutputStream());
		byte[] buffer = new byte[4096];
		int bytesRead = 0;
		while ((bytesRead = input.read(buffer)) != -1) {
			output.write(buffer, 0, bytesRead);
		}
		output.flush();
		output.close();
		input.close();

		response = reader.readLine();
		return response.startsWith("226 ");
	}

	private void sendCommand(String com) throws IOException {

		if (socket == null) {
			throw new IOException("SimpleFTP is not connected.");
		}

		try {
			writer.write(com + "/r/n");
			writer.flush();
System.out.println(com);
		} catch (IOException e) {
			socket = null;
			throw e;
		}
	}

	public static void main(String args[]) throws IOException {
		String host = "192.168.1.100";
		int port = 21;
		String uname = "test";
		String pwd = "123456";

		FtpClient fr = new FtpClient();
		fr.connect(host, port);
		fr.listFiles("/fliqlo_133.zip");
		// fr.upload("D://1400x1050-asia-emea.jpg");

		fr.disconnect();
	}

}
