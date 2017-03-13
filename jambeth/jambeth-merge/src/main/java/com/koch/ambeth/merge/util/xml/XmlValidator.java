package com.koch.ambeth.merge.util.xml;

import java.io.File;
import java.io.IOException;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.xml.IXmlValidator;

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
