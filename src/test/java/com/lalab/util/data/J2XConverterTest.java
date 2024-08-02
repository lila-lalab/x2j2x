// MIT License

// Copyright (c) 2024 lila-lalab

// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:

// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.

// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.


package com.lalab.util.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class J2XConverterTest {
    private J2XConverterConfig config;
    private J2XConverter converter;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        config = new J2XConverterConfig();
        config.rootName = "root";
        config.namespace = "http://example.com";
        config.alias = "ex";
        config.createNamespace = true;
        config.wrapSoapEnvelope = false;
        config.ignoreXmlAttribute = false;
        config.xmlAttributePrefix = "@";
        config.xmlValueFieldName = "_value";
        config.unnamedArrXmlNodeName = "item";
        config.ignoreEmptyArray = false;

        converter = new J2XConverter(config);
        objectMapper = new ObjectMapper();
    }

    @Test
    void testSimpleJsonToXml() throws Exception {
        String jsonString = "{\"name\":\"John\", \"age\":30}";
        JsonNode jsonNode = objectMapper.readTree(jsonString);
        config.createNamespace = false;
        Document doc = converter.j2x(jsonNode);

        Element root = doc.getDocumentElement();
        assertEquals("root", root.getNodeName());
        assertEquals("John", root.getElementsByTagName("name").item(0).getTextContent());
        assertEquals("30", root.getElementsByTagName("age").item(0).getTextContent());
    }

    @Test
    void testJsonWithAttributes() throws Exception {
        config.ignoreXmlAttribute = false;
        String jsonString = "{\"person\":{\"@id\":\"123\",\"name\":\"John\"}}";
        JsonNode jsonNode = objectMapper.readTree(jsonString);
        Document doc = converter.j2x(jsonNode);

        Element root = doc.getDocumentElement();
        Element person = (Element) root.getElementsByTagName("person").item(0);
        assertEquals("123", person.getAttribute("id"));
        assertEquals("John", person.getElementsByTagName("name").item(0).getTextContent());
    }

    @Test
    void testJsonArray() throws Exception {
        String jsonString = "{\"people\":[{\"name\":\"John\"},{\"name\":\"Jane\"}]}";
        JsonNode jsonNode = objectMapper.readTree(jsonString);
        Document doc = converter.j2x(jsonNode);

        Element root = doc.getDocumentElement();
        assertEquals(2, root.getElementsByTagName("people").getLength());
        assertEquals("John", root.getElementsByTagName("people").item(0).getTextContent());
        assertEquals("Jane", root.getElementsByTagName("people").item(1).getTextContent());
    }

    @Test
    void testJsonWithEmptyArray() throws IOException, ParserConfigurationException {
        config.ignoreEmptyArray = false;
        String jsonString = "{\"people\":[]}";
        JsonNode jsonNode = objectMapper.readTree(jsonString);
        Document doc = converter.j2x(jsonNode);

        Element root = doc.getDocumentElement();
        assertEquals(1, root.getElementsByTagName("people").getLength());
    }

    @Test
    void testJsonToSoapEnvelope() throws Exception {
        config.wrapSoapEnvelope = true;
        config.soapVersion = "1.2";
        config.soapBodyAsRoot = false;
        config.rootName = "request";
        config.createNamespace = false;
        String jsonString = "{\"name\":\"John\", \"age\":30}";
        JsonNode jsonNode = objectMapper.readTree(jsonString);
        Document doc = converter.j2x(jsonNode);

        Element envelope = doc.getDocumentElement();
        assertEquals("soapenv:Envelope", envelope.getNodeName());
        Element body = (Element) envelope.getElementsByTagName("soapenv:Body").item(0);
        Element request = (Element) body.getElementsByTagName("request").item(0);
        assertEquals("John", request.getElementsByTagName("name").item(0).getTextContent());
        assertEquals("30", request.getElementsByTagName("age").item(0).getTextContent());
    }

    @Test
    void testJsonWithNamespace() throws IOException, ParserConfigurationException {
        config.createNamespace = true;
        config.namespace = "http://example.com";
        config.alias = "ex";
        String jsonString = "{\"name\":\"John\", \"age\":30}";
        JsonNode jsonNode = objectMapper.readTree(jsonString);
        Document doc = converter.j2x(jsonNode);

        Element root = doc.getDocumentElement();
        assertEquals("ex:root", root.getNodeName());
        assertEquals("John", root.getElementsByTagName("name").item(0).getTextContent());
        assertEquals("30", root.getElementsByTagName("age").item(0).getTextContent());
    }
}