package nerd.tuxmobil.fahrplan.congress;

public class MissingXmlAttributeException extends IllegalStateException {

    /**
     * Default constructor
     *
     * @param elementName          The name of the containing XML element
     * @param missingAttributeName The name of the XML attribute which cannot be found
     */
    public MissingXmlAttributeException(final String elementName,
            final String missingAttributeName) {
        super("The <" + elementName + "> element does not contain " +
                "the mandatory '" + missingAttributeName + "' attribute.");
    }

}
