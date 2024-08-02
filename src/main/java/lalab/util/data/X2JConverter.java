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
package lalab.util.data;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

/**
 * A converter class to convert XML data to JSON format.
 */
public class X2JConverter {
    private X2JConverterConfig config;
    private ObjectMapper objectMapper;
    private String trimPattern;

    /**
     * Constructs an X2JConverter with the specified configuration.
     *
     * @param config the configuration for the converter
     */
    public X2JConverter(X2JConverterConfig config) {
        this.config = config;
        this.objectMapper = new ObjectMapper();
        
        if (config.trimNewLine && config.trimWhitespace)
            this.trimPattern = REGEX_NEWLINEANDSPACE;
        else if (config.trimNewLine) 
            this.trimPattern = REGEX_NEWLINEONLY;
        else if (config.trimWhitespace) 
            this.trimPattern = REGEX_SPACEONLY;
        else
            this.trimPattern = null;
    }

    /**
     * Converts an XML Document to a JSON node.
     *
     * @param doc the XML Document to convert
     * @return the JSON node representing the XML data
     */
    public JsonNode x2J(Document doc) {
        Node root;
        if (config.tearSOAPEnvelope) {
            if (config.includeRoot) {
                root = doc.getDocumentElement();
                NodeList soapComps = root.getChildNodes();
                for (int i = 0; i < soapComps.getLength(); i++) {
                    Node comp = soapComps.item(i);
                    if (!comp.getNodeName().endsWith("Body")) {
                        root.removeChild(comp);
                    }
                }
            } else {
                NamedNodeMap attributes = doc.getDocumentElement().getAttributes();
                String namespaceAlias = "";
                if (!config.ignoreXmlAttribute && attributes.getLength() > 0) {
                    for (int i = 0; i < attributes.getLength(); i++) {
                        Node attribute = attributes.item(i);
                        if (attribute.getNodeName().startsWith("xmlns"))
                            namespaceAlias = attribute.getNodeName().substring(6);
                    }
                }
                
                root = doc.getDocumentElement().getElementsByTagName(namespaceAlias + ":Body").item(0);
            }
        } else {
            if (config.includeRoot) root = doc;
            else root = doc.getDocumentElement();
        }
        
        return handleNumberFields(handleBooleanFields(handleArrFields(convertToJsonNode(root))));
    }

    /**
     * Writes the JSON representation of an XML Document to a writer.
     *
     * @param doc the XML Document to convert
     * @param writer the writer to write the JSON data to
     * @throws IOException if an I/O error occurs
     */
    public void writex2J(Document doc, Writer writer) throws IOException {
        objectMapper.writeTree(objectMapper.createGenerator(writer), x2J(doc));
    }

    /**
     * Trims the namespace alias from an XML node name.
     *
     * @param xmlNodeName the XML node name to trim
     * @return the trimmed XML node name
     */
    public String trimNodeName(String xmlNodeName) {
        if (config.removeNamespaceAlias) {
            String[] namespaceSplitted = xmlNodeName.split(":");
            if (namespaceSplitted.length > 1) return xmlNodeName.substring(namespaceSplitted[0].length() + 1);
        }
        return xmlNodeName;
    }

