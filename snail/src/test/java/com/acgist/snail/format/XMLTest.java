package com.acgist.snail.format;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.acgist.snail.utils.Performance;

class XMLTest extends Performance {

    @Test
    void testXML() {
        final XML xml = XML.build();
        final String text = "text";
        final var root = xml.element(xml.document(), "root");
        xml.element(root, "nodeEmpty");
        xml.element(root, "nodeText", text);
        xml.elementNS(root, "s:nodeEmpty", "https://www.acgist.com");
        var element = xml.elementNS(root, "s:nodeText", "text", "https://www.acgist.com");
        element.setAttribute("color", "#FFF");
        this.log(xml.xml());
        this.log(xml.xml(true));
        assertNotNull(xml);
        final XML readXml = XML.load(xml.xml(true));
        this.log(readXml.elementValue("nodeEmpty"));
        this.log(readXml.elementValue("nodeText"));
        this.log(readXml.elementValues("s:nodeEmpty"));
        this.log(readXml.elementValues("s:nodeText"));
        assertEquals(text, readXml.elementValue("nodeText"));
        assertEquals(text, readXml.elementValue("s:nodeText"));
    }

    @Test
    void testCosted() {
        long costed = this.costed(10000, () -> {
            final XML xml = XML.build();
            var root = xml.element(xml.document(), "root");
            xml.element(root, "nodeEmpty");
            xml.element(root, "nodeText", "text");
            xml.elementNS(root, "s:nodeEmpty", "https://www.acgist.com");
            xml.elementNS(root, "s:nodeText", "text", "https://www.acgist.com");
            xml.xml();
        });
        final String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><nodeEmpty/><nodeText>text</nodeText><s:nodeEmpty xmlns:s=\"https://www.acgist.com\"/><s:nodeText xmlns:s=\"https://www.acgist.com\" color=\"#FFF\">text</s:nodeText></root>";
        assertTrue(costed < 10000);
        costed = this.costed(10000, () -> {
            final XML xml = XML.load(content);
            xml.elementValue("nodeEmpty");
            xml.elementValue("nodeText");
            xml.elementValues("s:nodeEmpty");
            xml.elementValues("s:nodeText");
        });
        assertTrue(costed < 10000);
    }
    
}
