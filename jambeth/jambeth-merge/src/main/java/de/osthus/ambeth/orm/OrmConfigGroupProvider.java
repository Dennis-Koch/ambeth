package de.osthus.ambeth.orm;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.w3c.dom.Document;

import de.osthus.ambeth.cache.ClearAllCachesEvent;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.LinkedHashSet;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.util.xml.IXmlConfigUtil;

public class OrmConfigGroupProvider implements IOrmConfigGroupProvider
{
	public static final String handleClearAllCachesEvent = "handleClearAllCachesEvent";

	@Autowired
	protected IOrmEntityTypeProvider defaultOrmEntityTypeProvider;

	@Autowired
	protected IOrmXmlReaderRegistry ormXmlReaderRegistry;

	@Autowired
	protected IXmlConfigUtil xmlConfigUtil;

	protected final HashMap<String, Reference<IOrmConfigGroup>> xmlFileNamesConfigGroupMap = new HashMap<String, Reference<IOrmConfigGroup>>(0.5f);

	protected final Lock writeLock = new ReentrantLock();

	public void handleClearAllCachesEvent(ClearAllCachesEvent evnt)
	{
		writeLock.lock();
		try
		{
			xmlFileNamesConfigGroupMap.clear();
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public IOrmConfigGroup getOrmConfigGroup(String xmlFileNames)
	{
		Reference<IOrmConfigGroup> ormConfigGroupR;
		writeLock.lock();
		try
		{
			ormConfigGroupR = xmlFileNamesConfigGroupMap.get(xmlFileNames);
		}
		finally
		{
			writeLock.unlock();
		}
		IOrmConfigGroup ormConfigGroup = null;
		if (ormConfigGroupR != null)
		{
			ormConfigGroup = ormConfigGroupR.get();
		}
		if (ormConfigGroup != null)
		{
			return ormConfigGroup;
		}
		Document[] docs = xmlConfigUtil.readXmlFiles(xmlFileNames);
		IOrmConfigGroup newOrmConfigGroup = getOrmConfigGroup(docs, defaultOrmEntityTypeProvider);
		writeLock.lock();
		try
		{
			ormConfigGroupR = xmlFileNamesConfigGroupMap.get(xmlFileNames);
			if (ormConfigGroupR != null)
			{
				ormConfigGroup = ormConfigGroupR.get();
			}
			if (ormConfigGroup == null)
			{
				xmlFileNamesConfigGroupMap.put(xmlFileNames, new WeakReference<IOrmConfigGroup>(newOrmConfigGroup));
				ormConfigGroup = newOrmConfigGroup;
			}
			return ormConfigGroup;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public IOrmConfigGroup getOrmConfigGroup(Document[] docs, IOrmEntityTypeProvider ormEntityTypeProvider)
	{
		LinkedHashSet<EntityConfig> localEntities = new LinkedHashSet<EntityConfig>();
		LinkedHashSet<EntityConfig> externalEntities = new LinkedHashSet<EntityConfig>();

		for (Document doc : docs)
		{
			doc.normalizeDocument();
			String documentNamespace = xmlConfigUtil.readDocumentNamespace(doc);
			IOrmXmlReader ormXmlReader = ormXmlReaderRegistry.getOrmXmlReader(documentNamespace);
			ormXmlReader.loadFromDocument(doc, localEntities, externalEntities, ormEntityTypeProvider);
		}
		return new OrmConfigGroup(new LinkedHashSet<IEntityConfig>(localEntities), new LinkedHashSet<IEntityConfig>(externalEntities));
	}
}
