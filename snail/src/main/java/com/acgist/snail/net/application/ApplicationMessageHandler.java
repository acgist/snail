package com.acgist.snail.net.application;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.SystemContext;
import com.acgist.snail.downloader.DownloaderManager;
import com.acgist.snail.exception.DownloadException;
import com.acgist.snail.exception.NetException;
import com.acgist.snail.exception.PacketSizeException;
import com.acgist.snail.format.BEncodeDecoder;
import com.acgist.snail.format.BEncodeEncoder;
import com.acgist.snail.gui.GuiManager;
import com.acgist.snail.gui.GuiManager.Mode;
import com.acgist.snail.net.TcpMessageHandler;
import com.acgist.snail.net.codec.IMessageCodec;
import com.acgist.snail.net.codec.impl.LineMessageCodec;
import com.acgist.snail.net.codec.impl.StringMessageCodec;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.message.ApplicationMessage;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>系统消息代理</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class ApplicationMessageHandler extends TcpMessageHandler implements IMessageCodec<String> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationMessageHandler.class);
	
	/**
	 * <p>多条消息分隔符：{@value}</p>
	 */
	private static final String SEPARATOR = SystemConfig.LINE_COMPAT_SEPARATOR;
	
	public ApplicationMessageHandler() {
		final var lineMessageCodec = new LineMessageCodec(this, SEPARATOR);
		final var stringMessageCodec = new StringMessageCodec(lineMessageCodec);
		this.messageCodec = stringMessageCodec;
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
			LOGGER.warn("系统消息错误（类型不支持）：{}", message.getType());
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
		case ALERT:
			this.onAlert(message);
			break;
		case NOTICE:
			this.onNotice(message);
			break;
		case REFRESH:
			this.onRefresh(message);
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
	 * <p>注册GUI</p>
	 * <p>将当前连接的消息代理注册为GUI消息代理，需要使用{@linkplain Mode#EXTEND 后台模式}启动。</p>
	 */
	private void onGui() {
		final boolean ok = GuiManager.getInstance().extendGuiMessageHandler(this);
		if(ok) {
			this.send(ApplicationMessage.response(ApplicationMessage.SUCCESS));
		} else {
			this.send(ApplicationMessage.response(ApplicationMessage.FAIL));
		}
	}

	/**
	 * <p>文本消息</p>
	 * <p>原样返回文本</p>
	 * 
	 * @param message 系统消息
	 */
	private void onText(ApplicationMessage message) {
		this.send(ApplicationMessage.response(message.getBody()));
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
		GuiManager.getInstance().show();
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
	 * 	<dd>files：种子文件选择列表（B编码）</dd>
	 * </dl>
	 * 
	 * @param message 系统消息
	 * 
	 * @since 1.1.1
	 */
	private void onTaskNew(ApplicationMessage message) {
		final String body = message.getBody();
		try {
			final var decoder = BEncodeDecoder.newInstance(body);
			decoder.nextMap();
			if(decoder.isEmpty()) { // 空数据返回失败
				this.send(ApplicationMessage.response(ApplicationMessage.FAIL));
				return;
			}
			final String url = decoder.getString("url");
			final String files = decoder.getString("files");
			GuiManager.getInstance().files(files); // 设置选择文件
			DownloaderManager.getInstance().newTask(url); // 开始下载任务
			this.send(ApplicationMessage.response(ApplicationMessage.SUCCESS));
		} catch (NetException | DownloadException e) {
			LOGGER.debug("新建下载任务异常：{}", body, e);
			this.send(ApplicationMessage.response(e.getMessage()));
		}
	}

	/**
	 * <p>获取任务列表</p>
	 * <p>返回任务列表（B编码）</p>
	 * 
	 * @since 1.1.1
	 */
	private void onTaskList() {
		final List<Map<String, Object>> list = DownloaderManager.getInstance().allTask().stream()
			.map(session -> session.taskMessage())
			.collect(Collectors.toList());
		final String body = BEncodeEncoder.encodeListString(list);
		this.send(ApplicationMessage.response(body));
	}

	/**
	 * <p>开始任务</p>
	 * <p>body：任务ID</p>
	 * 
	 * @param message 系统消息
	 * 
	 * @since 1.1.1
	 */
	private void onTaskStart(ApplicationMessage message) {
		final var optional = selectTaskSession(message);
		if(optional.isEmpty()) {
			this.send(ApplicationMessage.response(ApplicationMessage.FAIL));
		} else {
			try {
				DownloaderManager.getInstance().start(optional.get());
				this.send(ApplicationMessage.response(ApplicationMessage.SUCCESS));
			} catch (DownloadException e) {
				this.send(ApplicationMessage.response(e.getMessage()));
			}
		}
	}
	
	/**
	 * <p>暂停任务</p>
	 * <p>body：任务ID</p>
	 * 
	 * @param message 系统消息
	 * 
	 * @since 1.1.1
	 */
	private void onTaskPause(ApplicationMessage message) {
		final var optional = selectTaskSession(message);
		if(optional.isEmpty()) {
			this.send(ApplicationMessage.response(ApplicationMessage.FAIL));
		} else {
			DownloaderManager.getInstance().pause(optional.get());
			this.send(ApplicationMessage.response(ApplicationMessage.SUCCESS));
		}
	}
	
	/**
	 * <p>删除任务</p>
	 * <p>body：任务ID</p>
	 * 
	 * @param message 系统消息
	 * 
	 * @since 1.1.1
	 */
	private void onTaskDelete(ApplicationMessage message) {
		final var optional = selectTaskSession(message);
		if(optional.isEmpty()) {
			this.send(ApplicationMessage.response(ApplicationMessage.FAIL));
		} else {
			DownloaderManager.getInstance().delete(optional.get());
			this.send(ApplicationMessage.response(ApplicationMessage.SUCCESS));
		}
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
			final String type = decoder.getString("type");
			final String title = decoder.getString("title");
			final String content = decoder.getString("message");
			GuiManager.getInstance().alert(title, content, GuiManager.MessageType.valueOf(type));
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
			final String type = decoder.getString("type");
			final String title = decoder.getString("title");
			final String content = decoder.getString("message");
			GuiManager.getInstance().notice(title, content, GuiManager.MessageType.valueOf(type));
		} catch (PacketSizeException e) {
			LOGGER.warn("处理提示消息异常", e);
		}
	}
	
	/**
	 * <p>刷新消息</p>
	 * 
	 * @param message 系统消息
	 */
	private void onRefresh(ApplicationMessage message) {
		GuiManager.getInstance().refreshTaskList();
	}
	
	/**
	 * <p>响应消息</p>
	 * 
	 * @param message 系统消息
	 */
	private void onResponse(ApplicationMessage message) {
		GuiManager.getInstance().response(message.getBody());
	}
	
	/**
	 * <p>获取任务信息</p>
	 * <p>body：任务ID</p>
	 * 
	 * @param message 系统消息
	 * 
	 * @return 任务信息
	 * 
	 * @since 1.1.1
	 */
	private Optional<ITaskSession> selectTaskSession(ApplicationMessage message) {
		final String body = message.getBody(); // 任务ID
		return DownloaderManager.getInstance().allTask().stream()
			.filter(session -> session.getId().equals(body))
			.findFirst();
	}

	/**
	 * <p>发送系统消息</p>
	 * 
	 * @param message 系统消息
	 */
	private void send(ApplicationMessage message) {
		try {
			this.send(message.toString());
		} catch (NetException e) {
			LOGGER.error("发送系统消息异常", e);
		}
	}

}
