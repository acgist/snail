package com.acgist.snail.net.ftp;

import com.acgist.snail.utils.StringUtils;

/**
 * FTP响应码
 * 
 * 协议链接：https://www.rfc-editor.org/rfc/rfc959.txt
 * 
 * @author acgist
 */
public enum CommandCode {

    /**
     * 数据连接打开
     */
    DATA_CONNECTION_OPEN("125"),
    /**
     * 文件准备就绪
     */
    FILE_STATUS_OKAY("150"),
    /**
     * 系统状态或者系统帮助
     */
    SYSTEM_STATUS("211"),
    /**
     * 准备迎接新的用户
     */
    READY_FOR_NEW_USER("220"),
    /**
     * 文件操作成功
     */
    FILE_ACTION_SUCCESS("226"),
    /**
     * 被动模式
     */
    PASSIVE_MODE("227"),
    /**
     * 登录系统
     */
    LOGIN_SUCCESS("230"),
    /**
     * 等待文件操作（支持断点续传）
     */
    FILE_ACTION_PENDING("350"),
    /**
     * 连接已经关闭
     */
    CONNECTION_CLOSED("421"),
    /**
     * 不支持的命令
     */
    NOT_SUPPORT_COMMAND("502"),
    /**
     * 没有登录
     */
    NOT_LOGIN("530"),
    /**
     * 文件无效
     */
    FILE_UNAVAILABLE("550");
    
    /**
     * FTP响应码
     */
    private final String code;
    
    /**
     * @param code 响应码
     */
    private CommandCode(String code) {
        this.code = code;
    }
    
    /**
     * @param message FTP消息
     * 
     * @return FTP响应码
     */
    public static final CommandCode of(String message) {
        final CommandCode[] values = CommandCode.values();
        for (CommandCode commandCode : values) {
            if(StringUtils.startsWith(message, commandCode.code)) {
                return commandCode;
            }
        }
        return null;
    }
    
}