package com.acgist.snail.system.format;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.acgist.snail.system.config.SystemConfig;
import com.acgist.snail.utils.StringUtils;

/**
 * <p>XML</p>
 * 
 * @author acgist
 * @since 1.0.0
 */
public final class XML {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(XML.class);

	/**
	 * <p>XML格式化输出：{@value}</p>
	 */
	private static final String DOM_FORMAT_PRETTY_PRINT = "format-pretty-print";
	
	/**
	 * <p>根节点</p>
	 */
	private Document document;
	
	/**
	 * <p>创建XML</p>
	 * 
	 * @return XML工具对象
	 */
	public static final XML build() {
		final XML xml = new XML();
		final DocumentBuilderFactory factory = buildFactory();
		try {
			xml.document = factory.newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			LOGGER.error("创建XML异常", e);
		}
		return xml;
	}
	
	/**
	 * <p>解析XML</p>
	 * 
	 * @param content XML内容
	 * 
	 * @return XML工具对象
	 */
	public static final XML load(String content) {
		final XML xml = new XML();
		final DocumentBuilderFactory factory = buildFactory();
		try {
			final DocumentBuilder builder = factory.newDocumentBuilder();
			xml.document = builder.parse(new ByteArrayInputStream(content.getBytes()));
		} catch (ParserConfigurationException | SAXException | IOException e) {
			LOGGER.info("解析XML异常：{}", content, e);
		}
		return xml;
	}
	
	/**
	 * <p>创建{@code DocumentBuilderFactory}</p>
	 * 
	 * @return {@code DocumentBuilderFactory}
	 */
	private static final DocumentBuilderFactory buildFactory() {
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			// 防止实体注入
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			factory.setXIncludeAware(false);
			factory.setExpandEntityReferences(false);
		} catch (ParserConfigurationException e) {
			LOGGER.error("设置DocumentBuilderFactory异常 ", e);
		}
		return factory;
	}

	/**
	 * @return 根节点
	 */
	public Document document() {
		return this.document;
	}
	
	/**
	 * <p>创建节点</p>
	 * 
	 * @param node 父节点
	 * @param name 节点名称
	 * 
	 * @return 节点
	 */
	public Element element(Node node, String name) {
		return element(node, name, null);
	}
	
	/**
	 * <p>创建节点</p>
	 * 
	 * @param node 父节点
	 * @param name 节点名称
	 * @param text 节点文本
	 * 
	 * @return 节点
	 */
	public Element element(Node node, String name, String text) {
		return elementNS(node, name, text, null);
	}
	
	/**
	 * <p>创建节点</p>
	 * 
	 * @param node 父节点
	 * @param name 节点名称
	 * @param namespaceUri 命名空间
	 * 
	 * @return 节点
	 */
	public Element elementNS(Node node, String name, String namespaceUri) {
		return elementNS(node, name, null, namespaceUri);
	}
	
	/**
	 * <p>创建节点</p>
	 * 
	 * @param node 父节点
	 * @param name 节点名称
	 * @param text 节点文本
	 * @param namespaceUri 命名空间
	 * 
	 * @return 节点
	 */
	public Element elementNS(Node node, String name, String text, String namespaceUri) {
		Element element = null;
		if(StringUtils.isEmpty(namespaceUri)) {
			element = this.document.createElement(name);
		} else {
			element = this.document.createElementNS(namespaceUri, name);
		}
		if(StringUtils.isNotEmpty(text)) {
			element.setTextContent(text);
		}
		node.appendChild(element);
		return element;
	}

	/**
	 * <p>读取节点文本</p>
	 * <p>如果存在多个节点默认返回第一个节点</p>
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
	 * <p>读取节点文本列表</p>
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
	 * <p>输出XML</p>
	 * <p>默认不格式化</p>
	 * 
	 * @return XML
	 */
	public String xml() {
		return xml(false);
	}
	
	/**
	 * <p>输出XML</p>
	 * 
	 * @param format 是否格式化
	 * 
	 * @return XML
	 */
	public String xml(boolean format) {
		try(final Writer writer = new StringWriter()) { // 可以不用关闭
			final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
			final DOMImplementationLS ementation = (DOMImplementationLS) registry.getDOMImplementation("LS");
			final LSOutput output = ementation.createLSOutput();
			final LSSerializer serializer = ementation.createLSSerializer();
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
