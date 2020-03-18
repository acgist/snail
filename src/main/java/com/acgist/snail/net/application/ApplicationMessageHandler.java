package com.acgist.snail.net.application;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.DownloaderManager;
import com.acgist.snail.gui.GuiManager;
import com.acgist.snail.gui.event.impl.TorrentEvent;
import com.acgist.snail.net.TcpMessageHandler;
import com.acgist.snail.net.codec.IMessageCodec;
import com.acgist.snail.net.codec.impl.LineMessageCodec;
import com.acgist.snail.net.codec.impl.StringMessageCodec;
import com.acgist.snail.pojo.ITaskSession;
import com.acgist.snail.pojo.message.ApplicationMessage;
import com.acgist.snail.system.bencode.BEncodeDecoder;
import com.acgist.snail.system.bencode.BEncodeEncoder;
import com.acgist.snail.system.context.SystemContext;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.exception.NetException;
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
	 * <p>消息分隔符</p>
	 */
	private static final String SPLIT = "\r\n";
	
	public ApplicationMessageHandler() {
		final var lineMessageCodec = new LineMessageCodec(this, SPLIT);
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
	 * @param 系统消息
	 */
	private void execute(ApplicationMessage message) {
		if(message.getType() == null) {
			LOGGER.warn("系统消息错误（类型不支持）：{}", message.getType());
			return;
		}
		LOGGER.debug("处理系统消息：{}", message);
		switch (message.getType()) {
		case GUI:
			onGui(message);
			break;
		case TEXT:
			onText(message);
			break;
		case CLOSE:
			onClose(message);
			break;
		case NOTIFY:
			onNotify(message);
			break;
		case SHUTDOWN:
			onShutdown(message);
			break;
		case TASK_NEW:
			onTaskNew(message);
			break;
		case TASK_LIST:
			onTaskList(message);
			break;
		case TASK_START:
			onTaskStart(message);
			break;
		case TASK_PAUSE:
			onTaskPause(message);
			break;
		case TASK_DELETE:
			onTaskDelete(message);
			break;
		case RESPONSE:
			onResponse(message);
			break;
		default:
			LOGGER.warn("系统消息错误（类型未适配）：{}", message.getType());
			break;
		}
	}
	
	/**
	 * <p>注册GUI</p>
	 * 
	 * @param message 系统消息
	 */
	private void onGui(ApplicationMessage message) {
		final boolean ok = GuiManager.getInstance().extendGuiMessageHandler(this);
		if(ok) {
			send(ApplicationMessage.response(ApplicationMessage.SUCCESS));
		} else {
			send(ApplicationMessage.response(ApplicationMessage.FAIL));
		}
	}

	/**
	 * <p>文本消息</p>
	 * <p>原样返回</p>
	 * 
	 * @param message 系统消息
	 */
	private void onText(ApplicationMessage message) {
		send(ApplicationMessage.response(message.getBody()));
	}
	
	/**
	 * <p>关闭连接</p>
	 * 
	 * @param message 系统消息
	 */
	private void onClose(ApplicationMessage message) {
		this.close();
	}
	
	/**
	 * <p>唤醒窗口</p>
	 * 
	 * @param message 系统消息
	 */
	private void onNotify(ApplicationMessage message) {
		GuiManager.getInstance().show();
	}
	
	/**
	 * <p>关闭程序</p>
	 * 
	 * @param message 系统消息
	 */
	private void onShutdown(ApplicationMessage message) {
		SystemContext.shutdown();
	}
	
	/**
	 * <p>新建任务</p>
	 * <dl>
	 * 	<dt>body：Map（B编码）</dt>
	 * 	<dd>url：下载链接</dd>
	 * 	<dd>files：种子文件选择列表（文件包含路径：snail/video/demo.mp4）</dd>
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
				send(ApplicationMessage.response(ApplicationMessage.FAIL));
				return;
			}
			final String url = decoder.getString("url");
			final String files = decoder.getString("files");
			TorrentEvent.getInstance().files(files); // 设置选择文件
			DownloaderManager.getInstance().newTask(url); // 开始下载任务
			send(ApplicationMessage.response(ApplicationMessage.SUCCESS));
		} catch (NetException | DownloadException e) {
			LOGGER.debug("新建下载任务异常：{}", body, e);
			send(ApplicationMessage.response(e.getMessage()));
		}
	}

	/**
	 * <p>任务列表</p>
	 * <p>返回任务列表（B编码）</p>
	 * 
	 * @param message 系统消息
	 * 
	 * @since 1.1.1
	 */
	private void onTaskList(ApplicationMessage message) {
		final List<Map<String, Object>> list = DownloaderManager.getInstance().allTask().stream()
			.map(session -> session.taskMessage())
			.collect(Collectors.toList());
		final String body = BEncodeEncoder.encodeListString(list);
		send(ApplicationMessage.response(body));
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
			send(ApplicationMessage.response(ApplicationMessage.FAIL));
		} else {
			try {
				DownloaderManager.getInstance().start(optional.get());
				send(ApplicationMessage.response(ApplicationMessage.SUCCESS));
			} catch (DownloadException e) {
				send(ApplicationMessage.response(e.getMessage()));
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
			send(ApplicationMessage.response(ApplicationMessage.FAIL));
		} else {
			DownloaderManager.getInstance().pause(optional.get());
			send(ApplicationMessage.response(ApplicationMessage.SUCCESS));
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
			send(ApplicationMessage.response(ApplicationMessage.FAIL));
		} else {
			DownloaderManager.getInstance().delete(optional.get());
			send(ApplicationMessage.response(ApplicationMessage.SUCCESS));
		}
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
	 * <p>响应消息</p>
	 * 
	 * @param message 系统消息
	 */
	private void onResponse(ApplicationMessage message) {
		LOGGER.debug("系统响应：{}", message.getBody());
	}

	/**
	 * <p>发送系统消息</p>
	 * 
	 * @param message 系统消息
	 */
	private void send(ApplicationMessage message) {
		try {
			send(message.toString());
		} catch (NetException e) {
			LOGGER.error("发送系统消息异常", e);
		}
	}

}
