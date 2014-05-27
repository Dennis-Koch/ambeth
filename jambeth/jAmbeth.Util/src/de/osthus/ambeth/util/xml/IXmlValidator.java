package de.osthus.ambeth.util.xml;

import java.io.File;
import java.io.IOException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Wrapper for xml validation. This is used to have the same API as in C# Ambeth.
 */
public interface IXmlValidator
{
	void validate(Document doc) throws SAXException, IOException;

	void validate(File file) throws SAXException, IOException;
}
