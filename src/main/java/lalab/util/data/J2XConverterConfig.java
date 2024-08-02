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

/**
 * Configuration class for J2XConverter.
 */
public class J2XConverterConfig {
    // Element conversion options

    /**
     * The root element name.
     */
    public String rootName = "root";

    /**
     * The XML element name for unnamed JSON arrays.
     */
    public String unnamedArrXmlNodeName = "item";

    /**
     * Whether to ignore XML attributes.
     */
    public boolean ignoreXmlAttribute = false;

    /**
     * The prefix for XML attributes.
     */
    public String xmlAttributePrefix = "@";

    /**
     * The field name for XML values.
     */
    public String xmlValueFieldName = "_value";

    /**
     * Whether to trim whitespace.
     */
    public boolean trimWhitespace = true;

    /**
     * Whether to ignore empty arrays.
     */
    public boolean ignoreEmptyArray = false;

    // Root namespace options

    /**
     * Whether to create a namespace.
     */
    public boolean createNamespace = false;

    /**
     * The alias for the namespace.
     */
    public String alias = "n0";

    /**
     * The namespace URI.
     */
    public String namespace = null;

    // SOAP specific options

    /**
     * Whether to wrap the output in a SOAP envelope.
     */
    public boolean wrapSoapEnvelope = false;

    /**
     * The SOAP version ("1.1" or "1.2").
     */
    public String soapVersion = "1.2";

    /**
     * Whether to use the SOAP body as the root element.
     */
    public boolean soapBodyAsRoot = false;
}