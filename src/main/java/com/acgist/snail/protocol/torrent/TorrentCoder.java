package com.acgist.snail.protocol.torrent;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.acgist.snail.pojo.wrapper.TorrentWrapper;
import com.acgist.snail.protocol.torrent.bean.InfoHash;
import com.acgist.snail.protocol.torrent.bean.Torrent;
import com.acgist.snail.protocol.torrent.bean.TorrentInfo;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.utils.StringUtils;

/**
 * 种子解析器
 */
public class TorrentCoder {
	
	private Torrent torrent = null; // 种子文件信息
	private InfoHash infoHash = null; // infoHash
	
	private TorrentCoder() {
	}
	
	/**
	 * 解析种子文件
	 */
	public static final TorrentCoder newInstance(String filePath) throws DownloadException {
		TorrentCoder decoder = new TorrentCoder();
		try(InputStream input = new FileInputStream(filePath)) {
			decoder.decode(input);
		} catch (Exception e) {
			throw new DownloadException("解析种子异常", e);
		}
		return decoder;
	}
	
	/**
	 * 获取磁力链接HASH
	 */
	public String hash() {
		return infoHash.hashHex();
	}
	
	/**
	 * 获取种子信息
	 */
	public TorrentWrapper wrapper() throws DownloadException {
		return TorrentWrapper.newInstance(torrent);
	}
	
	/**
	 * 编码：http://blog.sina.com.cn/s/blog_ec8c9eae0102wa9p.html
	 * i e：long
	 * l e：list
	 * d e：map
	 */
	private void decode(InputStream input) throws NumberFormatException, IOException, Exception {
		int index;
		char indexChar;
		String key = null;
		Torrent torrent = new Torrent();
		StringBuilder lengthBuilder = new StringBuilder();
		InfoHashBuilder infoHashBuilder = InfoHashBuilder.newInstance();
		while ((index = input.read()) != -1) {
			infoHashBuilder.build(key, index);
			indexChar = (char) index;
			switch (indexChar) {
				case 'i':
					StringBuilder valueBuilder = new StringBuilder();
					while((index = input.read()) != -1) {
						infoHashBuilder.build(key, index);
						indexChar = (char) index;
						if(indexChar == 'e') {
							break;
						} else {
							valueBuilder.append(indexChar);
						}
					}
					torrent.setValue(key, valueBuilder.toString());
					break;
				case 'l':
				case 'd':
					break;
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
					lengthBuilder.append(indexChar);
					break;
				case ':':
					int length = Integer.parseInt(lengthBuilder.toString());
					lengthBuilder.setLength(0);
					byte[] bytes = new byte[length];
					input.read(bytes);
					infoHashBuilder.build(key, bytes);
					String value = new String(bytes);
					if (Torrent.infoKeys().contains(value)) { // 初始化
						key = value;
						if (value.equals("info")) {
							torrent.setInfo(new TorrentInfo());
						} else if (value.equals("files")) {
							torrent.newTorrentFile();
						}
					} else {
						if(key.equals("ed2k") || key.equals("pieces") || key.equals("filehash")) {
							torrent.setValue(key, bytes);
						} else {
							torrent.setValue(key, value);
						}
					}
					break;
			}
		}
		this.torrent = torrent;
		this.infoHash = infoHashBuilder.buildInfoHash();
	}
	
	/**
	 * 验证BT种子
	 */
	public static final boolean verify(String url) {
		return StringUtils.endsWith(url.toLowerCase(), TorrentProtocol.TORRENT_SUFFIX);
	}
	
}
