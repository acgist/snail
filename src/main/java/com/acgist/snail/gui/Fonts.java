package com.acgist.snail.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acgist.snail.pojo.ITaskSession.FileType;

import javafx.scene.control.Label;
import javafx.scene.text.Font;

/**
 * <p>字体工具</p>
 * 
 * @author acgist
 * @since 1.3.0
 */
public final class Fonts {

	private static final Logger LOGGER = LoggerFactory.getLogger(Fonts.class);

	/**
	 * <p>字体大小：{@value}</p>
	 */
	private static final int FONT_SIZE = 16;
	/**
	 * <p>字体名称：{@value}</p>
	 */
	private static final String FONT_NAME = "AcgistSnail";
	/**
	 * <p>字体路径：{@value}</p>
	 */
	private static final String FONT_PATH = "/font/AcgistSnail.ttf";
	/**
	 * <p>图标样式：{@value}</p>
	 */
	private static final String ICON_CLASS = "snail-icon";
	
	static {
		LOGGER.debug("加载字体：{}-{}", FONT_NAME, FONT_PATH);
		try(final var input = Fonts.class.getResourceAsStream(FONT_PATH)) {
			Font.loadFont(input, FONT_SIZE);
		} catch (Exception e) {
			LOGGER.error("字体加载失败", e);
		}
	}

	/**
	 * <p>图标</p>
	 */
	public enum SnailIcon {
		
		HOME             (''),
		HOME2            (''),
		HOME3            (''),
		IMAGE            (''),
		IMAGES           (''),
		HEADPHONES       (''),
		MUSIC            (''),
		FILM             (''),
		FILE_TEXT        (''),
		FILE_EMPTY       (''),
		FILES_EMPTY      (''),
		FILE_TEXT2       (''),
		FILE_ZIP         (''),
		COPY             (''),
		FOLDER           (''),
		FOLDER_OPEN      (''),
		FOLDER_PLUS      (''),
		FOLDER_MINUS     (''),
		FOLDER_DOWNLOAD  (''),
		FOLDER_UPLOAD    (''),
		PRICE_TAG        (''),
		QRCODE           (''),
		COIN_YEN         (''),
		ENVELOP          (''),
		HISTORY          (''),
		CLOCK            (''),
		CLOCK2           (''),
		ALARM            (''),
		BELL             (''),
		STOPWATCH        (''),
		DRAWER           (''),
		DRAWER2          (''),
		BOX_ADD          (''),
		BOX_REMOVE       (''),
		DOWNLOAD         (''),
		UPLOAD           (''),
		FLOPPY_DISK      (''),
		DATABASE         (''),
		SEARCH           (''),
		ZOOM_IN          (''),
		ZOOM_OUT         (''),
		ENLARGE          (''),
		SHRINK           (''),
		ENLARGE2         (''),
		SHRINK2          (''),
		LOCK             (''),
		UNLOCKED         (''),
		WRENCH           (''),
		EQUALIZER        (''),
		EQUALIZER2       (''),
		COG              (''),
		COGS             (''),
		STATS_BARS       (''),
		GIFT             (''),
		ROCKET           (''),
		BIN              (''),
		BIN2             (''),
		POWER            (''),
		SWITCH           (''),
		CLIPBOARD        (''),
		CLOUD            (''),
		CLOUD_DOWNLOAD   (''),
		CLOUD_UPLOAD     (''),
		CLOUD_CHECK      (''),
		DOWNLOAD2        (''),
		UPLOAD2          (''),
		DOWNLOAD3        (''),
		UPLOAD3          (''),
		SPHERE           (''),
		EARTH            (''),
		LINK             (''),
		EYE              (''),
		EYE_BLOCKED      (''),
		BOOKMARK         (''),
		BOOKMARKS        (''),
		STAR_EMPTY       (''),
		STAR_FULL        (''),
		HEART            (''),
		HEART_BROKEN     (''),
		WARNING          (''),
		NOTIFICATION     (''),
		QUESTION         (''),
		PLUS             (''),
		MINUS            (''),
		INFO             (''),
		CANCEL_CIRCLE    (''),
		BLOCKED          (''),
		CROSS            (''),
		CHECKMARK        (''),
		ENTER            (''),
		EXIT             (''),
		PLAY2            (''),
		PAUSE            (''),
		STOP             (''),
		PREVIOUS         (''),
		NEXT             (''),
		BACKWARD         (''),
		FORWARD2         (''),
		PLAY3            (''),
		PAUSE2           (''),
		STOP2            (''),
		BACKWARD2        (''),
		FORWARD3         (''),
		FIRST            (''),
		LAST             (''),
		PREVIOUS2        (''),
		NEXT2            (''),
		VOLUME_HIGH      (''),
		VOLUME_MEDIUM    (''),
		VOLUME_LOW       (''),
		VOLUME_MUTE      (''),
		VOLUME_MUTE2     (''),
		VOLUME_INCREASE  (''),
		VOLUME_DECREASE  (''),
		LOOP             (''),
		LOOP2            (''),
		SHUFFLE          (''),
		CIRCLE_UP        (''),
		CIRCLE_RIGHT     (''),
		CIRCLE_DOWN      (''),
		CIRCLE_LEFT      (''),
		TAB              (''),
		SHARE            (''),
		NEW_TAB          (''),
		EMBED            (''),
		EMBED2           (''),
		TERMINAL         (''),
		SHARE2           (''),
		DROPBOX          (''),
		ONEDRIVE         ('');
		
		/**
		 * <p>图标字符</p>
		 */
		private final char value;
		
		private SnailIcon(char value) {
			this.value = value;
		}

		/**
		 * <p>获取图标字符</p>
		 * 
		 * @return 图标字符
		 */
		public char value() {
			return this.value;
		}
		
		/**
		 * <p>获取图标标签</p>
		 * 
		 * @return 图标标签
		 */
		public Label iconLabel() {
			return Fonts.iconLabel(this);
		}
		
	}
	
	/**
	 * <p>不允许实例化</p>
	 */
	private Fonts() {
	}
	
	/**
	 * <p>获取图标标签</p>
	 * 
	 * @param icon 图标
	 * 
	 * @return 图标标签
	 */
	public static final Label iconLabel(SnailIcon icon) {
		final Label iconLabel = new Label(Character.toString(icon.value()));
		iconLabel.getStyleClass().add(ICON_CLASS); // 添加样式
		iconLabel.setFont(Font.font(FONT_NAME)); // 设置字体
		return iconLabel;
	}
	
	/**
	 * <p>获取文件类型的图标标签</p>
	 * 
	 * @param fileType 文件类型
	 * 
	 * @return 图标标签
	 */
	public static final Label fileTypeIconLabel(FileType fileType) {
		if(fileType == null) {
			fileType = FileType.UNKNOWN;
		}
		switch (fileType) {
		case IMAGE:
			return SnailIcon.IMAGE.iconLabel();
		case VIDEO:
			return SnailIcon.FILM.iconLabel();
		case AUDIO:
			return SnailIcon.MUSIC.iconLabel();
		case SCRIPT:
			return SnailIcon.TERMINAL.iconLabel();
		case TORRENT:
			return SnailIcon.FOLDER_DOWNLOAD.iconLabel();
		case COMPRESS:
			return SnailIcon.FILE_ZIP.iconLabel();
		case DOCUMENT:
			return SnailIcon.FILE_TEXT2.iconLabel();
		case INSTALL:
			return SnailIcon.DROPBOX.iconLabel();
		case UNKNOWN:
		default:
			return SnailIcon.QUESTION.iconLabel();
		}
	}
	
}
