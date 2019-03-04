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
	private boolean icon;
	
	public TaskCell(Pos pos, boolean icon) {
		this.pos = pos;
		this.icon = icon;
	}
	
	@Override
	public void updateItem(String value, boolean empty) {
		super.updateItem(value, empty);
		TaskWrapper wrapper = this.getTableRow().getItem();
		if(wrapper != null) {
			HBox box = new HBox();
			box.setAlignment(pos);
			Text name = new Text(value);
			if(this.icon) { // 名称：添加图标和手势
				name.setCursor(Cursor.HAND);
				FileType fileType = wrapper.getFileType();
				if(fileType != null) {
					ImageView icon = new ImageView("/image/32/" + fileType.getIcon());
					box.getChildren().add(icon);
				}
			}
			box.getChildren().add(name);
			this.setGraphic(box);
		} else {
			this.setGraphic(new HBox());
		}
	}
	
}
