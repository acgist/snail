package com.acgist.snail.gui.javafx;

import com.acgist.snail.config.SymbolConfig;
import com.acgist.snail.context.SystemContext.SystemType;
import com.acgist.snail.gui.javafx.theme.ITheme;
import com.acgist.snail.gui.javafx.theme.local.WindowsTheme;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;

import javafx.collections.ObservableList;
import javafx.css.Styleable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * 主题助手
 * 
 * 使用CMD命令获取（可以使用JNI调用系统接口替换）
 * 
 * @author acgist
 */
public final class Themes {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Themes.class);

    /**
     * FXML样式路径：{@value}
     */
    public static final String FXML_STYLE = "/style/fxml.css";
    /**
     * LOGO文件路径（16PX）：{@value}
     */
    public static final String LOGO_ICON_16 = "/image/16/logo.png";
    /**
     * LOGO文件路径（32PX）：{@value}
     */
    public static final String LOGO_ICON_32 = "/image/32/logo.png";
    /**
     * LOGO文件路径（64PX）：{@value}
     */
    public static final String LOGO_ICON_64 = "/image/64/logo.png";
    /**
     * LOGO文件路径（128PX）：{@value}
     */
    public static final String LOGO_ICON_128 = "/image/128/logo.png";
    /**
     * LOGO文件路径（256PX）：{@value}
     */
    public static final String LOGO_ICON_256 = "/image/logo.png";
    /**
     * 红色：禁用
     */
    public static final Color COLOR_RED = Color.rgb(0xDD, 0x33, 0x55);
    /**
     * 灰色：禁用
     */
    public static final Color COLOR_GRAY = Color.rgb(0xCC, 0xCC, 0xCC);
    /**
     * 蓝色：可用
     */
    public static final Color COLOR_BLUD = Color.rgb(0x00, 0x99, 0xCC);
    /**
     * 绿色：可用
     */
    public static final Color COLOR_GREEN = Color.rgb(0x22, 0xAA, 0x22);
    /**
     * 黄色：警告
     */
    public static final Color COLOR_YELLOW = Color.rgb(0xFF, 0xEE, 0x99);
    /**
     * 托盘样式
     */
    public static final String CLASS_TRAY = "tray";
    /**
     * 图标样式
     */
    public static final String CLASS_SNAIL_ICON = "snail-icon";
    /**
     * 没有任务样式
     */
    public static final String CLASS_TASK_EMPTY = "placeholder";
    /**
     * 系统信息样式
     */
    public static final String CLASS_SYSTEM_INFO = "system-info";
    /**
     * 画图信息样式
     */
    public static final String CLASS_PAINTER_INFO = "painter-info";
    /**
     * 统计信息样式
     */
    public static final String CLASS_STATISTICS_INFO = "statistics-info";
    /**
     * 默认主题颜色
     */
    public static final Color DEFAULT_THEME_COLOR = Themes.COLOR_BLUD;
    /**
     * 系统主题颜色
     */
    private static final Color SYSTEM_THEME_COLOR;
    /**
     * 系统主题样式
     */
    private static final String SYSTEM_THEME_STYLE;

    static {
        // 设置系统主题颜色
        final ITheme themeHandler;
        final SystemType systemType = SystemType.local();
        if(systemType == SystemType.WINDOWS) {
            themeHandler = WindowsTheme.newInstance();
        } else {
            themeHandler = null;
            LOGGER.info("没有适配系统主题类型：{}", systemType);
        }
        final Color color;
        if(themeHandler == null) {
            color = DEFAULT_THEME_COLOR;
        } else {
            color = themeHandler.systemThemeColor();
        }
        SYSTEM_THEME_COLOR = color;
        // 设置系统主题样式
        final String        colorHex   = SYSTEM_THEME_COLOR.toString();
        final StringBuilder themeStyle = new StringBuilder();
        themeStyle
            .append("-fx-snail-main-color:#")
            // 十六进制颜色：0x + RRGGBB + OPACITY
            .append(colorHex, 2, 8)
            .append(SymbolConfig.Symbol.SEMICOLON.toString());
        SYSTEM_THEME_STYLE = themeStyle.toString();
        LOGGER.debug("系统主题颜色：{}", SYSTEM_THEME_COLOR);
        LOGGER.debug("系统主题样式：{}", SYSTEM_THEME_STYLE);
    }
    
    private Themes() {
    }
    
    /**
     * @return Logo图标
     */
    public static final Image[] getLogo() {
        return new Image[] {
            new Image(LOGO_ICON_16),
            new Image(LOGO_ICON_32),
            new Image(LOGO_ICON_64),
            new Image(LOGO_ICON_128),
            new Image(LOGO_ICON_256)
        };
    }
    
    /**
     * @return 系统主题颜色
     */
    public static final Color getColor() {
        return SYSTEM_THEME_COLOR;
    }
    
    /**
     * @return 系统主题样式
     */
    public static final String getStyle() {
        return SYSTEM_THEME_STYLE;
    }
    
    /**
     * 设置Logo图标
     * 
     * @param icons 图标列表
     */
    public static final void applyLogo(ObservableList<Image> icons) {
        icons.addAll(Themes.getLogo());
    }
    
    /**
     * 设置场景主题样式
     * 
     * @param scene 场景
     */
    public static final void applyStyle(Scene scene) {
        final Parent root = scene.getRoot();
        Themes.applyStyle(root);
        Themes.applyStylesheet(root);
    }

    /**
     * 设置节点主题样式
     * 
     * @param node 节点
     */
    public static final void applyStyle(Node node) {
        node.setStyle(Themes.getStyle());
    }
    
    /**
     * 设置节点样式文件
     * 
     * @param parent 节点
     */
    public static final void applyStylesheet(Parent parent) {
        parent.getStylesheets().add(Themes.FXML_STYLE);
    }
    
    /**
     * 添加样式
     * 
     * @param styleable  样式列表
     * @param styleClass 样式名称
     */
    public static final void applyClass(Styleable styleable, String styleClass) {
        styleable.getStyleClass().add(styleClass);
    }
    
}
