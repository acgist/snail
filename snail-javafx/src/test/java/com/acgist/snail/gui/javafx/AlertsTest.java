package com.acgist.snail.gui.javafx;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.acgist.snail.context.GuiContext.MessageType;
import com.acgist.snail.utils.Performance;

import javafx.application.Platform;
import javafx.scene.control.ButtonType;

public class AlertsTest extends Performance {

	@Test
	public void testAlerts() throws InterruptedException {
		final Object lock = new Object();
		Platform.startup(() -> {});
		Platform.runLater(() -> {
			Alerts.info("测试", "测试信息");
			Alerts.warn("警告", "警告信息");
			Optional<ButtonType> optional;
			do {
				optional = Alerts.build("确认", "是否删除？", MessageType.CONFIRM);
			} while (optional.isPresent() && optional.get() != ButtonType.OK);
			synchronized (lock) {
				lock.notify();
			}
		});
		synchronized (lock) {
			lock.wait();
		}
		assertNotNull(lock);
	}
	
}
