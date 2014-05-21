package de.osthus.ambeth.util.xml;

import java.io.File;
import java.io.IOException;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class XmlValidator implements IXmlValidator
{
	@LogInstance
	private ILogger log;

	protected Validator validator;

	public XmlValidator(Validator validator)
	{
		this.validator = validator;
	}

	@Override
	public void validate(Document doc) throws SAXException, IOException
	{
		validator.validate(new DOMSource(doc));
	}

	@Override
	public void validate(File file) throws SAXException, IOException
	{
		validator.validate(new StreamSource(file));
	}
}
