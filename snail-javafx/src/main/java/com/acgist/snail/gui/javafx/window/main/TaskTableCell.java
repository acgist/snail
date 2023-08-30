package com.acgist.snail.gui.javafx.window.main;

import com.acgist.snail.context.ITaskSession;
import com.acgist.snail.context.ITaskSession.FileType;
import com.acgist.snail.gui.javafx.Fonts;
import com.acgist.snail.gui.javafx.Tooltips;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Screen;

/**
 * 任务单元格
 * 
 * @author acgist
 */
public final class TaskTableCell extends TableCell<ITaskSession, String> {

    /**
     * 对齐方式
     */
    private final Pos pos;
    /**
     * 是否显示Icon
     */
    private final boolean icon;
    /**
     * 是否显示Tooltip
     */
    private final boolean tooltip;
    
    /**
     * @param pos     对齐方式
     * @param icon    是否显示Icon
     * @param tooltip 是否显示Tooltip
     */
    public TaskTableCell(Pos pos, boolean icon, boolean tooltip) {
        this.pos     = pos;
        this.icon    = icon;
        this.tooltip = tooltip;
    }
    
    @Override
    public void updateItem(String value, boolean empty) {
        super.updateItem(value, empty);
        final HBox box = new HBox();
        final ITaskSession taskSession = this.getTableRow().getItem();
        if(taskSession != null) {
            if(this.pos != null) {
                box.setAlignment(this.pos);
            }
            if(this.icon) {
                final FileType fileType = taskSession.getFileType();
                final Label fileLabel   = Fonts.fileTypeIconLabel(fileType);
                final double scaleX = Screen.getPrimary().getOutputScaleX();
                final double scaleY = Screen.getPrimary().getOutputScaleY();
                if(scaleX != 1.0D && scaleY != 1.0D) {
                    fileLabel.setStyle("-fx-font-size:14px;");
                }
                box.getChildren().add(fileLabel);
            }
            if(this.tooltip) {
                this.setTooltip(Tooltips.newTooltip(value));
            }
            final Text name = new Text(value);
            box.getChildren().add(name);
        }
        this.setGraphic(box);
    }
    
}
