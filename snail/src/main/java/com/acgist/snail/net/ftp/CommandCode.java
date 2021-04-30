package com.acgist.snail.net.ftp;

import com.acgist.snail.utils.StringUtils;

/**
 * <p>FTP响应码</p>
 * 
 * <p>协议链接：https://www.rfc-editor.org/rfc/rfc959.txt</p>
 * 
 * @author acgist
 */
public enum CommandCode {

	/**
	 * <p>数据连接打开</p>
	 */
	DATA_CONNECTION_OPEN("125"),
	/**
	 * <p>文件准备就绪</p>
	 */
	FILE_STATUS_OKAY("150"),
	/**
	 * <p>系统状态或者系统帮助</p>
	 */
	SYSTEM_STATUS("211"),
	/**
	 * <p>准备迎接新的用户</p>
	 */
	READY_FOR_NEW_USER("220"),
	/**
	 * <p>文件操作成功</p>
	 */
	FILE_ACTION_SUCCESS("226"),
	/**
	 * <p>被动模式</p>
	 */
	PASSIVE_MODE("227"),
	/**
	 * <p>登录系统</p>
	 */
	LOGIN_SUCCESS("230"),
	/**
	 * <p>等待文件操作（支持断点续传）</p>
	 */
	FILE_ACTION_PENDING("350"),
	/**
	 * <p>连接已经关闭</p>
	 */
	CONNECTION_CLOSED("421"),
	/**
	 * <p>不支持的命令</p>
	 */
	NOT_SUPPORT_COMMAND("502"),
	/**
	 * <p>没有登录</p>
	 */
	NOT_LOGIN("530"),
	/**
	 * <p>文件无效</p>
	 */
	FILE_UNAVAILABLE("550");
	
	/**
	 * <p>响应码</p>
	 */
	private final String code;
	
	/**
	 * <p>FTP响应码</p>
	 * 
	 * @param code 响应码
	 */
	private CommandCode(String code) {
		this.code = code;
	}
	
	/**
	 * <p>通过FTP消息获取FTP响应码</p>
	 * 
	 * @param message FTP消息
	 * 
	 * @return FTP响应码
	 */
	public static final CommandCode of(String message) {
		final var values = CommandCode.values();
		for (CommandCode commandCode : values) {
			if(StringUtils.startsWith(message, commandCode.code)) {
				return commandCode;
			}
		}
		return null;
	}
	
}