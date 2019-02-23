package com.acgist.snail.window.main;

import com.acgist.snail.module.config.FileTypeConfig.FileType;
import com.acgist.snail.pojo.wrapper.TaskWrapper;

import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.TableCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

/**
 * 任务单元格
 */
public class TaskCell extends TableCell<TaskWrapper, String> {

	private Pos pos = Pos.CENTER_LEFT;
	private boolean name;
	
	public TaskCell(Pos pos, boolean name) {
		this.pos = pos;
		this.name = name;
	}
	
	@Override
	public void updateItem(String value, boolean empty) {
		super.updateItem(value, empty);
		TaskWrapper task = this.getTableRow().getItem();
		if(task != null) {
			HBox box = new HBox();
			Text name = new Text(value);
			if(this.name) { // 名称：添加图标和手势
				FileType fileType = task.getFileType();
				ImageView icon = new ImageView("/image/32/" + fileType.getIcon());
				name.setCursor(Cursor.HAND);
				box.getChildren().add(icon);
			}
			box.getChildren().add(name);
			box.setAlignment(pos);
			this.setGraphic(box);
		} else {
			this.setGraphic(new HBox());
		}
	}
	
}
