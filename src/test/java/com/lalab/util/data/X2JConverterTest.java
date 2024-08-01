package com.lalab.util.data;

import com.fasterxml.jackson.databind.JsonNode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static org.junit.jupiter.api.Assertions.*;

class X2JConverterTest {
    private X2JConverterConfig config;

    @BeforeEach
    public void setUp() {
        config = new X2JConverterConfig();
    }

    @Test
    void testSimpleElementConversion() throws Exception {
        X2JConverter converter = new X2JConverter(config);
        Document doc = createDocument("<root><name>John</name></root>");
        JsonNode json = converter.x2J(doc);

        assertTrue(json.isObject());
        assertTrue(json.has("name"));
        assertEquals("John", json.get("name").asText());
    }
    @Test
    void testNameSpaceTrimming() throws Exception {
        config.removeNamespaceAlias=true;
        X2JConverter converter = new X2JConverter(config);
        Document doc = createDocument("<root:root><body:name>John</body:name></root:root>");
        JsonNode json = converter.x2J(doc);
        System.out.println(json.toString());

        assertTrue(json.isObject());
        assertTrue(json.has("name"));
        assertEquals("John", json.get("name").asText());
    }

    @Test
    void testNestedElementConversion() throws Exception {
        X2JConverter converter = new X2JConverter(config);
        Document doc = createDocument("<root><person><name>John</name><age>30</age></person></root>");
        JsonNode json = converter.x2J(doc);

        assertTrue(json.isObject());
        assertFalse(json.at("/person").isMissingNode());

        JsonNode personNode = json.at("/person");
        assertTrue(personNode.isObject());
        assertTrue(personNode.has("name"));
        assertTrue(personNode.has("age"));

        assertEquals("John", personNode.get("name").asText());
        assertEquals(30, personNode.get("age").asInt());
    }

    @Test
    void testMultipleOccurrencesConversion() throws Exception {
        X2JConverter converter = new X2JConverter(config);
        Document doc = createDocument("<root><person><name>John</name></person><person><name>Jane</name></person></root>");
        JsonNode json = converter.x2J(doc);

        assertTrue(json.isObject());
        assertTrue(json.has("person"));

        JsonNode personNode = json.get("person");
        assertTrue(personNode.isArray());
        assertEquals(2, personNode.size());

        assertEquals("John", personNode.get(0).get("name").asText());
        assertEquals("Jane", personNode.get(1).get("name").asText());
    }

    @Test
    void testXmlAttributeConversion() throws Exception {
        config.ignoreXmlAttribute = false;
        X2JConverter converter = new X2JConverter(config);
        Document doc = createDocument("<root><person id=\"123\"><name>John</name>person</person></root>");
        JsonNode json = converter.x2J(doc);

        assertTrue(json.isObject());
        assertTrue(json.has("person"));

        JsonNode personNode = json.get("person");
        assertTrue(personNode.isObject());
        assertTrue(personNode.has("@id"));
        assertTrue(personNode.has("name"));

        assertEquals("123", personNode.get("@id").asText());
        assertEquals("John", personNode.get("name").asText());
    }

    @Test
    void testSOAPUnwrap() throws Exception {
        config.ignoreXmlAttribute = false;
        config.tearSOAPEnvelope = true;
        X2JConverter converter = new X2JConverter(config);
        Document doc = createDocument("<soapenv:Envelope xmlns:soapenv = \"http://www.w3.org/2003/05/soap-envelope\"><soapenv:Body><person id=\"123\"><name>John</name>person</person></soapenv:Body></soapenv:Envelope>");
        JsonNode json = converter.x2J(doc);

        assertTrue(json.isObject());
        assertTrue(json.has("person"));

        JsonNode personNode = json.get("person");
        assertTrue(personNode.isObject());
        assertTrue(personNode.has("@id"));
        assertTrue(personNode.has("name"));

        assertEquals("123", personNode.get("@id").asText());
        assertEquals("John", personNode.get("name").asText());
    }

    private Document createDocument(String docString) throws ParserConfigurationException,IOException,SAXException{
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(docString)));
    }
}