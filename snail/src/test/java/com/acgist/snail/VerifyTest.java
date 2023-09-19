package com.acgist.snail;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.format.XML;
import com.acgist.snail.utils.Performance;

class VerifyTest extends Performance {

    @Test
    void testVersionVerify() throws IOException {
        final String basePath           = Paths.get(System.getProperty("user.dir")).toFile().getParent() + File.separator;
        final String parentPomPath      = basePath + "pom.xml";
        final String snailPomPath       = basePath + "snail/pom.xml";
        final String snailJavaFXPomPath = basePath + "snail-javafx/pom.xml";
        final String systemConfigPath   = basePath + "snail/src/main/resources/config/system.properties";
        final String githubBuildPath    = basePath + ".github/workflows/build.yml";
        final String githubPackagePath  = basePath + ".github/workflows/package.yml";
        final String parentPomVersion   = this.xml(parentPomPath, "version");
        final String javaVersion        = this.xml(parentPomPath, "java.version");
        final String javafxVersion      = this.xml(parentPomPath, "javafx.version");
        this.log("当前版本：{} - {}", parentPomVersion, javaVersion);
        assertEquals(javaVersion, javafxVersion.substring(0, javafxVersion.indexOf('.')));
        final String snailPomVersion = this.xml(snailPomPath, "version");
        assertEquals(parentPomVersion, snailPomVersion);
        final String snailJavaFXPomVersion = this.xml(snailJavaFXPomPath, "version");
        assertEquals(parentPomVersion, snailJavaFXPomVersion);
        final String systemConfigVersion = this.property(systemConfigPath, "acgist.system.version");
        assertEquals(parentPomVersion, systemConfigVersion);
        final String githubBuildVersion = this.githubYml(githubBuildPath, "java-version");
        assertEquals(javaVersion, githubBuildVersion);
        final String githubPackageVersion = this.githubYml(githubPackagePath, "java-version");
        assertEquals(javaVersion, githubPackageVersion);
    }
    
    private String xml(String path, String name) {
        final XML xml = XML.loadFile(path);
        return xml.elementValue(name);
    }
    
    private String property(String path, String name) throws IOException {
        final File file = new File(path);
        final Reader reader = new InputStreamReader(new FileInputStream(file), SystemConfig.DEFAULT_CHARSET);
        final Properties properties = new Properties();
        properties.load(reader);
        return properties.getProperty(name);
    }
    
    private String githubYml(String path, String name) throws IOException {
        return Files.readAllLines(Paths.get(path)).stream()
            .filter(line -> line.strip().startsWith(name))
            .map(line    -> line.strip().substring(name.length() + 1).strip())
            .findFirst().get();
    }
    
    @Test
    void testFormat() throws IOException {
        assertDoesNotThrow(() -> this.format(Paths.get(System.getProperty("user.dir")).toFile().getParentFile()));
    }
    
    void format(File file) throws IOException {
        if (file.isFile()) {
            final String name = file.getName();
            if (
                name.endsWith(".md")         ||
                name.endsWith(".xml")        ||
                name.endsWith(".java")       ||
                name.endsWith(".properties")
            ) {
                Files.readAllLines(file.toPath()).forEach(line -> {
                    if(line.endsWith(" ") && !line.endsWith("* ") && !line.strip().isEmpty()) {
                        this.log("文件格式错误（空格）：{} - {}", file.getAbsolutePath(), line);
                    }
                    if(line.endsWith("    ") && !line.strip().isEmpty()) {
                        this.log("文件格式错误（制表）：{} - {}", file.getAbsolutePath(), line);
                    }
                });
            }
        } else {
            final File[] files = file.listFiles();
            for (File children : files) {
                this.format(children);
            }
        }
    }

}
