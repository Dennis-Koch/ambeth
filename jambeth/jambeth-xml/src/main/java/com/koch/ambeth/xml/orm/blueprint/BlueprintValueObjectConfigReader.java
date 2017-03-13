package com.koch.ambeth.xml.orm.blueprint;

import java.util.List;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.config.ValueObjectConfigReader;
import com.koch.ambeth.merge.orm.blueprint.IBlueprintVomProvider;
import com.koch.ambeth.merge.orm.blueprint.IRuntimeBlueprintVomReader;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.xml.ioc.XmlBlueprintModule;

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
			readAndConsumeDocs(docs);
		}
	}

	@Override
	public void addEntityBlueprintVom(String businessObjectType, String valueObjectType)
	{
		Document doc = blueprintVomProvider.getVomDocument(businessObjectType, valueObjectType);
		readAndConsumeDocs(new Document[] { doc });
	}

	protected void readAndConsumeDocs(Document[] docs)
	{
		HashMap<Class<?>, List<Element>> configsToConsume = readConfig(docs);
		for (Entry<Class<?>, List<Element>> entry : configsToConsume)
		{
			Class<?> entityType = entry.getKey();
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType, true);
			if (metaData == null)
			{
				if (log.isWarnEnabled())
				{
					log.warn("Could not resolve entity meta data for '" + entityType.getName() + "'");
				}
			}
			else
			{
				consumeConfigs(metaData, entry.getValue());
			}
		}
	}

}
