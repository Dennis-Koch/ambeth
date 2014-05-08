package de.osthus.ambeth.xml.namehandler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.xml.INameBasedHandler;
import de.osthus.ambeth.xml.IReader;
import de.osthus.ambeth.xml.IWriter;
import de.osthus.ambeth.xml.typehandler.AbstractHandler;

public class StringNameHandler extends AbstractHandler implements INameBasedHandler
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	private static final String emptyString = "";

	private static final String cdataStartSeq = "<![CDATA[";

	private static final String cdataEndSeq = "]]>";

	protected static final Pattern cdataPattern = Pattern.compile("([\\s\\S]*?\\])(\\][\\s\\S]*)");

	@Override
	public boolean writesCustom(Object obj, Class<?> type, IWriter writer)
	{
		if (!String.class.equals(type))
		{
			return false;
		}
		String value = (String) obj;

		String stringElement = xmlDictionary.getStringElement();
		writer.writeStartElement(stringElement);
		int id = writer.acquireIdForObject(obj);
		writer.writeAttribute(xmlDictionary.getIdAttribute(), Integer.toString(id));

		if (value.isEmpty())
		{
			writer.writeEndElement();
			return true;
		}
		writer.writeStartElementEnd();

		boolean firstCdataElement = true;
		while (true)
		{
			Matcher matcher = cdataPattern.matcher(value);
			if (!matcher.matches())
			{
				if (!firstCdataElement)
				{
					writer.writeStartElement("s");
					writer.writeStartElementEnd();
				}
				writer.write(cdataStartSeq);
				writer.write(value);
				writer.write(cdataEndSeq);
				if (!firstCdataElement)
				{
					writer.writeCloseElement("s");
				}
				break;
			}
			firstCdataElement = false;

			String leftSeq = matcher.group(1);
			String rightSeq = matcher.group(2);
			writer.writeStartElement("s");
			writer.writeStartElementEnd();
			writer.write(cdataStartSeq);
			writer.write(leftSeq);
			writer.write(cdataEndSeq);
			writer.writeCloseElement("s");
			value = rightSeq;
		}
		writer.writeCloseElement(stringElement);
		return true;
	}

	@Override
	public Object readObject(Class<?> returnType, String elementName, int id, IReader reader)
	{
		if (!xmlDictionary.getStringElement().equals(elementName))
		{
			throw new IllegalStateException("Element '" + elementName + "' not supported");
		}
		if (reader.isEmptyElement())
		{
			return emptyString;
		}
		reader.nextToken();
		StringBuilder sb = null;
		try
		{
			if (!reader.isStartTag())
			{
				String value = reader.getElementValue();
				reader.nextTag();
				if (value == null)
				{
					value = emptyString;
				}
				return value;
			}
			while (reader.isStartTag())
			{
				if (!"s".equals(reader.getElementName()))
				{
					throw new IllegalStateException("Element '" + elementName + "' not supported");
				}
				if (sb == null)
				{
					sb = objectCollector.create(StringBuilder.class);
				}
				reader.nextToken();
				String textPart = reader.getElementValue();
				sb.append(textPart);
				reader.nextToken();
				reader.moveOverElementEnd();
			}
			return sb.toString();
		}
		finally
		{
			if (sb != null)
			{
				objectCollector.dispose(sb);
			}
		}
	}
}
