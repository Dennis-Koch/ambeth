package de.osthus.ambeth.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.EmptyList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.io.FileUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.util.xml.IXmlConfigUtil;
import de.osthus.ambeth.util.xml.IXmlValidator;
import de.osthus.ambeth.util.xml.XmlConstants;
import de.osthus.ambeth.util.xml.XmlValidator;

public class XmlConfigUtil implements IXmlConfigUtil, IInitializingBean
{
	private static final String STRING_NULL = "null";

	@LogInstance
	private ILogger log;

	protected final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

	protected final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

	protected final HashMap<String, Reference<Class<?>>> nameToTypeMap = new HashMap<String, Reference<Class<?>>>();

	protected final java.util.concurrent.locks.Lock writeLock = new ReentrantLock();

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected IProperties properties;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		dbf.setNamespaceAware(true);
	}

	@Override
	public Document[] readXmlFiles(String xmlFileNames)
	{
		String[] fileNames = FileUtil.splitConfigFileNames(xmlFileNames);
		Document[] docs = new Document[fileNames.length];

		for (int i = fileNames.length; i-- > 0;)
		{
			String fileName = fileNames[i];
			if (fileName == null || fileName.isEmpty() || STRING_NULL.equals(fileName))
			{
				if (log.isWarnEnabled())
				{
					log.warn("Possible wrong argument to resolve xml files: '" + xmlFileNames + "' at element index " + i
							+ ". Maybe an embedded property resolved to null previously?");
				}
				continue;
			}
		}

		InputStream[] streams = FileUtil.openFileStreams(fileNames, true, log);

		docs = readXmlStreams(streams);

		return docs;
	}

	@Override
	public IXmlValidator createValidator(String... xsdFileNames)
	{
		Source[] sources = new Source[xsdFileNames.length];
		for (int i = xsdFileNames.length; i-- > 0;)
		{
			sources[i] = openConfigAsSource(xsdFileNames[i]);
		}
		Schema schema;
		try
		{
			schema = schemaFactory.newSchema(sources);
		}
		catch (SAXException e)
		{
			IThreadLocalObjectCollector oc = objectCollector.getCurrent();
			throw RuntimeExceptionUtil.mask(e, "Error while reading schema file '" + Arrays.toString(oc, xsdFileNames) + "'");
		}
		IXmlValidator validator = new XmlValidator(schema.newValidator());

		return validator;
	}

	protected Source openConfigAsSource(String name)
	{
		InputStream schemaStream = FileUtil.openFileStream(name, log);
		Source source = new StreamSource(schemaStream);
		return source;
	}

	protected Document[] readXmlStreams(InputStream[] xmlStreams)
	{
		Document[] docs = new Document[xmlStreams.length];
		for (int i = xmlStreams.length; i-- > 0;)
		{
			InputStream stream = xmlStreams[i];
			docs[i] = readXmlStream(stream);
		}
		return docs;
	}

	protected Document readXmlStream(InputStream xmlStream)
	{
		Document doc = null;
		RuntimeException throwLater = null;
		try
		{
			doc = dbf.newDocumentBuilder().parse(xmlStream);
		}
		catch (Throwable e)
		{
			throwLater = RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			try
			{
				xmlStream.close();
				if (throwLater != null)
				{
					throw throwLater;
				}
			}
			catch (IOException e)
			{
				if (throwLater == null)
				{
					throw RuntimeExceptionUtil.mask(e);
				}
				else
				{
					throw throwLater;
				}
			}
		}
		return doc;
	}

	@Override
	public String readDocumentNamespace(Document doc)
	{
		return getAttribute(doc.getDocumentElement(), "xmlns");
	}

	@Override
	public IList<Element> nodesToElements(NodeList nodeList)
	{
		IList<Element> elements = new ArrayList<Element>();

		// Order is semantically important
		for (int i = 0, size = nodeList.getLength(); i < size; i++)
		{
			Node node = nodeList.item(i);
			if (node != null && node.getNodeType() == Node.ELEMENT_NODE)
			{
				elements.add((Element) node);
			}
		}

		return elements;
	}

	@Override
	public Element getChildUnique(Element parent, String childTagName)
	{
		IList<Element> matchingChildren = nodesToElements(parent.getElementsByTagName(childTagName));
		if (matchingChildren.isEmpty())
		{
			return null;
		}
		else if (matchingChildren.size() == 1)
		{
			return matchingChildren.get(0);
		}
		else
		{
			throw new IllegalStateException("There should only be one '" + childTagName + "' child node");
		}
	}

	@Override
	public IList<Element> getElementsByName(String name, IMap<String, IList<Element>> elementMap)
	{
		IList<Element> elements = elementMap.get(name);
		if (elements != null)
		{
			return elements;
		}
		return EmptyList.<Element> getInstance();
	}

	@Override
	public IMap<String, IList<Element>> childrenToElementMap(Node parent)
	{
		return toElementMap(parent.getChildNodes());
	}

	@Override
	public IMap<String, IList<Element>> toElementMap(NodeList nodeList)
	{
		HashMap<String, IList<Element>> elementMap = new HashMap<String, IList<Element>>();

		// Order is semantically important
		for (int i = 0, size = nodeList.getLength(); i < size; i++)
		{
			Node node = nodeList.item(i);
			if (node != null && node.getNodeType() == Node.ELEMENT_NODE)
			{
				String nodeName = node.getNodeName();
				IList<Element> elements = elementMap.get(nodeName);
				if (elements == null)
				{
					elements = new ArrayList<Element>();
					elementMap.put(nodeName, elements);
				}
				elements.add((Element) node);
			}
		}

		return elementMap;
	}

	@Override
	public String getAttribute(Element element, String attrName)
	{
		return getRequiredAttribute(element, attrName, false, false);
	}

	@Override
	public String getRequiredAttribute(Element element, String attrName)
	{
		return getRequiredAttribute(element, attrName, true, false);
	}

	@Override
	public String getRequiredAttribute(Element element, String attrName, boolean firstToUpper)
	{
		return getRequiredAttribute(element, attrName, true, firstToUpper);
	}

	protected String getRequiredAttribute(Element element, String attrName, boolean required, boolean firstToUpper)
	{
		String value = element.getAttribute(attrName);
		if (required && value.isEmpty())
		{
			throw new IllegalArgumentException("Attribute '" + attrName + "' has to be set on tag '" + element.getNodeName() + "'");
		}
		value = properties.resolvePropertyParts(value);
		if (firstToUpper)
		{
			value = StringConversionHelper.upperCaseFirst(objectCollector, value);
		}
		return value;
	}

	@Override
	public String getChildElementAttribute(Element parent, String childName, String attrName, String error)
	{
		String value = null;
		List<Element> tags = nodesToElements(parent.getElementsByTagName(childName));
		if (tags.size() == 1)
		{
			value = getAttribute(tags.get(0), attrName);
			value = value.trim();
		}
		else if (error != null)
		{
			throw new IllegalArgumentException(error);
		}

		return value;
	}

	@Override
	public boolean attributeIsTrue(Element element, String attrName)
	{
		String attrValue = getAttribute(element, attrName);
		return attrValue.equals(XmlConstants.TRUE);
	}

	@Override
	public Class<?> getTypeForName(String name)
	{
		return getTypeForName(name, false);
	}

	@Override
	public Class<?> getTypeForName(String name, boolean tryOnly)
	{
		writeLock.lock();
		try
		{
			Reference<Class<?>> typeR = nameToTypeMap.get(name);
			Class<?> type = null;
			if (typeR != null)
			{
				type = typeR.get();
			}
			if (type != null)
			{
				return type;
			}
			if (typeR == null && nameToTypeMap.containsKey(name))
			{
				// tried to load once so we eagerly give up
				if (tryOnly)
				{
					return null;
				}
				throw RuntimeExceptionUtil.mask(new ClassNotFoundException(name));
			}
			Class<?> entityType;
			try
			{
				entityType = Thread.currentThread().getContextClassLoader().loadClass(name);
			}
			catch (ClassNotFoundException e)
			{
				if (log.isErrorEnabled())
				{
					log.error("Configured class '" + name + "' was not found");
				}
				nameToTypeMap.put(name, null);
				if (tryOnly)
				{
					return null;
				}
				throw RuntimeExceptionUtil.mask(e);
			}
			nameToTypeMap.put(name, new WeakReference<Class<?>>(entityType));
			return entityType;
		}
		finally
		{
			writeLock.unlock();
		}
	}
}
