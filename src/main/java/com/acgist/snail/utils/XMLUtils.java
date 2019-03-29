package com.acgist.snail.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

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

/**
 * XML工具
 */
public class XMLUtils {

	private static final String DOM_FORMAT_PRETTY_PRINT = "format-pretty-print";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(XMLUtils.class);
	
	private Document document;
	
	/**
	 * 创建XML
	 */
	public static final XMLUtils create() {
		XMLUtils utils = new XMLUtils();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			utils.document = factory.newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException e) {
			LOGGER.error("XML创建异常", e);
		}
		return utils;
	}
	
	/**
	 * 解析XML
	 */
	public static final XMLUtils load(String xml) {
		XMLUtils utils = new XMLUtils();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			utils.document = builder.parse(new ByteArrayInputStream(xml.getBytes()));
		} catch (ParserConfigurationException | SAXException | IOException e) {
			LOGGER.info("XML解析异常：{}", xml, e);
		}
		return utils;
	}

	/**
	 * 获取节点
	 */
	public Document document() {
		return this.document;
	}
	
	/**
	 * 创建标签
	 */
	public Element element(Node node, String name) {
		return element(node, name, null);
	}
	
	/**
	 * 创建标签
	 */
	public Element element(Node node, String name, String text) {
		return elementNS(node, name, text, null);
	}
	
	/**
	 * 创建标签
	 */
	public Element elementNS(Node node, String name, String namespaceURI) {
		return elementNS(node, name, null, namespaceURI);
	}
	
	/**
	 * 创建标签
	 */
	public Element elementNS(Node node, String name, String text, String namespaceURI) {
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
	 * 读取节点值：多个节点获取第一个
	 */
	public String elementValue(String name) {
		NodeList list = this.document.getElementsByTagName(name);
		if(list.getLength() == 0) {
			return null;
		}
		return list.item(0).getTextContent();
	}
	
	/**
	 * XML格式输出
	 */
	public String xml() {
		return xml(false);
	}
	
	public String xml(boolean format) {
		try {
			final Writer out = new StringWriter();
			final DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
			final DOMImplementationLS ementation = (DOMImplementationLS) registry.getDOMImplementation("LS");
			final LSOutput output = ementation.createLSOutput();
			final LSSerializer serializer = ementation.createLSSerializer();
			output.setEncoding(SystemConfig.DEFAULT_CHARSET);
			output.setCharacterStream(out);
			if(format) {
				DOMConfiguration configuration = serializer.getDomConfig();
				if (configuration.canSetParameter(DOM_FORMAT_PRETTY_PRINT, true)) {
					configuration.setParameter(DOM_FORMAT_PRETTY_PRINT, true);
				}
			}
			serializer.write(this.document, output);
			return out.toString();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException e) {
			LOGGER.error("XML格式化异常", e);
		}
		return null;
	}

}
