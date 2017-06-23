package com.koch.ambeth.xml;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

/*-
 * #%L
 * jambeth-xml
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.koch.ambeth.event.IEventDispatcher;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.ILoggerHistory;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.IProxyHelper;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.IClassCache;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.AbstractTuple2KeyHashMap;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.Tuple2KeyHashMap;
import com.koch.ambeth.util.collections.WeakHashMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.xml.XmlTypeRegistryUpdatedEvent.EventType;

public class XmlTypeRegistry implements IXmlTypeExtendable, IInitializingBean, IXmlTypeRegistry {
	public static final String DefaultNamespace = "http://schema.kochdev.com/Ambeth";

	@LogInstance
	private ILogger log;

	@Autowired
	protected IClassCache classCache;

	@Autowired
	protected IEventDispatcher eventDispatcher;

	@Autowired
	protected ILoggerHistory loggerHistory;

	@Autowired
	protected IProxyHelper proxyHelper;

	protected final WeakHashMap<Class<?>, List<XmlTypeKey>> weakClassToXmlTypeMap =
			new WeakHashMap<>(0.5f);

	protected final Tuple2KeyHashMap<String, String, Reference<Class<?>>> xmlTypeToClassMap =
			new Tuple2KeyHashMap<>(0.5f);

	protected final WeakHashMap<Class<?>, List<XmlTypeKey>> classToXmlTypeMap =
			new WeakHashMap<>(0.5f);

	protected final Lock writeLock = new ReentrantLock();

	@Override
	public void afterPropertiesSet() throws Throwable {
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
	public AbstractTuple2KeyHashMap<String, String, Reference<Class<?>>> createSnapshot() {
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try {
			return new Tuple2KeyHashMap<>(xmlTypeToClassMap);
		}
		finally {
			writeLock.unlock();
		}
	}

	@Override
	public Class<?> getType(String name, String namespace) {
		ParamChecker.assertParamNotNull(name, "name");
		if (namespace == null) {
			namespace = "";
		}
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try {
			Reference<Class<?>> typeR = xmlTypeToClassMap.get(name, namespace);
			Class<?> type = typeR != null ? typeR.get() : null;
			if (type != null) {
				return type;
			}
		}
		finally {
			writeLock.unlock();
		}
		if (namespace.isEmpty()) {
			Class<?> type;
			try {
				type = classCache.loadClass(name);
			}
			catch (ClassNotFoundException e) {
				throw RuntimeExceptionUtil.mask(e);
			}
			if (type != null) {
				writeLock.lock();
				try {
					Reference<Class<?>> typeR = xmlTypeToClassMap.get(name, namespace);
					Class<?> existingType = typeR != null ? typeR.get() : null;
					if (existingType != null) {
						return existingType;
					}
					xmlTypeToClassMap.put(name, namespace, new WeakReference<Class<?>>(type));
					return type;
				}
				finally {
					writeLock.unlock();
				}
			}
		}
		if (log.isDebugEnabled()) {
			loggerHistory.debugOnce(log, this,
					"XmlTypeNotFound: name=" + name + ", namespace=" + namespace);
		}
		return null;
	}

	@Override
	public String getXmlTypeName(Class<?> type, String name) {
		if (name == null || name.length() == 0 || "##default".equals(name)) {
			name = type.getName();
		}
		return name;
	}

	@Override
	public String getXmlTypeNamespace(Class<?> type, String namespace) {
		if (DefaultNamespace.equals(namespace) || "##default".equals(namespace)
				|| namespace != null && namespace.length() == 0) {
			namespace = null;
		}
		return namespace;
	}

	@Override
	public IXmlTypeKey getXmlType(Class<?> type) {
		return getXmlType(type, true);
	}

	@Override
	public IXmlTypeKey getXmlType(Class<?> type, boolean expectExisting) {
		ParamChecker.assertParamNotNull(type, "type");

		writeLock.lock();
		try {
			List<XmlTypeKey> xmlTypeKeys = weakClassToXmlTypeMap.get(type);
			if (xmlTypeKeys != null) {
				return xmlTypeKeys.get(0);
			}
			xmlTypeKeys = classToXmlTypeMap.get(type);
			if (xmlTypeKeys == null) {
				Class<?> realType = type;
				if (IObjRef.class.isAssignableFrom(type)) {
					realType = IObjRef.class;
				}
				xmlTypeKeys = classToXmlTypeMap.get(realType);
			}
			if (xmlTypeKeys == null) {
				xmlTypeKeys = new ArrayList<>(1);
				xmlTypeKeys.add(new XmlTypeKey(type.getName(), null));
			}
			if (xmlTypeKeys != null) {
				weakClassToXmlTypeMap.put(type, xmlTypeKeys);
			}
			return xmlTypeKeys != null ? xmlTypeKeys.get(0) : null;
		}
		finally {
			writeLock.unlock();
		}
	}

	@Override
	public void registerXmlType(Class<?> type, String name, String namespace) {
		ParamChecker.assertParamNotNull(type, "type");
		ParamChecker.assertParamNotNull(name, "name");
		if (namespace == null) {
			namespace = "";
		}
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try {
			Reference<Class<?>> existingTypeR =
					xmlTypeToClassMap.put(name, namespace, new WeakReference<Class<?>>(type));
			Class<?> existingType = existingTypeR != null ? existingTypeR.get() : null;
			if (existingType != null) {
				if (type.isAssignableFrom(existingType)) {
					// Nothing else to to
				}
				else if (existingType.isAssignableFrom(type)) {
					xmlTypeToClassMap.put(name, namespace, new WeakReference<Class<?>>(existingType));
				}
				else {
					throw new IllegalStateException("Error while registering '" + type.getName()
							+ "': Unassignable type '" + existingType.getName()
							+ "' already registered for: Name='" + name + "' Namespace='" + namespace + "'");
				}
			}

			List<XmlTypeKey> xmlTypeKeys = classToXmlTypeMap.get(type);
			if (xmlTypeKeys == null) {
				xmlTypeKeys = new ArrayList<>();
				classToXmlTypeMap.put(type, xmlTypeKeys);
			}
			xmlTypeKeys.add(new XmlTypeKey(name, namespace));
		}
		finally {
			writeLock.unlock();
		}
		eventDispatcher
				.dispatchEvent(new XmlTypeRegistryUpdatedEvent(EventType.ADDED, type, name, namespace));
	}

	@Override
	public void unregisterXmlType(Class<?> type, String name, String namespace) {
		ParamChecker.assertParamNotNull(type, "type");
		ParamChecker.assertParamNotNull(name, "name");
		if (namespace == null) {
			namespace = "";
		}

		XmlTypeKey xmlTypeKey = new XmlTypeKey(name, namespace);

		Lock writeLock = this.writeLock;
		writeLock.lock();
		try {
			xmlTypeToClassMap.remove(xmlTypeKey.getName(), xmlTypeKey.getNamespace());

			List<XmlTypeKey> xmlTypeKeys = classToXmlTypeMap.get(type);
			xmlTypeKeys.remove(xmlTypeKey);
			if (xmlTypeKeys.size() == 0) {
				classToXmlTypeMap.remove(type);
			}
		}
		finally {
			writeLock.unlock();
		}
		eventDispatcher
				.dispatchEvent(new XmlTypeRegistryUpdatedEvent(EventType.REMOVED, type, name, namespace));
	}
}
