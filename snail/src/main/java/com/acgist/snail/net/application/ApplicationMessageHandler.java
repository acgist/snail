package com.acgist.snail.net.application;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.SymbolConfig;
import com.acgist.snail.context.GuiContext;
import com.acgist.snail.context.GuiContext.Mode;
import com.acgist.snail.context.SystemContext;
import com.acgist.snail.context.TaskContext;
import com.acgist.snail.context.exception.DownloadException;
import com.acgist.snail.context.exception.NetException;
import com.acgist.snail.context.exception.PacketSizeException;
import com.acgist.snail.format.BEncodeDecoder;
import com.acgist.snail.format.BEncodeEncoder;
import com.acgist.snail.gui.event.GuiEventMessage;
import com.acgist.snail.net.TcpMessageHandler;
import com.acgist.snail.net.codec.IMessageDecoder;
import com.acgist.snail.net.codec.IMessageEncoder;
import com.acgist.snail.net.codec.LineMessageCodec;
import com.acgist.snail.net.codec.StringMessageCodec;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.message.ApplicationMessage;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>系统消息代理</p>
 * 
 * @author acgist
 */
public final class ApplicationMessageHandler extends TcpMessageHandler implements IMessageDecoder<String> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationMessageHandler.class);
	
	/**
	 * <p>消息编码器</p>
	 */
	private final IMessageEncoder<String> messageEncoder;
	
	public ApplicationMessageHandler() {
		final var lineMessageCodec = new LineMessageCodec(this, SymbolConfig.LINE_SEPARATOR_COMPAT);
		final var stringMessageCodec = new StringMessageCodec(lineMessageCodec);
		this.messageDecoder = stringMessageCodec;
		this.messageEncoder = lineMessageCodec;
	}
	
	/**
	 * <p>发送系统消息</p>
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
		message = message.trim();
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
	 * <p>处理系统消息</p>
	 * 
	 * @param message 系统消息
	 */
	private void execute(ApplicationMessage message) {
		if(message.getType() == null) {
			LOGGER.warn("系统消息错误（未知类型）：{}", message.getType());
			return;
		}
		LOGGER.debug("处理系统消息：{}", message);
		switch (message.getType()) {
		case GUI:
			this.onGui();
			break;
		case TEXT:
			this.onText(message);
			break;
		case CLOSE:
			this.onClose();
			break;
		case NOTIFY:
			this.onNotify();
			break;
		case SHUTDOWN:
			this.onShutdown();
			break;
		case TASK_NEW:
			this.onTaskNew(message);
			break;
		case TASK_LIST:
			this.onTaskList();
			break;
		case TASK_START:
			this.onTaskStart(message);
			break;
		case TASK_PAUSE:
			this.onTaskPause(message);
			break;
		case TASK_DELETE:
			this.onTaskDelete(message);
			break;
		case SHOW:
			this.onShow();
			break;
		case HIDE:
			this.onHide();
			break;
		case ALERT:
			this.onAlert(message);
			break;
		case NOTICE:
			this.onNotice(message);
			break;
		case REFRESH_TASK_LIST:
			this.onRefreshTaskList();
			break;
		case REFRESH_TASK_STATUS:
			this.onRefreshTaskStatus();
			break;
		case RESPONSE:
			this.onResponse(message);
			break;
		default:
			LOGGER.warn("系统消息错误（类型未适配）：{}", message.getType());
			break;
		}
	}
	
	/**
	 * <p>扩展GUI注册</p>
	 * <p>将当前连接的消息代理注册为GUI消息代理，需要使用{@linkplain Mode#EXTEND 后台模式}启动。</p>
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
	 * <p>文本消息</p>
	 * <p>原样返回文本</p>
	 * 
	 * @param message 系统消息
	 */
	private void onText(ApplicationMessage message) {
		this.send(ApplicationMessage.Type.RESPONSE.build(message.getBody()));
	}
	
	/**
	 * <p>关闭连接</p>
	 */
	private void onClose() {
		this.close();
	}
	
	/**
	 * <p>唤醒窗口</p>
	 */
	private void onNotify() {
		GuiContext.getInstance().show();
	}
	
	/**
	 * <p>关闭程序</p>
	 */
	private void onShutdown() {
		SystemContext.shutdown();
	}
	
	/**
	 * <p>新建任务</p>
	 * <dl>
	 * 	<dt>body：Map（B编码）</dt>
	 * 	<dd>url：下载链接</dd>
	 * 	<dd>files：选择下载文件列表（B编码）</dd>
	 * </dl>
	 * 
	 * @param message 系统消息
	 */
	private void onTaskNew(ApplicationMessage message) {
		final String body = message.getBody();
		try {
			final var decoder = BEncodeDecoder.newInstance(body);
			decoder.nextMap();
			if(decoder.isEmpty()) {
				this.send(ApplicationMessage.Type.RESPONSE.build(ApplicationMessage.FAIL));
				return;
			}
			final String url = decoder.getString("url");
			final String files = decoder.getString("files");
			synchronized (this) {
				GuiContext.getInstance().files(files); // 设置选择文件
				TaskContext.getInstance().download(url); // 开始下载任务
			}
			this.send(ApplicationMessage.Type.RESPONSE.build(ApplicationMessage.SUCCESS));
		} catch (NetException | DownloadException e) {
			this.send(ApplicationMessage.Type.RESPONSE.build(e.getMessage()));
		}
	}

	/**
	 * <p>任务列表</p>
	 * <p>返回任务列表（B编码）</p>
	 */
	private void onTaskList() {
		final List<Map<String, Object>> list = TaskContext.getInstance().allTask().stream()
			.map(ITaskSession::taskMessage)
			.collect(Collectors.toList());
		final String body = BEncodeEncoder.encodeListString(list);
		this.send(ApplicationMessage.Type.RESPONSE.build(body));
	}

	/**
	 * <p>开始任务</p>
	 * <p>body：任务ID</p>
	 * 
	 * @param message 系统消息
	 */
	private void onTaskStart(ApplicationMessage message) {
		final var optional = this.selectTaskSession(message);
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
	 * <p>暂停任务</p>
	 * <p>body：任务ID</p>
	 * 
	 * @param message 系统消息
	 */
	private void onTaskPause(ApplicationMessage message) {
		final var optional = this.selectTaskSession(message);
		if(optional.isEmpty()) {
			this.send(ApplicationMessage.Type.RESPONSE.build(ApplicationMessage.FAIL));
		} else {
			optional.get().pause();
			this.send(ApplicationMessage.Type.RESPONSE.build(ApplicationMessage.SUCCESS));
		}
	}
	
	/**
	 * <p>删除任务</p>
	 * <p>body：任务ID</p>
	 * 
	 * @param message 系统消息
	 */
	private void onTaskDelete(ApplicationMessage message) {
		final var optional = this.selectTaskSession(message);
		if(optional.isEmpty()) {
			this.send(ApplicationMessage.Type.RESPONSE.build(ApplicationMessage.FAIL));
		} else {
			optional.get().delete();
			this.send(ApplicationMessage.Type.RESPONSE.build(ApplicationMessage.SUCCESS));
		}
	}
	
	/**
	 * <p>显示窗口</p>
	 */
	private void onShow() {
		GuiContext.getInstance().show();
	}
	
	/**
	 * <p>隐藏窗口</p>
	 */
	private void onHide() {
		GuiContext.getInstance().hide();
	}
	
	/**
	 * <p>窗口消息</p>
	 * 
	 * @param message 系统消息
	 */
	private void onAlert(ApplicationMessage message) {
		final String body = message.getBody();
		final var decoder = BEncodeDecoder.newInstance(body);
		try {
			decoder.nextMap();
			final String type = decoder.getString(GuiEventMessage.MESSAGE_TYPE);
			final String title = decoder.getString(GuiEventMessage.MESSAGE_TITLE);
			final String content = decoder.getString(GuiEventMessage.MESSAGE_MESSAGE);
			GuiContext.getInstance().alert(title, content, GuiContext.MessageType.of(type));
		} catch (PacketSizeException e) {
			LOGGER.warn("处理窗口消息异常", e);
		}
	}
	
	/**
	 * <p>提示消息</p>
	 * 
	 * @param message 系统消息
	 */
	private void onNotice(ApplicationMessage message) {
		final String body = message.getBody();
		final var decoder = BEncodeDecoder.newInstance(body);
		try {
			decoder.nextMap();
			final String type = decoder.getString(GuiEventMessage.MESSAGE_TYPE);
			final String title = decoder.getString(GuiEventMessage.MESSAGE_TITLE);
			final String content = decoder.getString(GuiEventMessage.MESSAGE_MESSAGE);
			GuiContext.getInstance().notice(title, content, GuiContext.MessageType.of(type));
		} catch (PacketSizeException e) {
			LOGGER.warn("处理提示消息异常", e);
		}
	}
	
	/**
	 * <p>刷新任务列表</p>
	 */
	private void onRefreshTaskList() {
		GuiContext.getInstance().refreshTaskList();
	}
	
	/**
	 * <p>刷新任务状态</p>
	 */
	private void onRefreshTaskStatus() {
		GuiContext.getInstance().refreshTaskStatus();
	}
	
	/**
	 * <p>响应消息</p>
	 * 
	 * @param message 系统消息
	 */
	private void onResponse(ApplicationMessage message) {
		GuiContext.getInstance().response(message.getBody());
	}
	
	/**
	 * <p>获取任务信息</p>
	 * <p>body：任务ID</p>
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
