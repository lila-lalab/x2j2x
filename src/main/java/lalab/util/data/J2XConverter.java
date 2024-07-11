package lalab.util.data;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * A converter class to convert JSON data to XML format.
 */
public class J2XConverter {
    J2XConverterConfig config;
    public static final String SOAPNS_1_1 = "http://schemas.xmlsoap.org/soap/envelope/";
    public static final String SOAPNS_1_2 = "http://www.w3.org/2003/05/soap-envelope";

    /**
     * Constructs a J2XConverter with the specified configuration.
     *
     * @param config the configuration for the converter
     */
    public J2XConverter(J2XConverterConfig config) {
        this.config = config;
    }

    /**
     * Converts a JSON node to an XML Document.
     *
     * @param json the JSON node to convert
     * @return the XML Document representing the JSON data
     * @throws ParserConfigurationException if a DocumentBuilder cannot be created
     */
    public Document j2x(JsonNode json) throws ParserConfigurationException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.newDocument();

        if (config.wrapSoapEnvelope) {
            Element soapEnv = getSoapEnvElm(doc);
            Element soapBody = doc.createElement("soapenv:Body");

            if (config.soapBodyAsRoot) {
                soapEnv.appendChild(mapJsonElement(doc, soapBody, json, config.rootName, config.createNamespace));
            } else {
                Element root = getNewXmlElm(doc, config.createNamespace, config.rootName);
                soapBody.appendChild(mapJsonElement(doc, root, json, null, config.createNamespace));
                soapEnv.appendChild(soapBody);
            }
            doc.appendChild(soapEnv);
        } else {
            Element root = getNewXmlElm(doc, config.createNamespace, config.rootName);
            doc.appendChild(mapJsonElement(doc, root, json, null, false));
        }
        return doc;
    }

    /**
     * Creates a new SOAP envelope element based on the SOAP version.
     *
     * @param doc the XML document
     * @return the SOAP envelope element
     */
    private Element getSoapEnvElm(Document doc) {
        if (config.soapVersion.equals("1.1")) {
            return doc.createElementNS(SOAPNS_1_1, "soapenv:Envelope");
        } else {
            return doc.createElementNS(SOAPNS_1_2, "soapenv:Envelope");
        }
    }

    /**
     * Creates a new XML element with or without namespace.
     *
     * @param doc the XML document
     * @param setNamespace whether to set the namespace
     * @param elmName the name of the element
     * @return the new XML element
     */
    private Element getNewXmlElm(Document doc, boolean setNamespace, String elmName) {
        if (setNamespace) {
            return doc.createElementNS(config.namespace, config.alias + ":" + elmName);
        } else {
            return doc.createElement(elmName);
        }
    }

    /**
     * Maps a JSON node to an XML element.
     *
     * @param doc the XML document
     * @param currentNode the current XML element
     * @param json the JSON node
     * @param fieldName the field name (if any)
     * @param setNamespace whether to set the namespace
     * @return the mapped XML element
     */
    public Element mapJsonElement(Document doc, Element currentNode, JsonNode json, String fieldName, boolean setNamespace) {
        switch (json.getNodeType()) {
            case ARRAY:
                ArrayNode arrayNode = (ArrayNode) json;
                String name = fieldName == null ? config.unnamedArrXmlNodeName : fieldName;
                if (arrayNode.size() == 0) {
                    if (!config.ignoreEmptyArray) {
                        currentNode.appendChild(getNewXmlElm(doc, setNamespace, name));
                    }
                } else {
                    for (JsonNode item : arrayNode) {
                        if (item.isArray()) {
                            mapJsonElement(doc, currentNode, item, name, false);
                        } else {
                            Element arrElm = getNewXmlElm(doc, setNamespace, name);
                            currentNode.appendChild(mapJsonElement(doc, arrElm, item, name, false));
                        }
                    }
                }
                break;
            case OBJECT:
                json.fields().forEachRemaining(f -> {
                    if (!config.ignoreXmlAttribute && f.getKey().startsWith(config.xmlAttributePrefix) && !f.getValue().isContainerNode()) {
                        String key = f.getKey().substring(config.xmlAttributePrefix.length());
                        currentNode.setAttribute(key, f.getValue().asText());
                    } else if (f.getKey().equals(config.xmlValueFieldName) && !f.getValue().isContainerNode()) {
                        currentNode.setTextContent(f.getValue().asText());
                    } else {
                        if (f.getValue().isArray()) {
                            mapJsonElement(doc, currentNode, f.getValue(), f.getKey(), false);
                        } else {
                            Element fieldElm = getNewXmlElm(doc, setNamespace, f.getKey());
                            currentNode.appendChild(mapJsonElement(doc, fieldElm, f.getValue(), f.getKey(), false));
                        }
                    }
                });
                break;
            default:
                currentNode.setTextContent(json.asText());
        }
        return currentNode;
    }
}