package com.acgist.snail.gui.javafx.window.about;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.context.SystemContext;
import com.acgist.snail.gui.GuiContext;
import com.acgist.snail.gui.javafx.Alerts;
import com.acgist.snail.gui.javafx.Desktops;
import com.acgist.snail.gui.javafx.window.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;

/**
 * 关于窗口控制器
 * 
 * @author acgist
 */
public final class AboutController extends Controller {
    
    @FXML
    private BorderPane root;
    @FXML
    private Text name;
    @FXML
    private Text version;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.buildName();
        this.buildVersion();
    }

    /**
     * 作者按钮
     * 
     * @param event 事件
     */
    @FXML
    public void handleAuthorAction(ActionEvent event) {
        Desktops.browse(SystemConfig.getAuthor());
    }
    
    /**
     * 检测更新按钮
     * 
     * @param event 事件
     */
    @FXML
    public void handleUpdateAction(ActionEvent event) {
        if(SystemContext.latestRelease()) {
            Alerts.info("检测更新", "当前已是最新版本");
        } else {
            final Optional<ButtonType> optional = Alerts.build("检测更新", "是否下载最新版本？", GuiContext.MessageType.CONFIRM);
            if(Alerts.ok(optional)) {
                Desktops.browse(SystemConfig.getSource());
            }
        }
    }
    
    /**
     * 官网与源码按钮
     * 
     * @param event 事件
     */
    @FXML
    public void handleSourceAction(ActionEvent event) {
        Desktops.browse(SystemConfig.getSource());
    }
    
    /**
     * 问题与建议按钮
     * 
     * @param event 事件
     */
    @FXML
    public void handleSupportAction(ActionEvent event) {
        Desktops.browse(SystemConfig.getSupport());
    }

    /**
     * 设置软件名称
     */
    private void buildName() {
        final StringBuilder builder = new StringBuilder();
        builder.append(SystemConfig.getName())
            .append("（")
            .append(SystemConfig.getNameEn())
            .append("）");
        this.name.setText(builder.toString());
    }
    
    /**
     * 设置软件版本
     */
    private void buildVersion() {
        this.version.setText(SystemConfig.getVersion());
    }

}
