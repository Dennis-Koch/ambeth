package de.osthus.ambeth.xml;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.Tuple2KeyHashMap;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.ParamChecker;

public class XmlTypeRegistry implements IXmlTypeExtendable, IInitializingBean, IXmlTypeRegistry
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final Tuple2KeyHashMap<String, String, Class<?>> xmlTypeToClassMap = new Tuple2KeyHashMap<String, String, Class<?>>(0.5f);

	protected final HashMap<Class<?>, List<XmlTypeKey>> classToXmlTypeMap = new HashMap<Class<?>, List<XmlTypeKey>>(0.5f);

	protected final Lock readLock, writeLock;

	public XmlTypeRegistry()
	{
		ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
		readLock = rwLock.readLock();
		writeLock = rwLock.writeLock();
	}

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		registerXmlType(Boolean.class, "BoolN", null);
		registerXmlType(Boolean.TYPE, "Bool", null);
		registerXmlType(Character.class, "CharN", null);
		registerXmlType(Character.TYPE, "Char", null);
		registerXmlType(Byte.class, "ByteN", null);
		registerXmlType(Byte.TYPE, "Byte", null);
		registerXmlType(Long.class, "Int64N", null);
		registerXmlType(Long.TYPE, "Int64", null);
		registerXmlType(Integer.class, "Int32N", null);
		registerXmlType(Integer.TYPE, "Int32", null);
		registerXmlType(Short.class, "Int16N", null);
		registerXmlType(Short.TYPE, "Int16", null);
		// First register of BigInteger defines the default ClassToXml-mapping
		registerXmlType(BigInteger.class, "UInt64N", null);
		registerXmlType(BigInteger.class, "UInt64", null);
		registerXmlType(BigInteger.class, "UInt32N", null);
		registerXmlType(BigInteger.class, "UInt32", null);
		registerXmlType(BigInteger.class, "UInt16N", null);
		registerXmlType(BigInteger.class, "UInt16", null);
		registerXmlType(Float.class, "Float32N", null);
		registerXmlType(Float.TYPE, "Float32", null);
		registerXmlType(Double.class, "Float64N", null);
		registerXmlType(Double.TYPE, "Float64", null);
		registerXmlType(String.class, "String", null);
		registerXmlType(Object.class, "Object", null);
		registerXmlType(Class.class, "Class", null);
		registerXmlType(List.class, "List", null);
		registerXmlType(List.class, "ListG", null);
		registerXmlType(Set.class, "SetG", null);
		registerXmlType(Date.class, "Date", null);
	}

	@Override
	public Class<?> getType(String name, String namespace)
	{
		ParamChecker.assertParamNotNull(name, "name");
		if (namespace == null)
		{
			namespace = "";
		}
		Lock readLock = this.readLock;
		readLock.lock();
		try
		{
			Class<?> type = xmlTypeToClassMap.get(name, namespace);
			if (type == null)
			{
				if (log.isDebugEnabled())
				{
					log.debug("XmlTypeNotFound: name=" + name + ", namespace=" + namespace);
				}
				return null;
			}
			return type;
		}
		finally
		{
			readLock.unlock();
		}
	}

	@Override
	public IXmlTypeKey getXmlType(Class<?> type)
	{
		return getXmlType(type, true);
	}

	@Override
	public IXmlTypeKey getXmlType(Class<?> type, boolean expectExisting)
	{
		ParamChecker.assertParamNotNull(type, "type");

		readLock.lock();
		try
		{
			List<XmlTypeKey> xmlTypeKeys = classToXmlTypeMap.get(type);
			if (expectExisting && xmlTypeKeys == null)
			{
				throw new IllegalArgumentException("No xml type found: Type=" + type);
			}
			return xmlTypeKeys != null ? xmlTypeKeys.get(0) : null;
		}
		finally
		{
			readLock.unlock();
		}
	}

	@Override
	public void registerXmlType(Class<?> type, String name, String namespace)
	{
		ParamChecker.assertParamNotNull(type, "type");
		ParamChecker.assertParamNotNull(name, "name");
		if (namespace == null)
		{
			namespace = "";
		}
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			Class<?> existingType = xmlTypeToClassMap.put(name, namespace, type);
			if (existingType != null)
			{
				if (type.isAssignableFrom(existingType))
				{
					// Nothing else to to
				}
				else if (existingType.isAssignableFrom(type))
				{
					xmlTypeToClassMap.put(name, namespace, existingType);
				}
				else
				{
					throw new IllegalStateException("Error while registering '" + type.getName() + "': Unassignable type '" + existingType.getName()
							+ "' already registered for: Name='" + name + "' Namespace='" + namespace + "'");
				}
			}

			List<XmlTypeKey> xmlTypeKeys = classToXmlTypeMap.get(type);
			if (xmlTypeKeys == null)
			{
				xmlTypeKeys = new ArrayList<XmlTypeKey>();
				classToXmlTypeMap.put(type, xmlTypeKeys);
			}
			xmlTypeKeys.add(new XmlTypeKey(name, namespace));
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public void unregisterXmlType(Class<?> type, String name, String namespace)
	{
		ParamChecker.assertParamNotNull(type, "type");
		ParamChecker.assertParamNotNull(name, "name");
		if (namespace == null)
		{
			namespace = "";
		}

		XmlTypeKey xmlTypeKey = new XmlTypeKey(name, namespace);

		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			xmlTypeToClassMap.remove(xmlTypeKey.getName(), xmlTypeKey.getNamespace());

			List<XmlTypeKey> xmlTypeKeys = classToXmlTypeMap.get(type);
			xmlTypeKeys.remove(xmlTypeKey);
			if (xmlTypeKeys.size() == 0)
			{
				classToXmlTypeMap.remove(type);
			}
		}
		finally
		{
			writeLock.unlock();
		}
	}
}