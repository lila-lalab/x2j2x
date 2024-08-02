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

import java.util.Collections;
import java.util.List;

/**
 * Configuration class for the X2JConverter.
 */
public class X2JConverterConfig {

    /**
     * If true, XML attributes are ignored during conversion.
     */
    public boolean ignoreXmlAttribute = true;

    /**
     * Prefix for XML attributes when converting to JSON.
     */
    public String xmlAttributePrefix = "@";

    /**
     * Field name for XML values in JSON representation.
     */
    public String xmlValueFieldName = "_value";

    /**
     * If true, includes the root element in the JSON output.
     */
    public boolean includeRoot = false;

    /**
     * If true, ignores attributes with the "xsd:" prefix.
     */
    public boolean ignoreXsdTypeAttr = true;

    /**
     * List of XML fields that should be treated as arrays in the JSON output.
     */
    public List<String> xmlArrayFields = Collections.emptyList();

    /**
     * List of XML fields that should be treated as numbers in the JSON output.
     */
    public List<String> xmlNumberFields = Collections.emptyList();

    /**
     * List of XML fields that should be treated as booleans in the JSON output.
     */
    public List<String> xmlBooleanFields = Collections.emptyList();

    /**
     * If true, trims whitespace from XML values during conversion.
     */
    public boolean trimWhitespace = true;

    /**
     * If true, trims newline characters from XML values during conversion.
     */
    public boolean trimNewLine = true;

    /**
     * If true, treats null values as empty strings in the JSON output.
     */
    public boolean nullAsEmptyString = false;

    /**
     * If true, removes the SOAP envelope from the XML document during conversion.
     */
    public boolean tearSOAPEnvelope = false;

    /**
     * If true, removes namespace aliases from XML element names during conversion.
     */
    public boolean removeNamespaceAlias = true;
}