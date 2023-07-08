package com.acgist.snail.net.application;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.acgist.snail.config.SymbolConfig;
import com.acgist.snail.context.ITaskSession;
import com.acgist.snail.context.SystemContext;
import com.acgist.snail.context.TaskContext;
import com.acgist.snail.format.BEncodeDecoder;
import com.acgist.snail.format.BEncodeEncoder;
import com.acgist.snail.gui.GuiContext;
import com.acgist.snail.gui.GuiContext.Mode;
import com.acgist.snail.gui.GuiMessage;
import com.acgist.snail.gui.event.adapter.MultifileEventAdapter;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.net.DownloadException;
import com.acgist.snail.net.NetException;
import com.acgist.snail.net.PacketSizeException;
import com.acgist.snail.net.TcpMessageHandler;
import com.acgist.snail.net.codec.IMessageDecoder;
import com.acgist.snail.net.codec.IMessageEncoder;
import com.acgist.snail.net.codec.LineMessageCodec;
import com.acgist.snail.net.codec.StringMessageCodec;
import com.acgist.snail.utils.StringUtils;

/**
 * 系统消息代理
 * 
 * @author acgist
 */
public final class ApplicationMessageHandler extends TcpMessageHandler implements IMessageDecoder<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationMessageHandler.class);
    
    /**
     * 消息编码器
     */
    private final IMessageEncoder<String> messageEncoder;
    
    public ApplicationMessageHandler() {
        final LineMessageCodec   lineMessageCodec   = new LineMessageCodec(this, SymbolConfig.LINE_SEPARATOR_COMPAT);
        final StringMessageCodec stringMessageCodec = new StringMessageCodec(lineMessageCodec);
        this.messageDecoder = stringMessageCodec;
        this.messageEncoder = lineMessageCodec;
    }
    
    /**
     * 发送系统消息
     * 
     * @param message 系统消息
     */
    public void send(ApplicationMessage message) {
        try {
            this.send(message.toString());
        } catch (NetException e) {
            LOGGER.error("发送系统消息异常", e);
        }
    }
    
    @Override
    public void send(String message, String charset) throws NetException {
        super.send(this.messageEncoder.encode(message), charset);
    }
    
    @Override
    public void onMessage(String message) {
        if(StringUtils.isEmpty(message)) {
            LOGGER.warn("系统消息错误：{}", message);
            return;
        }
        final ApplicationMessage applicationMessage = ApplicationMessage.valueOf(message);
        if(applicationMessage == null) {
            LOGGER.warn("系统消息错误（格式）：{}", message);
            return;
        }
        this.execute(applicationMessage);
    }
    
    /**
     * 处理系统消息
     * 
     * @param message 系统消息
     */
    private void execute(ApplicationMessage message) {
        final ApplicationMessage.Type type = message.getType();
        if(type == null) {
            LOGGER.warn("系统消息错误（未知类型）：{}", type);
            return;
        }
        LOGGER.debug("处理系统消息：{}", message);
        switch (type) {
            case GUI                 -> this.onGui();
            case TEXT                -> this.onText(message);
            case CLOSE               -> this.onClose();
            case NOTIFY              -> this.onNotify();
            case SHUTDOWN            -> this.onShutdown();
            case TASK_NEW            -> this.onTaskNew(message);
            case TASK_LIST           -> this.onTaskList();
            case TASK_START          -> this.onTaskStart(message);
            case TASK_PAUSE          -> this.onTaskPause(message);
            case TASK_DELETE         -> this.onTaskDelete(message);
            case SHOW                -> this.onShow();
            case HIDE                -> this.onHide();
            case ALERT               -> this.onAlert(message);
            case NOTICE              -> this.onNotice(message);
            case MULTIFILE           -> this.onMultifile(message);
            case REFRESH_TASK_LIST   -> this.onRefreshTaskList();
            case REFRESH_TASK_STATUS -> this.onRefreshTaskStatus();
            case RESPONSE            -> this.onResponse(message);
            default                  -> LOGGER.warn("系统消息错误（类型未适配）：{}", type);
        }
    }
    
    /**
     * 扩展GUI注册
     * 将当前连接的消息代理注册为GUI消息代理
     * 
     * @see Mode#EXTEND
     */
    private void onGui() {
        final boolean success = GuiContext.getInstance().extendGuiMessageHandler(this);
        if(success) {
            this.send(ApplicationMessage.Type.RESPONSE.build(ApplicationMessage.SUCCESS));
        } else {
            this.send(ApplicationMessage.Type.RESPONSE.build(ApplicationMessage.FAIL));
        }
    }

    /**
     * 文本消息
     * 
     * @param message 系统消息
     */
    private void onText(ApplicationMessage message) {
        this.send(ApplicationMessage.Type.RESPONSE.build(message.getBody()));
    }
    
    /**
     * 关闭连接
     */
    private void onClose() {
        this.close();
    }
    
    /**
     * 唤醒窗口
     */
    private void onNotify() {
        GuiContext.getInstance().show();
    }
    
    /**
     * 关闭程序
     */
    private void onShutdown() {
        SystemContext.shutdown();
    }
    
    /**
     * 新建任务
     * 
     * body  Map（B编码）
     * url   下载链接
     * files 选择下载文件列表（B编码）
     * 
     * @param message 系统消息
     */
    private void onTaskNew(ApplicationMessage message) {
        final String body = message.getBody();
        try {
            final BEncodeDecoder decoder = BEncodeDecoder.newInstance(body).next();
            if(decoder.isEmpty()) {
                this.send(ApplicationMessage.Type.RESPONSE.build(ApplicationMessage.FAIL));
                return;
            }
            final String url   = decoder.getString("url");
            final String files = decoder.getString("files");
            synchronized (this) {
                if(StringUtils.isNotEmpty(files)) {
                    // 设置选择文件
                    MultifileEventAdapter.files(files);
                }
                if(StringUtils.isNotEmpty(url)) {
                    // 开始下载任务
                    TaskContext.getInstance().download(url);
                }
            }
            this.send(ApplicationMessage.Type.RESPONSE.build(ApplicationMessage.SUCCESS));
        } catch (NetException | DownloadException e) {
            this.send(ApplicationMessage.Type.RESPONSE.build(e.getMessage()));
        }
    }

    /**
     * 任务列表
     * 返回任务列表（B编码）
     */
    private void onTaskList() {
        final List<Map<String, Object>> list = TaskContext.getInstance().allTask().stream()
            .map(ITaskSession::toMap)
            .collect(Collectors.toList());
        final String body = BEncodeEncoder.encodeListString(list);
        this.send(ApplicationMessage.Type.RESPONSE.build(body));
    }

    /**
     * 开始任务
     * body：任务ID
     * 
     * @param message 系统消息
     */
    private void onTaskStart(ApplicationMessage message) {
        final Optional<ITaskSession> optional = this.selectTaskSession(message);
        if(optional.isEmpty()) {
            this.send(ApplicationMessage.Type.RESPONSE.build(ApplicationMessage.FAIL));
        } else {
            try {
                optional.get().start();
                this.send(ApplicationMessage.Type.RESPONSE.build(ApplicationMessage.SUCCESS));
            } catch (DownloadException e) {
                this.send(ApplicationMessage.Type.RESPONSE.build(e.getMessage()));
            }
        }
    }
    
    /**
     * 暂停任务
     * body：任务ID
     * 
     * @param message 系统消息
     */
    private void onTaskPause(ApplicationMessage message) {
        final Optional<ITaskSession> optional = this.selectTaskSession(message);
        if(optional.isEmpty()) {
            this.send(ApplicationMessage.Type.RESPONSE.build(ApplicationMessage.FAIL));
        } else {
            optional.get().pause();
            this.send(ApplicationMessage.Type.RESPONSE.build(ApplicationMessage.SUCCESS));
        }
    }
    
    /**
     * 删除任务
     * body：任务ID
     * 
     * @param message 系统消息
     */
    private void onTaskDelete(ApplicationMessage message) {
        final Optional<ITaskSession> optional = this.selectTaskSession(message);
        if(optional.isEmpty()) {
            this.send(ApplicationMessage.Type.RESPONSE.build(ApplicationMessage.FAIL));
        } else {
            optional.get().delete();
            this.send(ApplicationMessage.Type.RESPONSE.build(ApplicationMessage.SUCCESS));
        }
    }
    
    /**
     * 显示窗口
     */
    private void onShow() {
        GuiContext.getInstance().show();
    }
    
    /**
     * 隐藏窗口
     */
    private void onHide() {
        GuiContext.getInstance().hide();
    }
    
    /**
     * 文件选择
     * 
     * @param message 系统消息
     */
    private void onMultifile(ApplicationMessage message) {
        LOGGER.debug("文件选择：{}", message.getBody());
    }
    
    /**
     * 窗口消息
     * 
     * @param message 系统消息
     */
    private void onAlert(ApplicationMessage message) {
        try {
            final GuiMessage guiMessage = GuiMessage.of(message);
            if(guiMessage == null) {
                LOGGER.warn("窗口消息错误：{}", message);
                return;
            }
            GuiContext.getInstance().alert(guiMessage.title(), guiMessage.message(), guiMessage.type());
        } catch (PacketSizeException e) {
            LOGGER.warn("处理窗口消息异常", e);
        }
    }
    
    /**
     * 提示消息
     * 
     * @param message 系统消息
     */
    private void onNotice(ApplicationMessage message) {
        try {
            final GuiMessage guiMessage = GuiMessage.of(message);
            if(guiMessage == null) {
                LOGGER.warn("提示消息错误：{}", message);
                return;
            }
            GuiContext.getInstance().notice(guiMessage.title(), guiMessage.message(), guiMessage.type());
        } catch (PacketSizeException e) {
            LOGGER.warn("处理提示消息异常", e);
        }
    }
    
    /**
     * 刷新任务列表
     */
    private void onRefreshTaskList() {
        GuiContext.getInstance().refreshTaskList();
    }
    
    /**
     * 刷新任务状态
     */
    private void onRefreshTaskStatus() {
        GuiContext.getInstance().refreshTaskStatus();
    }
    
    /**
     * 响应消息
     * 
     * @param message 系统消息
     */
    private void onResponse(ApplicationMessage message) {
        GuiContext.getInstance().response(message.getBody());
    }
    
    /**
     * 任务信息
     * body：任务ID
     * 
     * @param message 系统消息
     * 
     * @return 任务信息
     */
    private Optional<ITaskSession> selectTaskSession(ApplicationMessage message) {
        final String body = message.getBody();
        return TaskContext.getInstance().allTask().stream()
            .filter(session -> session.getId().equals(body))
            .findFirst();
    }

}
