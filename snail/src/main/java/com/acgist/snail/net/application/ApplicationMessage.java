package com.acgist.snail.net.application;

import com.acgist.snail.format.BEncodeDecoder;
import com.acgist.snail.format.BEncodeEncoder;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.NetException;

/**
 * 系统消息、系统通知
 * 系统消息（被动消息）：系统接收来自GUI通知
 * 系统通知（主动消息）：系统主动发送GUI通知
 * 
 * @author acgist
 */
public class ApplicationMessage {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationMessage.class);
    
    /**
     * 失败：{@value}
     */
    public static final String FAIL = "fail";
    /**
     * 成功：{@value}
     */
    public static final String SUCCESS = "success";
    /**
     * 消息类型
     */
    private static final String MESSAGE_TYPE = "type";
    /**
     * 消息内容
     */
    private static final String MESSAGE_BODY = "body";

    /**
     * 系统消息、系统通知类型
     * 
     * @author acgist
     */
    public enum Type {
        
        /**
         * 系统消息：GUI注册
         */
        GUI,
        /**
         * 系统消息：文本消息
         */
        TEXT,
        /**
         * 系统消息：关闭连接
         */
        CLOSE,
        /**
         * 系统消息：唤醒窗口
         */
        NOTIFY,
        /**
         * 系统消息：关闭程序
         */
        SHUTDOWN,
        /**
         * 系统消息：新建任务
         */
        TASK_NEW,
        /**
         * 系统消息：任务列表
         */
        TASK_LIST,
        /**
         * 系统消息：开始任务
         */
        TASK_START,
        /**
         * 系统消息：暂停任务
         */
        TASK_PAUSE,
        /**
         * 系统消息：删除任务
         */
        TASK_DELETE,
        /**
         * 系统通知：显示窗口
         */
        SHOW,
        /**
         * 系统通知：隐藏窗口
         */
        HIDE,
        /**
         * 系统通知：窗口消息
         */
        ALERT,
        /**
         * 系统通知：提示消息
         */
        NOTICE,
        /**
         * 系统通知：选择下载文件
         */
        MULTIFILE,
        /**
         * 系统通知：刷新任务列表
         */
        REFRESH_TASK_LIST,
        /**
         * 系统通知：刷新任务状态
         */
        REFRESH_TASK_STATUS,
        /**
         * 系统通知：响应消息
         */
        RESPONSE;

        /**
         * @param name 类型名称（忽略大小写）
         * 
         * @return 消息类型
         */
        public static final Type of(String name) {
            final Type[] types = Type.values();
            for (Type type : types) {
                if(type.name().equalsIgnoreCase(name)) {
                    return type;
                }
            }
            return null;
        }
        
        /**
         * 新建消息
         * 
         * @return 系统消息
         */
        public ApplicationMessage build() {
            return this.build(null);
        }
        
        /**
         * 新建消息
         * 
         * @param body 消息内容
         * 
         * @return 系统消息
         */
        public ApplicationMessage build(String body) {
            return new ApplicationMessage(this, body);
        }
        
    }

    /**
     * 消息类型
     */
    private final Type type;
    /**
     * 消息内容
     */
    private final String body;

    /**
     * @param type 消息类型
     * @param body 消息内容
     */
    private ApplicationMessage(Type type, String body) {
        this.type = type;
        this.body = body;
    }
    
    /**
     * 读取系统文本消息（B编码）
     * 
     * @param content 系统文本消息
     * 
     * @return 系统消息
     */
    public static final ApplicationMessage valueOf(String content) {
        try {
            final BEncodeDecoder decoder = BEncodeDecoder.newInstance(content).next();
            if(decoder.isEmpty()) {
                return null;
            }
            final String type = decoder.getString(MESSAGE_TYPE);
            final String body = decoder.getString(MESSAGE_BODY);
            final Type messageType = Type.of(type);
            if(messageType == null) {
                LOGGER.debug("系统消息类型错误：{}", type);
                return null;
            }
            return messageType.build(body);
        } catch (NetException e) {
            LOGGER.error("读取系统文本消息异常：{}", content, e);
        }
        return null;
    }
    
    /**
     * @return 消息类型
     */
    public Type getType() {
        return this.type;
    }

    /**
     * @return 消息内容
     */
    public String getBody() {
        return this.body;
    }

    @Override
    public String toString() {
        final BEncodeEncoder encoder = BEncodeEncoder.newInstance()
            .newMap()
            .put(MESSAGE_TYPE, this.type.name());
        if(this.body != null) {
            encoder.put(MESSAGE_BODY, this.body);
        }
        return encoder.flush().toString();
    }
    
}
