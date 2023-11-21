package com.koch.ambeth.xml;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.util.IImmutableTypeSet;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.util.IClasspathScanner;
import com.koch.ambeth.util.typeinfo.ITypeInfoProvider;

public class XmlTransferScanner implements IInitializingBean, IStartingBean, IDisposableBean {
	@LogInstance
	private ILogger log;

	@Autowired(optional = true)
	protected IClasspathScanner classpathScanner;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IImmutableTypeSet immutableTypeSet;

	@Autowired
	protected ITypeInfoProvider typeInfoProvider;

	@Autowired
	protected IXmlTypeExtendable xmlTypeExtendable;

	@Autowired
	protected IXmlTypeRegistry xmlTypeRegistry;

	protected List<Class<?>> rootElementClasses;

	protected List<Runnable> unregisterRunnables = new ArrayList<>();

	@Override
	public void afterPropertiesSet() throws Throwable {
		if (classpathScanner == null) {
			if (log.isInfoEnabled()) {
				log.info("Skipped scanning for XML transfer types. Reason: No instance of "
						+ IClasspathScanner.class.getName() + " resolved");
			}
			return;
		}
		List<Class<?>> rootElementClasses = classpathScanner.scanClassesAnnotatedWith(
				XmlRootElement.class, XmlType.class, com.koch.ambeth.util.annotation.XmlType.class);
		if (log.isInfoEnabled()) {
			log.info("Found " + rootElementClasses.size() + " classes annotated as XML transfer types");
		}
		if (log.isDebugEnabled()) {
			Collections.sort(rootElementClasses, new Comparator<Class<?>>() {
				@Override
				public int compare(Class<?> o1, Class<?> o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
			StringBuilder sb = new StringBuilder();
			sb.append("Xml entities found: ");
			for (int a = 0, size = rootElementClasses.size(); a < size; a++) {
				sb.append("\n\t").append(rootElementClasses.get(a).getName());
			}
			log.debug(sb.toString());
		}
		for (int a = rootElementClasses.size(); a-- > 0;) {
			final Class<?> rootElementClass = rootElementClasses.get(a);
			String name, namespace;

			com.koch.ambeth.util.annotation.XmlType genericXmlType =
					rootElementClass.getAnnotation(com.koch.ambeth.util.annotation.XmlType.class);
			if (genericXmlType != null) {
				name = genericXmlType.name();
				namespace = genericXmlType.namespace();
			}
			else {
				XmlRootElement xmlRootElement = rootElementClass.getAnnotation(XmlRootElement.class);
				if (xmlRootElement != null) {
					name = xmlRootElement.name();
					namespace = xmlRootElement.namespace();
				}
				else {
					XmlType xmlType = rootElementClass.getAnnotation(XmlType.class);
					name = xmlType.name();
					namespace = xmlType.namespace();
				}
			}
			namespace = xmlTypeRegistry.getXmlTypeNamespace(rootElementClass, namespace);
			name = xmlTypeRegistry.getXmlTypeName(rootElementClass, name);

			xmlTypeExtendable.registerXmlType(rootElementClass, name, namespace);
			final String fName = name;
			final String fNamespace = namespace;
			unregisterRunnables.add(new Runnable() {
				@Override
				public void run() {
					xmlTypeExtendable.unregisterXmlType(rootElementClass, fName, fNamespace);
				}
			});
		}
		this.rootElementClasses = rootElementClasses;
	}

	@Override
	public void afterStarted() throws Throwable {
		if (rootElementClasses == null) {
			return;
		}
		// Eager fetch all meta data. Even if some of the classes are NOT an entity this is not a
		// problem
		List<Class<?>> types = new ArrayList<>();
		for (Class<?> type : rootElementClasses) {
			if (type.isInterface() || immutableTypeSet.isImmutableType(type)) {
				continue;
			}
			types.add(type);
		}
		entityMetaDataProvider.getMetaData(types);
	}

	@Override
	public void destroy() throws Throwable {
		for (int a = unregisterRunnables.size(); a-- > 0;) {
			unregisterRunnables.get(a).run();
		}
	}
}
