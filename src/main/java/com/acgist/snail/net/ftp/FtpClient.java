package com.acgist.snail.net.ftp;

import java.io.InputStream;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.net.TcpClient;
import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>FTP客户端</p>
 * <p>默认编码：GBK</p>
 * <p>登陆成功后会发送FEAT指令，如果服务器支持UTF8指令，使用UTF-8编码。</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class FtpClient extends TcpClient<FtpMessageHandler> {

	private static final Logger LOGGER = LoggerFactory.getLogger(FtpClient.class);
	
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
	 * <p>编码</p>
	 * <p>默认编码：GBK</p>
	 */
	private String charset = SystemConfig.CHARSET_GBK;

	private FtpClient(String host, int port, String user, String password, String filePath) {
		super("FTP Client", SystemConfig.CONNECT_TIMEOUT, new FtpMessageHandler());
		this.host = host;
		this.port = port;
		this.user = user;
		this.password = password;
		this.filePath = filePath;
	}
	
	public static final FtpClient newInstance(String host, int port, String user, String password, String filePath) {
		return new FtpClient(host, port, user, password, filePath);
	}

	@Override
	public boolean connect() {
		this.connect = connect(this.host, this.port);
		this.handler.lock(); // 锁定：等待FTP欢迎消息
		if(this.connect) {
			this.login(); // 登陆
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
	 */
	public InputStream download(Long downloadSize) throws NetException {
		if(!this.connect) {
			throw new NetException(this.failMessage("FTP服务器连接失败"));
		}
		synchronized (this) {
			changeMode();
			command("TYPE I"); // 设置数据模式：二进制
			if(downloadSize != null && downloadSize > 0L) {
				command("REST " + downloadSize); // 断点续传位置
			}
			command("RETR " + this.filePath); // 下载文件
			final var input = this.handler.inputStream(); // 文件流
			if(input == null) {
				throw new NetException(this.failMessage("打开FTP文件流失败"));
			}
			return input;
		}
	}
	
	/**
	 * @return 文件大小
	 */
	public Long size() throws NetException {
		if(!this.connect) {
			throw new NetException(this.failMessage("FTP服务器连接失败"));
		}
		synchronized (this) {
			this.changeMode();
			command("TYPE A"); // 切换数据模式：ASCII
			command("LIST " + this.filePath); // 列出文件信息
			final InputStream inputStream = this.handler.inputStream();
			final String data = StringUtils.ofInputStream(inputStream, this.charset);
			if(data == null) {
				throw new NetException(this.failMessage("未知错误"));
			}
			/*
			 * -rwx------ 1 user group 989344 Jul 01 2020 SnailLauncher.exe
			 * 第一部分：-rwx------：第一个字符：d表示目录、-表示文件，后面字符：r表示可读、w表示可写、x表示可执行
			 * 第二部分：1：未知
			 * 第三部分：user：所属用户
			 * 第四部分：group：所属分组
			 * 第五部分：989344：文件大小
			 * 第六部分：Jul 01 2020：创建时间
			 * 第七部分：SnailLauncher.exe：文件名称
			 */
			final Optional<String> optional = Stream.of(data.split(" "))
				.map(String::trim)
				.filter(StringUtils::isNotEmpty)
				.skip(4)
				.findFirst();
			if(optional.isPresent()) {
				return Long.valueOf(optional.get());
			}
			throw new NetException("读取FTP文件大小失败");
		}
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * <p>发送退出命令</p>
	 */
	@Override
	public void close() {
		if(!this.connect) {
			return;
		}
		command("QUIT", false); // 退出命令
		super.close();
	}
	
	/**
	 * @return 是否支持断点续传
	 */
	public boolean range() {
		if(!this.connect) {
			return false;
		}
		return this.handler.range();
	}
	
	/**
	 * @return 错误信息
	 */
	public String failMessage(String defaultMessage) {
		return this.handler.failMessage(defaultMessage);
	}
	
	/**
	 * <p>登陆服务器</p>
	 */
	private void login() {
		command("USER " + this.user);
		command("PASS " + this.password);
	}
	
	/**
	 * <p>设置编码</p>
	 */
	private void charset() {
		command("FEAT"); // 列出扩展命令
		this.charset = this.handler.charset();
		if(SystemConfig.CHARSET_UTF8.equals(this.charset)) {
			command("OPTS UTF8 ON"); // 设置UTF8
		}
	}
	
	/**
	 * <p>切换被动模式</p>
	 */
	private void changeMode() {
		command("PASV");
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
				send(command, this.charset);
				this.handler.lock();
			} else {
				send(command, this.charset);
			}
		} catch (NetException e) {
			LOGGER.error("发送FTP命令异常：{}", command, e);
		}
	}

}
