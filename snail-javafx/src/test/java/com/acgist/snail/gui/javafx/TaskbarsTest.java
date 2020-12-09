package com.acgist.snail.gui.javafx;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;
import com.acgist.snail.utils.ThreadUtils;

public class TaskbarsTest extends Performance {

	@Test
	public void testProgress() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		final JFrame frame = new JFrame("进度测试");
		JFrame.setDefaultLookAndFeelDecorated(true);
		frame.setSize(200, 100);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		Taskbars taskbars = Taskbars.newInstance(frame);
		int index = 0;
		while(++index < 100) {
			if(index == 50) {
				taskbars.paused();
				ThreadUtils.sleep(2000);
			} else if(index == 60) {
				taskbars.error();
				ThreadUtils.sleep(2000);
			} else {
				taskbars.progress(index);
				ThreadUtils.sleep(20);
			}
		}
		taskbars.stop();
		ThreadUtils.sleep(2000);
	}
	
}
