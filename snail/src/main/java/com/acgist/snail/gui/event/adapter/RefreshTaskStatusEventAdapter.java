package com.acgist.snail.gui.event.adapter;

import java.util.Map;

import com.acgist.snail.context.ITaskSessionStatus.Status;
import com.acgist.snail.format.BEncodeEncoder;
import com.acgist.snail.gui.event.GuiEvent;
import com.acgist.snail.gui.event.GuiEventMessage;
import com.acgist.snail.net.application.ApplicationMessage;
import com.acgist.snail.utils.ArrayUtils;

/**
 * GUI刷新任务状态事件
 * 
 * @author acgist
 */
public class RefreshTaskStatusEventAdapter extends GuiEvent {

    public RefreshTaskStatusEventAdapter() {
        super(Type.REFRESH_TASK_STATUS, "刷新任务状态事件");
    }

    @Override
    protected void executeNative(Object... args) {
        this.executeExtend(args);
    }
    
    @Override
    protected void executeExtend(Object ... args) {
        if(
            ArrayUtils.isNotEmpty(args)  &&
            args.length == 2             &&
            args[0] instanceof String id &&
            args[1] instanceof Status status
        ) {
            final Map<String, String> map = Map.of(
                GuiEventMessage.TASK_ID,     id,
                GuiEventMessage.TASK_STATUS, status.name()
            );
            final String body = BEncodeEncoder.encodeMapString(map);
            this.sendExtendGuiMessage(ApplicationMessage.Type.REFRESH_TASK_STATUS.build(body));
        } else {
            this.sendExtendGuiMessage(ApplicationMessage.Type.REFRESH_TASK_STATUS.build());
        }
    }

}
