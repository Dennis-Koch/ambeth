package de.osthus.ambeth.merge.config;

import org.w3c.dom.Document;

import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.ISet;
import de.osthus.ambeth.collections.LinkedHashSet;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.event.EntityMetaDataAddedEvent;
import de.osthus.ambeth.event.IEventDispatcher;
import de.osthus.ambeth.ioc.IDisposableBean;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataExtendable;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.EntityMetaData;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.orm.EntityConfig;
import de.osthus.ambeth.orm.IOrmXmlReader;
import de.osthus.ambeth.orm.IOrmXmlReaderRegistry;
import de.osthus.ambeth.service.config.ConfigurationConstants;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.util.xml.IXmlConfigUtil;

public class IndependentEntityMetaDataReader implements IStartingBean, IDisposableBean
{
	@LogInstance
	private ILogger log;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IEntityMetaDataExtendable entityMetaDataExtendable;

	@Autowired
	protected IEventDispatcher eventDispatcher;

	@Autowired
	protected IEntityMetaDataReader entityMetaDataReader;

	@Autowired
	protected IOrmXmlReaderRegistry ormXmlReaderRegistry;

	@Autowired
	protected IXmlConfigUtil xmlConfigUtil;

	protected final LinkedHashSet<IEntityMetaData> managedEntityMetaData = new LinkedHashSet<IEntityMetaData>();

	protected String xmlFileName = null;

	@Override
	public void afterStarted() throws Throwable
	{
		Class<?>[] newEntityTypes = null;
		if (xmlFileName != null)
		{
			Document[] docs = xmlConfigUtil.readXmlFiles(xmlFileName);
			ParamChecker.assertNotNull(docs, "docs");
			readConfig(docs);

			newEntityTypes = new Class<?>[managedEntityMetaData.size()];
			int index = 0;
			for (IEntityMetaData metaData : managedEntityMetaData)
			{
				newEntityTypes[index++] = metaData.getEntityType();
			}
		}
		if (newEntityTypes != null && newEntityTypes.length > 0)
		{
			eventDispatcher.dispatchEvent(new EntityMetaDataAddedEvent(newEntityTypes));
		}
	}

	@Override
	public void destroy()
	{
		for (IEntityMetaData entityMetaData : managedEntityMetaData)
		{
			entityMetaDataExtendable.unregisterEntityMetaData(entityMetaData);
		}
	}

	@Property(name = ConfigurationConstants.mappingFile, mandatory = false)
	public void setFileName(String fileName)
	{
		if (xmlFileName != null)
		{
			throw new IllegalArgumentException("XmlDatabaseMapper already configured! Tried to set the config file '" + fileName
					+ "'. File name is already set to '" + xmlFileName + "'");
		}

		xmlFileName = fileName;
	}

	@SuppressWarnings("deprecation")
	@Deprecated
	@Property(name = ConfigurationConstants.mappingResource, mandatory = false)
	public void setResourceName(String xmlResourceName)
	{
		if (xmlFileName != null)
		{
			throw new IllegalArgumentException("EntityMetaDataReader already configured! Tried to set the config resource '" + xmlResourceName
					+ "'. Resource name is already set to '" + xmlFileName + "'");
		}

		xmlFileName = xmlResourceName;
	}

	protected void readConfig(Document[] docs)
	{
		ISet<EntityConfig> entities = new HashSet<EntityConfig>();
		for (Document doc : docs)
		{
			doc.normalizeDocument();
			String documentNamespace = xmlConfigUtil.readDocumentNamespace(doc);
			IOrmXmlReader ormXmlReader = ormXmlReaderRegistry.getOrmXmlReader(documentNamespace);

			ormXmlReader.loadFromDocument(doc, entities, entities);
		}

		for (EntityConfig entityConfig : entities)
		{
			Class<?> entityType = entityConfig.getEntityType();
			if (entityMetaDataProvider.getMetaData(entityType, true) != null)
			{
				continue;
			}
			Class<?> realType = entityConfig.getRealType();

			EntityMetaData metaData = new EntityMetaData();
			metaData.setEntityType(entityType);
			metaData.setRealType(realType);
			metaData.setLocalEntity(entityConfig.isLocal());

			entityMetaDataReader.addMembers(metaData, entityConfig);

			managedEntityMetaData.add(metaData);
			synchronized (entityMetaDataExtendable)
			{
				entityMetaDataExtendable.registerEntityMetaData(metaData);
			}
		}
	}
}
