package com.acgist.snail.net.ftp;

import java.io.InputStream;
import java.util.stream.Stream;

import com.acgist.snail.config.SymbolConfig;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.TcpClient;
import com.acgist.snail.pojo.wrapper.URIWrapper;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>FTP客户端</p>
 * 
 * @author acgist
 */
public final class FtpClient extends TcpClient<FtpMessageHandler> {

	private static final Logger LOGGER = LoggerFactory.getLogger(FtpClient.class);
	
	/**
	 * <p>UNIX文件信息长度</p>
	 */
	private static final int UNIX_LENGTH = 9;
	/**
	 * <p>MS-DOS文件信息长度</p>
	 */
	private static final int MS_DOS_LENGTH = 4;
	/**
	 * <p>FTP默认端口：{@value}</p>
	 */
	private static final int DEFAULT_PORT = 21;
	
	/**
	 * <p>连接状态</p>
	 */
	private boolean connect = false;
	/**
	 * <p>服务器地址</p>
	 */
	private final String host;
	/**
	 * <p>服务器端口</p>
	 */
	private final int port;
	/**
	 * <p>用户账号</p>
	 */
	private final String user;
	/**
	 * <p>用户密码</p>
	 */
	private final String password;
	/**
	 * <p>文件路径</p>
	 */
	private final String filePath;

	/**
	 * @param host 服务器地址
	 * @param port 服务器端口
	 * @param user 用户账号
	 * @param password 用户密码
	 * @param filePath 文件路径
	 */
	private FtpClient(String host, int port, String user, String password, String filePath) {
		super("FTP Client", SystemConfig.CONNECT_TIMEOUT, new FtpMessageHandler());
		this.host = host;
		this.port = port;
		this.user = user;
		this.password = password;
		this.filePath = filePath;
	}
	
	/**
	 * <p>新建FTP客户端</p>
	 * 
	 * @param host 服务器地址
	 * @param port 服务器端口
	 * @param user 用户账号
	 * @param password 用户密码
	 * @param filePath 文件路径
	 * 
	 * @return FTP客户端
	 */
	public static final FtpClient newInstance(String host, int port, String user, String password, String filePath) {
		return new FtpClient(host, port, user, password, filePath);
	}

	/**
	 * <p>新建FTP客户端</p>
	 * 
	 * @param url 链接
	 * 
	 * @return FTP客户端
	 */
	public static final FtpClient newInstance(String url) {
		final URIWrapper wrapper = URIWrapper.newInstance(url, DEFAULT_PORT, SystemConfig.getFtpUser(), SystemConfig.getFtpPassword()).decode();
		return newInstance(wrapper.host(), wrapper.port(), wrapper.user(), wrapper.password(), wrapper.path());
	}
	
	@Override
	public boolean connect() {
		this.handler.resetLock();
		this.connect = this.connect(this.host, this.port);
		// 锁定：等待FTP欢迎消息
		this.handler.lock();
		if(this.connect) {
			this.login();
			if(this.handler.login()) {
				this.charset();
			} else {
				this.connect = false;
			}
		}
		return this.connect;
	}
	
	/**
	 * <p>开始下载</p>
	 * <p>默认重新下载（不用断点续传）</p>
	 * 
	 * @return 文件流
	 * 
	 * @throws NetException 网络异常
	 */
	public InputStream download() throws NetException {
		return this.download(null);
	}
	
	/**
	 * <p>开始下载</p>
	 * 
	 * @param downloadSize 断点续传位置
	 * 
	 * @return 文件流
	 * 
	 * @throws NetException 网络异常
	 */
	public InputStream download(Long downloadSize) throws NetException {
		this.checkConnect();
		synchronized (this) {
			// 切换被动模式
			this.command("PASV");
			// 设置数据模式：二进制
			this.command("TYPE I");
			if(downloadSize != null && downloadSize > 0L) {
				// 断点续传位置
				this.command("REST " + downloadSize);
			}
			// 下载文件
			this.command("RETR " + this.filePath);
			final var input = this.handler.inputStream();
			if(input == null) {
				throw new NetException(this.failMessage("打开FTP文件流失败"));
			}
			return input;
		}
	}
	
