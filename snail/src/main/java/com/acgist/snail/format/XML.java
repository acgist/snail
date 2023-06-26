package com.acgist.snail.format;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

import com.acgist.snail.config.SystemConfig;
import com.acgist.snail.logger.Logger;
import com.acgist.snail.logger.LoggerFactory;
import com.acgist.snail.utils.StringUtils;

/**
 * XML
 * 
 * @author acgist
 */
public final class XML {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(XML.class);

    /**
     * XML格式化输出
     */
    private static final String DOM_FORMAT_PRETTY_PRINT = "format-pretty-print";
    
    /**
     * 文档
     */
    private Document document;
    
    private XML() {
    }
    
    /**
     * 新建XML
     * 
     * @return XML
     */
    public static final XML build() {
        final XML xml = new XML();
        final DocumentBuilderFactory factory = XML.buildFactory();
        try {
            xml.document = factory.newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            LOGGER.error("新建XML异常", e);
        }
        return xml;
    }
    
    /**
     * 解析XML
     * 
     * @param content XML内容
     * 
     * @return XML
     */
    public static final XML load(String content) {
        Objects.requireNonNull(content, "XML内容为空");
        final XML xml = new XML();
        final DocumentBuilderFactory factory = XML.buildFactory();
        try {
            final DocumentBuilder builder = factory.newDocumentBuilder();
            xml.document = builder.parse(new ByteArrayInputStream(content.getBytes()));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            LOGGER.error("解析XML异常：{}", content, e);
        }
        return xml;
    }
    
    /**
     * 解析XML
     * 
     * @param filePath XML文件路径
     * 
     * @return XML
     */
    public static final XML loadFile(String filePath) {
        String content = null;
        try {
            content = Files.readString(Paths.get(filePath));
        } catch (IOException e) {
            LOGGER.error("读取XML内容异常：{}", filePath, e);
        }
        return XML.load(content);
    }
    
    /**
     * 新建文档构造工厂
     * 
     * @return {@link DocumentBuilderFactory}
     */
    private static final DocumentBuilderFactory buildFactory() {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
        } catch (ParserConfigurationException e) {
            LOGGER.error("新建DocumentBuilderFactory异常", e);
        }
        return factory;
    }

    /**
     * @return 文档
     */
    public Document document() {
        return this.document;
    }
    
    /**
     * 新建节点
     * 
     * @param node 上级节点
     * @param name 节点名称
     * 
     * @return 节点
     * 
     * @see #element(Node, String, String)
     */
    public Element element(Node node, String name) {
        return this.element(node, name, null);
    }
    
    /**
     * 新建节点
     * 
     * @param node 上级节点
     * @param name 节点名称
     * @param text 节点文本
     * 
     * @return 节点
     * 
     * @see #elementNS(Node, String, String, String)
     */
    public Element element(Node node, String name, String text) {
        return this.elementNS(node, name, text, null);
    }
    
    /**
     * 新建节点
     * 
     * @param node         上级节点
     * @param name         节点名称
     * @param namespaceURI 命名空间
     * 
     * @return 节点
     * 
     * @see #elementNS(Node, String, String, String)
     */
    public Element elementNS(Node node, String name, String namespaceURI) {
        return this.elementNS(node, name, null, namespaceURI);
    }
    
    /**
     * 新建节点
     * 
     * @param node         上级节点
     * @param name         节点名称
     * @param text         节点文本
     * @param namespaceURI 命名空间
     * 
     * @return 节点
     */
    public Element elementNS(Node node, String name, String text, String namespaceURI) {
        Objects.requireNonNull(node, "上级节点为空");
        Element element = null;
        if(StringUtils.isEmpty(namespaceURI)) {
            element = this.document.createElement(name);
        } else {
            element = this.document.createElementNS(namespaceURI, name);
        }
        if(StringUtils.isNotEmpty(text)) {
            element.setTextContent(text);
        }
        node.appendChild(element);
        return element;
    }
    
    /**
     * 读取节点文本
     * 如果存在多个节点默认返回第一个节点
     * 
     * @param name 节点名称
     * 
     * @return 节点文本
     */
    public String elementValue(String name) {
        final NodeList list = this.document.getElementsByTagName(name);
        if(list.getLength() == 0) {
            return null;
        }
        return list.item(0).getTextContent();
    }

    /**
     * 读取节点文本列表
     * 
     * @param name 节点名称
     * 
     * @return 节点文本列表
     */
    public List<String> elementValues(String name) {
        final NodeList list = this.document.getElementsByTagName(name);
        final int length = list.getLength();
        if(length == 0) {
            return List.of();
        }
        final List<String> values = new ArrayList<>(length);
        for (int index = 0; index < length; index++) {
            values.add(list.item(index).getTextContent());
        }
        return values;
    }
    
    /**
     * 输出XML
     * 默认不格式化
     * 
     * @return XML
     * 
     * @see #xml(boolean)
     */
    public String xml() {
        return this.xml(false);
    }
    
    /**
     * 输出XML
     * 
     * @param format 是否格式化
     * 
     * @return XML
     */
    public String xml(boolean format) {
        try(final Writer writer = new StringWriter()) {
            final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            final DOMImplementationLS domImplementation = (DOMImplementationLS) registry.getDOMImplementation("LS");
            final LSOutput output = domImplementation.createLSOutput();
            final LSSerializer serializer = domImplementation.createLSSerializer();
            output.setEncoding(SystemConfig.DEFAULT_CHARSET);
            output.setCharacterStream(writer);
            if(format) {
                final DOMConfiguration configuration = serializer.getDomConfig();
                if (configuration.canSetParameter(DOM_FORMAT_PRETTY_PRINT, true)) {
                    configuration.setParameter(DOM_FORMAT_PRETTY_PRINT, true);
                }
            }
            serializer.write(this.document, output);
            return writer.toString();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException | IOException e) {
            LOGGER.error("输出XML异常", e);
        }
        return null;
    }

}
