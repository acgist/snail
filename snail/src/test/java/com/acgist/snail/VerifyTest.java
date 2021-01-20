package com.acgist.snail;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.format.XML;
import com.acgist.snail.utils.Performance;

public class VerifyTest extends Performance {

	private static final String PROJECT_BASE_PATH = "E:/gitee/snail/";
	
	@Test
	public void testVersionVerify() throws IOException {
		final String basePath = PROJECT_BASE_PATH;
		final String parentPomPath = basePath + "pom.xml";
		final String snailPomPath = basePath + "snail/pom.xml";
		final String snailJavaFXPomPath = basePath + "snail-javafx/pom.xml";
		final String builderConfigPath = basePath + "builder/config.bat";
		final String launcherConfigPath = basePath + "SnailLauncher/src/snail.ini";
		final String systemConfigPath = basePath + "snail/src/main/resources/config/system.properties";
		final String parentPomVersion = xml(parentPomPath, "version");
		this.log("当前版本：{}", parentPomVersion);
		final String acgistVersion = xml(parentPomPath, "acgist.version");
		assertEquals(parentPomVersion, acgistVersion);
		final String snailPomVersion = xml(snailPomPath, "version");
		assertEquals(parentPomVersion, snailPomVersion);
		final String snailJavaFXPomVersion = xml(snailJavaFXPomPath, "version");
		assertEquals(parentPomVersion, snailJavaFXPomVersion);
		final String builderConfigVersion = contain(builderConfigPath, acgistVersion);
		assertEquals(parentPomVersion, builderConfigVersion);
		final String launcherConfigVersion = contain(launcherConfigPath, acgistVersion);
		assertEquals(parentPomVersion, launcherConfigVersion);
		final String systemConfigVersion = property(systemConfigPath, "acgist.system.version");
		assertEquals(parentPomVersion, systemConfigVersion);
	}
	
	private String xml(String path, String name) {
		final XML xml = XML.loadFile(path);
		return xml.elementValue(name);
	}
	
	private String contain(String path, String version) throws IOException {
		final String content = Files.readString(Paths.get(path));
		if(content.contains(version)) {
			return version;
		}
		return content;
	}
	
	private String property(String path, String name) throws IOException {
		final File file = new File(path);
		final var input = new InputStreamReader(new FileInputStream(file), SystemConfig.DEFAULT_CHARSET);
		final Properties properties = new Properties();
		properties.load(input);
		return properties.getProperty(name);
	}
	
	@Test
	public void testFormat() throws IOException {
		format(new File(PROJECT_BASE_PATH));
	}
	
	public void format(File file) throws IOException {
		if (file.isFile()) {
			final String name = file.getName();
			if (
				name.endsWith(".properties") ||
				name.endsWith(".java") ||
				name.endsWith(".xml") ||
				name.endsWith(".md")
			) {
				Files.readAllLines(file.toPath()).forEach(line -> {
					if(line.endsWith(" ") && !line.endsWith("* ")) {
						this.log("文件格式错误（空格）：{}-{}", file.getAbsolutePath(), line);
					}
					if(line.endsWith("	") && !line.trim().isEmpty()) {
						this.log("文件格式错误（制表）：{}-{}", file.getAbsolutePath(), line);
					}
				});
			}
		} else {
			var files = file.listFiles();
			for (File children : files) {
				format(children);
			}
		}
	}
	
}
