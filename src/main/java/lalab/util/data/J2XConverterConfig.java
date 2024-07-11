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