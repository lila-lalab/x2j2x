# X2J2X
Direct XML to JSON / JSON to XML Converter.

## XML to JSON Conversion

```java
    void xml2json() throws Exception{
        String docString ="""
        <root>
            <person id=\"123\">
                <name>John</name>person
            </person>
        </root>
        """;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(docString)));
        
        X2JConverter converter = new X2JConverter(new X2JConverterConfig());
        System.out.println(converter.x2J(doc).toString());
        //{"person":{"name":"John","_value":"person"}}

        X2JConverterConfig config = new X2JConverterConfig();
        config.ignoreXmlAttribute = false;
        converter = new X2JConverter(config);
        System.out.println(converter.x2J(doc).toString());
        //{"person":{"@id":"123","name":"John","_value":"person"}}
    }
```

## JSON to XML Conversion
```java
    void json2xml() throws Exception{
        String jsonString = "{\"person\":{\"@id\":\"123\",\"name\":\"John\"}}";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(jsonString);

        J2XConverter converter = new J2XConverter(new J2XConverterConfig());
        printXml(converter.j2x(jsonNode));
        /**\
         * <?xml version="1.0" encoding="UTF-8" standalone="no"?>
         * <root>
         *    <person id="123">
         *        <name>John</name>
         *    </person>
         * </root>
         */

        J2XConverterConfig config = new J2XConverterConfig();
        config.createNamespace=true;
        config.namespace="http://test.x2j2x";
        config.alias="body";
        config.ignoreXmlAttribute=false;
        converter = new J2XConverter(config);
        printXml(converter.j2x(jsonNode));
        /**
         * <?xml version="1.0" encoding="UTF-8" standalone="no"?>
         * <body:root xmlns:body="http://test.x2j2x">
         *     <person id="123">
         *         <name>John</name>
         *     </person>
         * </body:root>
         */
    }

    void printXml(Document doc) throws Exception{
        DOMSource domSource = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.transform(domSource, result);
        System.out.println(writer.toString());
    }
```