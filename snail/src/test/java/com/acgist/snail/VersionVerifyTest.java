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

public class VersionVerifyTest extends Performance {

	/**
	 * <p>版本校验</p>
	 */
	@Test
	public void testVerify() throws IOException {
		final String basePath = "E:/gitee/snail/";
		final String parentPomPath = basePath + "pom.xml";
		final String snailPomPath = basePath + "snail/pom.xml";
		final String snailJavafxPomPath = basePath + "snail-javafx/pom.xml";
		final String builderConfigPath = basePath + "builder/config.bat";
		final String launcherConfigPath = basePath + "SnailLauncher/src/snail.ini";
		final String systemConfigPath = basePath + "snail/src/main/resources/config/system.properties";
		final String parentPomVersion = xml(parentPomPath, "version");
		this.log("当前版本：{}", parentPomVersion);
		final String acgistVersion = xml(parentPomPath, "acgist.version");
		assertEquals(parentPomVersion, acgistVersion);
		final String snailPomVersion = xml(snailPomPath, "version");
		assertEquals(parentPomVersion, snailPomVersion);
		final String snailJavafxPomVersion = xml(snailJavafxPomPath, "version");
		assertEquals(parentPomVersion, snailJavafxPomVersion);
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
	
}