    /**
     * Converts an XML node to a JSON node.
     *
     * @param xml the XML node to convert
     * @return the JSON node representing the XML data
     */
    private JsonNode convertToJsonNode(Node xml) {
        NodeList children = xml.getChildNodes();
        NamedNodeMap attributes = xml.getAttributes();
        Map<String, String> attrMap = new LinkedHashMap<>();
        if (!config.ignoreXmlAttribute && attributes.getLength() > 0) {
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attribute = attributes.item(i);
                String nodeName = attribute.getNodeName();
                if (!config.ignoreXsdTypeAttr || !nodeName.startsWith("xsd:"))
                    attrMap.put(trimNodeName(nodeName), trimStrValue(attribute.getNodeValue()));
            }
        }
        String value = null;
        Map<String, List<Node>> elements = new LinkedHashMap<>();
        if (children.getLength() >= 1) {
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (!(child instanceof Element)) {
                    value = trimStrValue(child.getNodeValue());
                    continue;
                }
                String nodeName = trimNodeName(child.getNodeName());
                elements.computeIfAbsent(nodeName, k -> new ArrayList<>()).add(child);
            }
        }
        if (!elements.isEmpty() || !attrMap.isEmpty()) {
            ObjectNode node = objectMapper.createObjectNode();
            for (Entry<String, String> attr : attrMap.entrySet()) {
                node.put(config.xmlAttributePrefix + attr.getKey(), attr.getValue());
            }
            for (Entry<String, List<Node>> items : elements.entrySet()) {
                if (items.getValue().size() == 1) {
                    Node item = items.getValue().get(0);
                    JsonNode target = convertToJsonNode(item);
                    node.set(trimNodeName(item.getNodeName()), target);
                } else {
                    ArrayNode array = objectMapper.createArrayNode();
                    for (Node xmlNode : items.getValue()) {
                        JsonNode target = convertToJsonNode(xmlNode);
                        array.add(target);
                    }
                    node.set(trimNodeName(items.getKey()), array);
                }
            }
            if (value != null) {
                String trimmed = trimStrValue(value);
                if (!trimmed.isEmpty())
                    node.put(config.xmlValueFieldName, trimmed);
            }
            return node;
        }
        if (value == null) value = config.nullAsEmptyString ? "" : null;
        return new TextNode(trimStrValue(value));
    }

    private static final String REGEX_NEWLINEONLY = "^[\\n\\r]+|[\\n\\r]+$";
    private static final String REGEX_SPACEONLY = "^\\s+|\\s+$";
    private static final String REGEX_NEWLINEANDSPACE = "^[\\n\\r\\s]+|[\\n\\r\\s]+$";

    /**
     * Trim the leading whitespace or newline character according to the configuration
     * @param value xml field value
     * @return trimmed value data
     */
    private String trimStrValue(String value) {
        if (value == null) return null;
        else if (this.trimPattern == null) return value;
        else return value.replaceAll(this.trimPattern, "");
    }

    private final Pattern findLastPathNode = Pattern.compile("/(?:.(?!/))+$");

    /**
     * Check JSON array fields and convert the node into an array if it's not.
     * @param json generated json root node
     * @return modified json root node
     */
    private JsonNode handleArrFields(JsonNode json) {
        for (String pathStr : config.xmlArrayFields) {
            Matcher matcher = findLastPathNode.matcher(pathStr);
            String nodeName;
            JsonPointer parent;
            if (matcher.matches()) {
                nodeName = matcher.group(1);
                parent = JsonPointer.compile(pathStr.substring(0, pathStr.length() - nodeName.length()));
                JsonNode parentNode = json.at(parent);
                if (parentNode.isObject()) {
                    JsonNode target = json.at(nodeName);
                    if (!target.isArray()) {
                        ArrayNode arr = objectMapper.createArrayNode();
                        arr.add(json);
                        ((ObjectNode) parentNode).set(nodeName.replaceFirst("/", ""), arr);
                    }
                }
            } else {
                if (pathStr.equals("/")) {
                    ArrayNode node = objectMapper.createArrayNode();
                    node.add(json);
                } else {
                    throw new InvalidJsonPointerException(pathStr);
                }
            }
        }
        return json;
    }

    /**
     * Check JSON number fields and parse the string
     * @param json generated json root node
     * @return modified json root node
     */
    private JsonNode handleNumberFields(JsonNode json) {
        for (String pathStr : config.xmlNumberFields) {
            Matcher matcher = findLastPathNode.matcher(pathStr);
            String nodeName;
            JsonPointer parent;
            if (matcher.matches()) {
                nodeName = matcher.group(1);
                parent = JsonPointer.compile(pathStr.substring(0, pathStr.length() - nodeName.length()));
                JsonNode parentNode = json.at(parent);
                if (parentNode.isObject()) {
                    JsonNode target = json.at(nodeName);
                    if (target.isTextual()) {
                        String value = target.asText();
                        try {
                            if (value.contains(".")) {
                                ((ObjectNode) parentNode).put(nodeName.replaceFirst("/", ""), Double.parseDouble(pathStr));
                            } else {
                                ((ObjectNode) parentNode).put(nodeName.replaceFirst("/", ""), Long.parseLong(pathStr));
                            }
                        } catch (NumberFormatException e) {
                            // do nothing
                        }
                    }
                }
            } else {
                if (pathStr.equals("/")) {
                    ArrayNode node = objectMapper.createArrayNode();
                    node.add(json);
                } else {
                    throw new InvalidJsonPointerException(pathStr);
                }
            }
        }
        return json;
    }

    /**
     * Check JSON number fields and parse the boolean
     * @param json generated json root node
     * @return modified json root node
     */
    private JsonNode handleBooleanFields(JsonNode json) {
        for (String pathStr : config.xmlBooleanFields) {
            Matcher matcher = findLastPathNode.matcher(pathStr);
            String nodeName;
            JsonPointer parent;
            if (matcher.matches()) {
                nodeName = matcher.group(1);
                parent = JsonPointer.compile(pathStr.substring(0, pathStr.length() - nodeName.length()));
                JsonNode parentNode = json.at(parent);
                if (parentNode.isObject()) {
                    JsonNode target = json.at(nodeName);
                    if (target.isTextual()) {
                        String value = target.asText();
                        if (value.equalsIgnoreCase("TRUE"))
                            ((ObjectNode) parentNode).put(nodeName.replaceFirst("/", ""), true);
                        else if (value.equalsIgnoreCase("FALSE"))
                            ((ObjectNode) parentNode).put(nodeName.replaceFirst("/", ""), false);
                    }
                }
            } else {
                if (pathStr.equals("/")) {
                    ArrayNode node = objectMapper.createArrayNode();
                    node.add(json);
                } else {
                    throw new InvalidJsonPointerException(pathStr);
                }
            }
        }
        return json;
    }

    /**
     * Exception thrown when an invalid JSON Pointer expression is encountered.
     */
    public static class InvalidJsonPointerException extends RuntimeException {
        public InvalidJsonPointerException(String jsonPointerExpr) {
            super("'" + jsonPointerExpr + "' is not a valid JSON Pointer Expression");
        }
    }
}