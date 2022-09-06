package info.metadude.android.eventfahrplan.network.serialization.exceptions

/**
 * Default constructor
 *
 * @param elementName          The name of the containing XML element
 * @param missingAttributeName The name of the XML attribute which cannot be found
 */
class MissingXmlAttributeException(

    elementName: String,
    missingAttributeName: String

) : IllegalStateException(
    """The <${elementName}> element does not contain the mandatory "$missingAttributeName" attribute."""
)
