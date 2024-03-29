package com.acgist.snail.net.ftp;

import java.io.InputStream;
import java.util.stream.Stream;

import com.acgist.snail.config.SymbolConfig;
import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.wrapper.URIWrapper;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.NetException;
import com.acgist.snail.net.TcpClient;
import com.acgist.snail.utils.StringUtils;

/**
 * FTP客户端
 * 
 * @author acgist
 */
public final class FtpClient extends TcpClient<FtpMessageHandler> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FtpClient.class);
    
    /**
     * UNIX文件信息长度
     */
    private static final int UNIX_LENGTH = 9;
    /**
     * MS-DOS文件信息长度
     */
    private static final int MS_DOS_LENGTH = 4;
    /**
     * FTP默认端口：{@value}
     */
    private static final int DEFAULT_PORT = 21;
    
    /**
     * 连接状态
     */
    private boolean connect = false;
    /**
     * 服务器地址
     */
    private final String host;
    /**
     * 服务器端口
     */
    private final int port;
    /**
     * 用户账号
     */
    private final String user;
    /**
     * 用户密码
     */
    private final String password;
    /**
     * 文件路径
     */
    private final String filePath;

    /**
     * @param host     服务器地址
     * @param port     服务器端口
     * @param user     用户账号
     * @param password 用户密码
     * @param filePath 文件路径
     */
    private FtpClient(String host, int port, String user, String password, String filePath) {
        super("FTP Client", SystemConfig.CONNECT_TIMEOUT, new FtpMessageHandler());
        this.host     = host;
        this.port     = port;
        this.user     = user;
        this.password = password;
        this.filePath = filePath;
    }
    
    /**
     * @param host     服务器地址
     * @param port     服务器端口
     * @param user     用户账号
     * @param password 用户密码
     * @param filePath 文件路径
     * 
     * @return FTP客户端
     */
    public static final FtpClient newInstance(String host, int port, String user, String password, String filePath) {
        return new FtpClient(host, port, user, password, filePath);
    }

    /**
     * @param url FTP链接
     * 
     * @return FTP客户端
     */
    public static final FtpClient newInstance(String url) {
        final URIWrapper wrapper = URIWrapper.newInstance(url, DEFAULT_PORT, SystemConfig.getFtpUser(), SystemConfig.getFtpPassword()).decode();
        return FtpClient.newInstance(wrapper.getHost(), wrapper.getPort(), wrapper.getUser(), wrapper.password(), wrapper.getPath());
    }
    
    @Override
    public boolean connect() {
        this.handler.resetLock();
        this.connect = this.connect(this.host, this.port);
        // 锁定：等待FTP连接成功消息
        this.handler.lock();
        if(this.connect) {
            this.login();
            if(this.handler.getLogin()) {
                this.charset();
            } else {
                this.connect = false;
            }
        }
        return this.connect;
    }
    
    /**
     * 开始下载
     * 默认重新下载
     * 
     * @return 文件流
     * 
     * @throws NetException 网络异常
     */
    public InputStream download() throws NetException {
        return this.download(null);
    }
    
    /**
     * 开始下载
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
            final InputStream input = this.handler.getInputStream();
            if(input == null) {
                throw new NetException(this.failMessage("打开FTP文件流失败"));
            }
            return input;
        }
    }
    
    /**
     * FTP文件信息格式（UNIX）
     * -rwx------ 1 user group 102400 Jan 01 2020 SnailLauncher.exe
     * -rwx------         首个字符：d-目录、--文件；其他字符：r-可读、w-可写、x-可执行（参考Linux文件权限）；
     * 1                  位置
     * user               所属用户
     * group              所属分组
     * 102400             文件大小
     * Jan 01 2020        创建日期
     * SnailLauncher.exe  文件名称
     * 
     * FTP文件信息格式（MS-DOS）
     * 04-08-14 03:09PM 403 SnailLauncher.exe
     * 04-08-14           创建日期
     * 03:09PM            创建时间
     * 403                文件大小
     * SnailLauncher.exe  文件名称
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
            final InputStream inputStream = this.handler.getInputStream();
            final String message = StringUtils.ofInputStream(inputStream, this.handler.getCharset());
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
     * @return 是否支持断点续传
     * 
     * @throws NetException 网络异常
     */
    public boolean range() throws NetException {
        this.checkConnect();
        return this.handler.getRange();
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
     * 登陆服务器
     */
    private void login() {
        this.command("USER " + this.user);
        this.command("PASS " + this.password);
    }
    
    /**
     * 设置编码
     */
    private void charset() {
        // 列出扩展命令
        this.command("FEAT");
        if(SystemConfig.CHARSET_UTF8.equals(this.handler.getCharset())) {
            // 设置UTF8
            this.command("OPTS UTF8 ON");
        }
    }
    
    /**
     * 发送FTP命令
     * 默认加锁
     * 
     * @param command 命令
     */
    private void command(String command) {
        this.command(command, true);
    }
    
    /**
     * 发送FTP命令
     * 
     * @param command 命令
     * @param lock    是否加锁
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
     * 验证是否已经连接成功
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
     * @see FtpMessageHandler#getFailMessage(String)
     */
    private String failMessage(String defaultMessage) {
        return this.handler.getFailMessage(defaultMessage);
    }

}
