package com.acgist.snail.gui.javafx.event;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.acgist.main.Application;
import com.acgist.snail.gui.GuiContext;
import com.acgist.snail.utils.ThreadUtils;

class EventTest {

	@Test
	void testEvent() {
		Application.main(null);
		ThreadUtils.sleep(5000);
		assertNotNull(GuiContext.getInstance());
		GuiContext.getInstance().alert("测试", "测试");
		ThreadUtils.sleep(1000);
		GuiContext.getInstance().hide();
		ThreadUtils.sleep(1000);
		GuiContext.getInstance().show();
		ThreadUtils.sleep(1000);
		GuiContext.getInstance().notice("测试", "测试");
		ThreadUtils.sleep(1000);
		GuiContext.getInstance().refreshTaskList();
		ThreadUtils.sleep(1000);
		GuiContext.getInstance().refreshTaskStatus();
		ThreadUtils.sleep(1000);
		GuiContext.getInstance().response("测试");
		ThreadUtils.sleep(1000);
		GuiContext.getInstance().exit();
		ThreadUtils.sleep(1000);
	}
	
}
