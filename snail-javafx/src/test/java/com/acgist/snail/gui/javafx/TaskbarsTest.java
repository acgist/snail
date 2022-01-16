package com.acgist.snail.gui.javafx;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.ThreadUtils;

class TaskbarsTest extends Performance {

	@Test
	void testTaskbars() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		JFrame.setDefaultLookAndFeelDecorated(true);
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		final JFrame frame = new JFrame("进度测试");
		frame.setSize(200, 200);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		final Taskbars taskbars = Taskbars.newInstance(frame);
		int index = 0;
		while(++index < 100) {
			if(index == 50) {
				taskbars.paused();
				ThreadUtils.sleep(1000);
			} else if(index == 60) {
				taskbars.error();
				ThreadUtils.sleep(1000);
			} else {
				taskbars.progress(index);
				ThreadUtils.sleep(20);
			}
		}
		taskbars.stop();
		ThreadUtils.sleep(2000);
		assertNotNull(frame);
	}
	
	@Test
	void testJavaFXTaskBar() {
//		final SwingNode node = new SwingNode();
		ThreadUtils.sleep(2000);
	}
	
}
