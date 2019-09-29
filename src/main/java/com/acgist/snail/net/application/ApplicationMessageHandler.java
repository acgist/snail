package com.acgist.snail.net.application;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.downloader.DownloaderManager;
import com.acgist.snail.gui.GuiHandler;
import com.acgist.snail.gui.event.impl.TorrentEvent;
import com.acgist.snail.net.TcpMessageHandler;
import com.acgist.snail.net.codec.IMessageCodec;
import com.acgist.snail.net.codec.impl.LineMessageCodec;
import com.acgist.snail.net.codec.impl.StringMessageCodec;
import com.acgist.snail.pojo.message.ApplicationMessage;
import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.system.bencode.BEncodeDecoder;
import com.acgist.snail.system.bencode.BEncodeEncoder;
import com.acgist.snail.system.context.SystemContext;
import com.acgist.snail.system.exception.DownloadException;
import com.acgist.snail.system.exception.NetException;
import com.acgist.snail.utils.BeanUtils;
import com.acgist.snail.utils.StringUtils;

/**
 * 系统消息代理
 * 
 * @author acgist
 * @since 1.0.0
 */
public class ApplicationMessageHandler extends TcpMessageHandler implements IMessageCodec<String> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationMessageHandler.class);
	
	/**
	 * 消息分隔符
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
			LOGGER.warn("系统消息内容为空");
			return;
		}
		final ApplicationMessage applicationMessage = ApplicationMessage.valueOf(message);
		if(applicationMessage == null) {
			LOGGER.warn("系统消息格式错误：{}", message);
			return;
		}
		LOGGER.debug("处理系统消息：{}", message);
		this.execute(applicationMessage);
	}
	
	/**
	 * <p>处理消息</p>
	 */
	private void execute(ApplicationMessage message) {
		if(message.getType() == null) {
			LOGGER.warn("系统消息类型为空：{}", message.getType());
			return;
		}
		switch (message.getType()) {
		case gui:
			onGui(message);
			break;
		case text:
			onText(message);
			break;
		case close:
			onClose(message);
			break;
		case notify:
			onNotify(message);
			break;
		case shutdown:
			onShutdown(message);
			break;
		case taskNew:
			onTaskNew(message);
			break;
		case taskList:
			onTaskList(message);
			break;
		case taskStart:
			onTaskStart(message);
			break;
		case taskPause:
			onTaskPause(message);
			break;
		case taskDelete:
			onTaskDelete(message);
			break;
		case response:
			onResponse(message);
			break;
		default:
			LOGGER.warn("未适配的系统消息类型：{}", message.getType());
			break;
		}
	}
	
	/**
	 * GUI注册
	 */
	private void onGui(ApplicationMessage message) {
		final boolean ok = GuiHandler.getInstance().messageHandler(this);
		if(ok) {
			send(ApplicationMessage.response(ApplicationMessage.SUCCESS));
		} else {
			send(ApplicationMessage.response(ApplicationMessage.FAIL));
		}
	}

	/**
	 * 文本消息，原因返回
	 */
	private void onText(ApplicationMessage message) {
		send(ApplicationMessage.response(message.getBody()));
	}
	
	/**
	 * 关闭连接
	 */
	private void onClose(ApplicationMessage message) {
		this.close();
	}
	
	/**
	 * 唤醒窗口
	 */
	private void onNotify(ApplicationMessage message) {
		GuiHandler.getInstance().show();
	}
	
	/**
	 * 关闭程序
	 */
	private void onShutdown(ApplicationMessage message) {
		SystemContext.shutdown();
	}
	
	/**
	 * <p>新建任务</p>
	 * <p>
	 * body=B编码Map：<br>
	 * url=下载链接；<br>
	 * files=种子文件选择列表（B编码），每条文件包含路径：snail/video/demo.mp4；
	 * </p>
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
		} catch (Exception e) {
			LOGGER.debug("新建任务异常：{}", body, e);
			send(ApplicationMessage.response(e.getMessage()));
		}
	}

	/**
	 * 任务列表：B编码
	 * 
	 * @since 1.1.1
	 */
	private void onTaskList(ApplicationMessage message) {
		final List<Map<String, Object>> list = DownloaderManager.getInstance().tasks().stream()
			.map(task -> task.entity())
			// 将对象属性转换为Map
			.map(entity -> {
				return BeanUtils.toMap(entity).entrySet().stream()
					.filter(entry -> {
						// 去掉key==null并且value==null的值
						return entry.getKey() != null && entry.getValue() != null;
					})
					.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
			}).collect(Collectors.toList());
		final String body = BEncodeEncoder.encodeListString(list);
		send(ApplicationMessage.response(body));
	}

	/**
	 * 开始任务：body=TaskId
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
	 * 暂停任务：body=TaskId
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
	 * 删除任务：body=TaskId
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
	 * 查找TaskSession
	 * 
	 * @since 1.1.1
	 */
	private Optional<TaskSession> selectTaskSession(ApplicationMessage message) {
		final String body = message.getBody();
		return DownloaderManager.getInstance().tasks().stream()
			.filter(session -> session.entity().getId().equals(body))
			.findFirst();
	}
	
	/**
	 * 响应
	 */
	private void onResponse(ApplicationMessage message) {
		LOGGER.debug("收到响应：{}", message.getBody());
	}

	/**
	 * 发送消息
	 */
	private void send(ApplicationMessage message) {
		try {
			send(message.toString());
		} catch (NetException e) {
			LOGGER.error("发送Application消息异常", e);
		}
	}

}
