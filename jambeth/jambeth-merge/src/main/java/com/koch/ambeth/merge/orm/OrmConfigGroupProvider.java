package com.koch.ambeth.merge.orm;

/*-
 * #%L
 * jambeth-merge
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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.w3c.dom.Document;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.service.cache.ClearAllCachesEvent;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.LinkedHashSet;
import com.koch.ambeth.util.xml.IXmlConfigUtil;

public class OrmConfigGroupProvider implements IOrmConfigGroupProvider {
	public static final String handleClearAllCachesEvent = "handleClearAllCachesEvent";

	@Autowired
	protected IOrmEntityTypeProvider defaultOrmEntityTypeProvider;

	@Autowired
	protected IOrmXmlReaderRegistry ormXmlReaderRegistry;

	@Autowired
	protected IXmlConfigUtil xmlConfigUtil;

	protected final HashMap<String, Reference<IOrmConfigGroup>> xmlFileNamesConfigGroupMap =
			new HashMap<>(0.5f);

	protected final Lock writeLock = new ReentrantLock();

	public void handleClearAllCachesEvent(ClearAllCachesEvent evnt) {
		writeLock.lock();
		try {
			xmlFileNamesConfigGroupMap.clear();
		}
		finally {
			writeLock.unlock();
		}
	}

	@Override
	public IOrmConfigGroup getOrmConfigGroup(String xmlFileNames) {
		Reference<IOrmConfigGroup> ormConfigGroupR;
		writeLock.lock();
		try {
			ormConfigGroupR = xmlFileNamesConfigGroupMap.get(xmlFileNames);
		}
		finally {
			writeLock.unlock();
		}
		IOrmConfigGroup ormConfigGroup = null;
		if (ormConfigGroupR != null) {
			ormConfigGroup = ormConfigGroupR.get();
		}
		if (ormConfigGroup != null) {
			return ormConfigGroup;
		}
		Document[] docs = xmlConfigUtil.readXmlFiles(xmlFileNames);
		IOrmConfigGroup newOrmConfigGroup = getOrmConfigGroup(docs, defaultOrmEntityTypeProvider);
		writeLock.lock();
		try {
			ormConfigGroupR = xmlFileNamesConfigGroupMap.get(xmlFileNames);
			if (ormConfigGroupR != null) {
				ormConfigGroup = ormConfigGroupR.get();
			}
			if (ormConfigGroup == null) {
				xmlFileNamesConfigGroupMap.put(xmlFileNames,
						new WeakReference<>(newOrmConfigGroup));
				ormConfigGroup = newOrmConfigGroup;
			}
			return ormConfigGroup;
		}
		finally {
			writeLock.unlock();
		}
	}

	@Override
	public IOrmConfigGroup getOrmConfigGroup(Document[] docs,
			IOrmEntityTypeProvider ormEntityTypeProvider) {
		LinkedHashSet<EntityConfig> localEntities = new LinkedHashSet<>();
		LinkedHashSet<EntityConfig> externalEntities = new LinkedHashSet<>();

		for (Document doc : docs) {
			doc.normalizeDocument();
			String documentNamespace = xmlConfigUtil.readDocumentNamespace(doc);
			IOrmXmlReader ormXmlReader = ormXmlReaderRegistry.getOrmXmlReader(documentNamespace);
			ormXmlReader.loadFromDocument(doc, localEntities, externalEntities, ormEntityTypeProvider);
		}
		return new OrmConfigGroup(new LinkedHashSet<IEntityConfig>(localEntities),
				new LinkedHashSet<IEntityConfig>(externalEntities));
	}
}
