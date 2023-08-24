/**
 * Sanil（蜗牛）
 * 基于JavaFX开发的Snail图形管理界面
 * 
 * @author acgist
 */
open module com.acgist.snail.javafx {

    exports com.acgist.main;
    
    requires transitive com.acgist.snail;
    
    requires java.base;
    
    requires transitive java.desktop;
    requires transitive java.scripting;
    
    requires transitive jdk.unsupported;
    
    requires transitive javafx.fxml;
    requires transitive javafx.controls;
    requires transitive javafx.graphics;

}