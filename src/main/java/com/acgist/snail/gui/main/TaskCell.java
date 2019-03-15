package com.acgist.snail.gui.main;

import com.acgist.snail.pojo.session.TaskSession;
import com.acgist.snail.system.config.FileTypeConfig.FileType;

import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.TableCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

/**
 * 任务单元格
 */
public class TaskCell extends TableCell<TaskSession, String> {

	private Pos pos = Pos.CENTER_LEFT;
	private boolean icon;
	
	public TaskCell(Pos pos, boolean icon) {
		this.pos = pos;
		this.icon = icon;
	}
	
	@Override
	public void updateItem(String value, boolean empty) {
		super.updateItem(value, empty);
		TaskSession session = this.getTableRow().getItem();
		if(session != null) {
			HBox box = new HBox();
			box.setAlignment(pos);
			Text name = new Text(value);
			if(this.icon) { // 名称：添加图标和手势
				name.setCursor(Cursor.HAND);
				FileType fileType = session.entity().getFileType();
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
