package de.osthus.ambeth.orm.blueprint;

import java.util.List;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.XmlBlueprintModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.config.ValueObjectConfigReader;
import de.osthus.ambeth.merge.model.IEntityMetaData;

public class BlueprintValueObjectConfigReader extends ValueObjectConfigReader implements IStartingBean, IRuntimeBlueprintVomReader
{
	@LogInstance
	private ILogger log;

	@Autowired(optional = true)
	protected IBlueprintVomProvider blueprintVomProvider;

	@Autowired(XmlBlueprintModule.JAVASSIST_ORM_ENTITY_TYPE_PROVIDER)
	protected JavassistOrmEntityTypeProvider entityTypeProvider;

	@Override
	public void afterPropertiesSet()
	{
		ormEntityTypeProvider = entityTypeProvider;
	}

	@Override
	public void afterStarted() throws Throwable
	{
		if (blueprintVomProvider != null)
		{
			Document[] docs = blueprintVomProvider.getVomDocuments();
			configsToConsume = readConfig(docs);

			for (Entry<Class<?>, List<Element>> entry : configsToConsume)
			{
				Class<?> entityType = entry.getKey();
				IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType, true);
				if (metaData == null)
				{
					if (log.isInfoEnabled())
					{
						log.info("Could not resolve entity meta data for '" + entityType.getName() + "'");
					}
				}
				else
				{
					consumeConfigs(metaData, entry.getValue());
				}
			}
		}
	}

	public void addEntityBlueprintVom(IEntityTypeBlueprint entityTypeBlueprint)
	{
		Document doc = blueprintVomProvider.getVomDocument(entityTypeBlueprint);
		HashMap<Class<?>, List<Element>> newConfigsToConsume = readConfig(new Document[] { doc });

		for (Entry<Class<?>, List<Element>> entry : newConfigsToConsume)
		{
			Class<?> entityType = entry.getKey();
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType, true);
			if (metaData == null)
			{
				if (log.isInfoEnabled())
				{
					log.info("Could not resolve entity meta data for '" + entityType.getName() + "'");
				}
			}
			else
			{
				consumeConfigs(metaData, entry.getValue());
			}
		}
	}

}
