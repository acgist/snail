package com.acgist.snail.gui.javafx;

import java.io.IOException;

import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.pojo.ITaskSession.FileType;

import javafx.scene.control.Label;
import javafx.scene.text.Font;

/**
 * <p>字体助手</p>
 * 
 * @author acgist
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
	private static final String FONT_NAME = "SnailIcon";
	/**
	 * <p>字体路径：{@value}</p>
	 */
	private static final String FONT_PATH = "/font/SnailIcon.ttf";
	
	/**
	 * <p>字体图标</p>
	 * <p>制作工具：https://icomoon.io/app/#/select</p>
	 * <p>注意：不要改变枚举顺序</p>
	 * 
	 * @author acgist
	 */
	public enum SnailIcon {
		
		AS_HOME,
		AS_HOME2,
		AS_HOME3,
		AS_IMAGE,
		AS_IMAGES,
		AS_CAMERA,
		AS_HEADPHONES,
		AS_MUSIC,
		AS_FILM,
		AS_BOOK,
		AS_FILE_TEXT,
		AS_FILE_EMPTY,
		AS_FILES_EMPTY,
		AS_FILE_TEXT2,
		AS_FILE_PICTURE,
		AS_FILE_MUSIC,
		AS_FILE_PLAY,
		AS_FILE_VIDEO,
		AS_FILE_ZIP,
		AS_COPY,
		AS_FOLDER,
		AS_FOLDER_OPEN,
		AS_FOLDER_PLUS,
		AS_FOLDER_MINUS,
		AS_FOLDER_DOWNLOAD,
		AS_FOLDER_UPLOAD,
		AS_PRICE_TAG,
		AS_QRCODE,
		AS_COIN_YEN,
		AS_ENVELOP,
		AS_HISTORY,
		AS_CLOCK,
		AS_CLOCK2,
		AS_ALARM,
		AS_BELL,
		AS_STOPWATCH,
		AS_DRAWER,
		AS_DRAWER2,
		AS_BOX_ADD,
		AS_BOX_REMOVE,
		AS_DOWNLOAD,
		AS_UPLOAD,
		AS_FLOPPY_DISK,
		AS_DATABASE,
		AS_HOUR_GLASS,
		AS_SPINNER11,
		AS_SEARCH,
		AS_ZOOM_IN,
		AS_ZOOM_OUT,
		AS_ENLARGE,
		AS_SHRINK,
		AS_ENLARGE2,
		AS_SHRINK2,
		AS_LOCK,
		AS_UNLOCKED,
		AS_WRENCH,
		AS_EQUALIZER,
		AS_EQUALIZER2,
		AS_COG,
		AS_COGS,
		AS_STATS_BARS,
		AS_GIFT,
		AS_ROCKET,
		AS_BIN,
		AS_BIN2,
		AS_POWER,
		AS_SWITCH,
		AS_CLIPBOARD,
		AS_CLOUD,
		AS_CLOUD_DOWNLOAD,
		AS_CLOUD_UPLOAD,
		AS_CLOUD_CHECK,
		AS_DOWNLOAD2,
		AS_UPLOAD2,
		AS_DOWNLOAD3,
		AS_UPLOAD3,
		AS_SPHERE,
		AS_EARTH,
		AS_LINK,
		AS_EYE,
		AS_EYE_BLOCKED,
		AS_BOOKMARK,
		AS_BOOKMARKS,
		AS_STAR_EMPTY,
		AS_STAR_FULL,
		AS_HEART,
		AS_HEART_BROKEN,
		AS_WARNING,
		AS_NOTIFICATION,
		AS_QUESTION,
		AS_PLUS,
		AS_MINUS,
		AS_INFO,
		AS_CANCEL_CIRCLE,
		AS_BLOCKED,
		AS_CROSS,
		AS_CHECKMARK,
		AS_CHECKMARK2,
		AS_ENTER,
		AS_EXIT,
		AS_PLAY2,
		AS_PAUSE,
		AS_STOP,
		AS_PREVIOUS,
		AS_NEXT,
		AS_BACKWARD,
		AS_FORWARD2,
		AS_PLAY3,
		AS_PAUSE2,
		AS_STOP2,
		AS_BACKWARD2,
		AS_FORWARD3,
		AS_FIRST,
		AS_LAST,
		AS_PREVIOUS2,
		AS_NEXT2,
		AS_VOLUME_HIGH,
		AS_VOLUME_MEDIUM,
		AS_VOLUME_LOW,
		AS_VOLUME_MUTE,
		AS_VOLUME_MUTE2,
		AS_VOLUME_INCREASE,
		AS_VOLUME_DECREASE,
		AS_LOOP,
		AS_LOOP2,
		AS_SHUFFLE,
		AS_CIRCLE_UP,
		AS_CIRCLE_RIGHT,
		AS_CIRCLE_DOWN,
		AS_CIRCLE_LEFT,
		AS_TAB,
		AS_CROP,
		AS_SHARE,
		AS_NEW_TAB,
		AS_EMBED,
		AS_EMBED2,
		AS_TERMINAL,
		AS_SHARE2,
		AS_DROPBOX,
		AS_ONEDRIVE;
		
		/**
		 * <p>字符开始索引：{@value}</p>
		 * <p>UTF-8编码（E000-F8FF）：自行使用区域</p>
		 */
		private static final char ICON_CHAR_CODE = 0xE900;
		
		static {
			// 注意：必须在枚举内加载字体（否者不能正常加载）
			LOGGER.debug("加载字体：{}-{}", FONT_NAME, FONT_PATH);
			try(final var input = Fonts.class.getResourceAsStream(FONT_PATH)) {
				Font.loadFont(input, FONT_SIZE);
			} catch (IOException e) {
				LOGGER.error("加载字体异常", e);
			}
		}
		
		private SnailIcon() {
		}

		/**
		 * <p>获取图标字符</p>
		 * <p>图标字符：{@link #ICON_CHAR_CODE} + 枚举索引</p>
		 * 
		 * @return 图标字符
		 */
		public final char value() {
			return (char) (ICON_CHAR_CODE + this.ordinal());
		}
		
		/**
		 * <p>获取图标标签</p>
		 * 
		 * @return 图标标签
		 */
		public final Label iconLabel() {
			final Label iconLabel = new Label(this.toString());
			// 设置字体
			iconLabel.setFont(Font.font(FONT_NAME));
			// 添加样式
			Themes.applyClass(iconLabel, Themes.CLASS_SNAIL_ICON);
			return iconLabel;
		}
		
		@Override
		public final String toString() {
			return Character.toString(this.value());
		}
		
	}
	
	private Fonts() {
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
			return SnailIcon.AS_IMAGE.iconLabel();
		case VIDEO:
			return SnailIcon.AS_FILM.iconLabel();
		case AUDIO:
			return SnailIcon.AS_MUSIC.iconLabel();
		case SCRIPT:
			return SnailIcon.AS_TERMINAL.iconLabel();
		case TORRENT:
			return SnailIcon.AS_FOLDER_DOWNLOAD.iconLabel();
		case COMPRESS:
			return SnailIcon.AS_FILE_ZIP.iconLabel();
		case DOCUMENT:
			return SnailIcon.AS_FILE_TEXT2.iconLabel();
		case INSTALL:
			return SnailIcon.AS_DROPBOX.iconLabel();
		case UNKNOWN:
		default:
			return SnailIcon.AS_QUESTION.iconLabel();
		}
	}
	
}