	/**
	 * <p>获取文件大小</p>
	 * <table border="1">
	 * 	<caption>FTP文件信息格式（UNIX）</caption>
	 * 	<tr>
	 * 		<th>内容</th><th>释义</th>
	 * 	</tr>
	 * 	<tr>
	 * 		<td colspan="2">-rwx------ 1 user group 102400 Jan 01 2020 SnailLauncher.exe</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>-rwx------</td>
	 * 		<td>
	 * 			首个字符：d-表示目录、--表示文件；<br>
	 * 			其他字符：r-表示可读、w-表示可写、x-表示可执行（参考Linux文件权限）；
	 * 		</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>1</td><td>位置</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>user</td><td>所属用户</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>group</td><td>所属分组</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>102400</td><td>文件大小</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>Jan 01 2020</td><td>创建时间</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>SnailLauncher.exe</td><td>文件名称</td>
	 * 	</tr>
	 * </table>
	 * <table border="1">
	 * 	<caption>FTP文件信息格式（MS-DOS）</caption>
	 * 	<tr>
	 * 		<th>内容</th><th>释义</th>
	 * 	</tr>
	 * 	<tr>
	 * 		<td colspan="2">04-08-14 03:09PM 403 SnailLauncher.exe</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>04-08-14</td><td>创建日期</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>03:09PM</td><td>创建时间</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>403</td><td>文件大小</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td>readme.txt</td><td>文件名称</td>
	 * 	</tr>
	 * </table>
	 * 
	 * @return 文件大小
	 * 
	 * @throws NetException 网络异常
	 */
	public Long size() throws NetException {
		this.checkConnect();
		synchronized (this) {
			// 切换被动模式
			this.command("PASV");
			// 切换数据模式：ASCII
			this.command("TYPE A");
			// 列出文件信息
			this.command("LIST " + this.filePath);
			final InputStream inputStream = this.handler.inputStream();
			final String message = StringUtils.ofInputStream(inputStream, this.handler.charset());
			if(message == null) {
				throw new NetException(this.failMessage("未知错误"));
			}
			// 去掉多余空格
			final String[] messages = Stream.of(message.split(SymbolConfig.Symbol.SPACE.toString()))
				.map(String::trim)
				.filter(StringUtils::isNotEmpty)
				.toArray(String[]::new);
			if(messages.length == MS_DOS_LENGTH) {
				return Long.valueOf(messages[2]);
			} else if(messages.length == UNIX_LENGTH) {
				return Long.valueOf(messages[4]);
			} else {
				throw new NetException("读取FTP文件大小失败");
			}
		}
	}
	
	/**
	 * <p>判断是否支持断点续传</p>
	 * 
	 * @return 是否支持断点续传
	 * 
	 * @throws NetException 网络异常
	 */
	public boolean range() throws NetException {
		this.checkConnect();
		return this.handler.range();
	}
	
	@Override
	public void close() {
		if(this.connect) {
			// 发送退出命令
			this.command("QUIT", false);
		}
		this.connect = false;
		super.close();
	}
	
	/**
	 * <p>登陆服务器</p>
	 */
	private void login() {
		this.command("USER " + this.user);
		this.command("PASS " + this.password);
	}
	
	/**
	 * <p>设置编码</p>
	 */
	private void charset() {
		// 列出扩展命令
		this.command("FEAT");
		if(SystemConfig.CHARSET_UTF8.equals(this.handler.charset())) {
			// 设置UTF8
			this.command("OPTS UTF8 ON");
		}
	}
	
	/**
	 * <p>发送FTP命令</p>
	 * <p>默认加锁</p>
	 * 
	 * @param command 命令
	 */
	private void command(String command) {
		this.command(command, true);
	}
	
	/**
	 * <p>发送FTP命令</p>
	 * 
	 * @param command 命令
	 * @param lock 是否加锁
	 */
	private void command(String command, boolean lock) {
		try {
			LOGGER.debug("发送FTP命令：{}", command);
			if(lock) {
				this.handler.resetLock();
				this.send(command);
				this.handler.lock();
			} else {
				this.send(command);
			}
		} catch (NetException e) {
			LOGGER.error("发送FTP命令异常：{}", command, e);
		}
	}
	
	/**
	 * <p>验证是否已经连接成功</p>
	 * 
	 * @throws NetException 网络异常
	 */
	private void checkConnect() throws NetException {
		if(!this.connect) {
			throw new NetException(this.failMessage("FTP服务器连接失败"));
		}
	}
	
	/**
	 * @param defaultMessage 默认错误信息
	 * 
	 * @return 错误信息
	 * 
	 * @see FtpMessageHandler#failMessage(String)
	 */
	private String failMessage(String defaultMessage) {
		return this.handler.failMessage(defaultMessage);
	}

}
