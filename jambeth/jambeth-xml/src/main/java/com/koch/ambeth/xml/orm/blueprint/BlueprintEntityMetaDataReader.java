package com.koch.ambeth.xml.orm.blueprint;

import org.w3c.dom.Document;

import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.config.AbstractEntityMetaDataReader;
import com.koch.ambeth.merge.orm.IOrmConfigGroup;
import com.koch.ambeth.merge.orm.blueprint.IBlueprintOrmProvider;
import com.koch.ambeth.merge.orm.blueprint.IEntityTypeBlueprint;
import com.koch.ambeth.merge.orm.blueprint.IOrmDatabaseMapper;
import com.koch.ambeth.merge.orm.blueprint.IRuntimeBlueprintEntityMetadataReader;
import com.koch.ambeth.xml.ioc.XmlBlueprintModule;

public class BlueprintEntityMetaDataReader extends AbstractEntityMetaDataReader implements IStartingBean, IRuntimeBlueprintEntityMetadataReader
{
	@LogInstance
	private ILogger log;

	@Autowired(optional = true)
	protected IBlueprintOrmProvider blueprintOrmProvider;

	@Autowired(XmlBlueprintModule.JAVASSIST_ORM_ENTITY_TYPE_PROVIDER)
	protected JavassistOrmEntityTypeProvider entityTypeProvider;

	@Autowired(optional = true)
	protected IOrmDatabaseMapper blueprintDatabaseMapper;

	@Override
	public void afterStarted() throws Throwable
	{
		if (blueprintOrmProvider != null && blueprintDatabaseMapper != null)
		{
			Document[] ormDocuments = blueprintOrmProvider.getOrmDocuments();
			IOrmConfigGroup ormConfigGroup = ormConfigGroupProvider.getOrmConfigGroup(ormDocuments, entityTypeProvider);
			readConfig(ormConfigGroup);
			blueprintDatabaseMapper.mapFields(ormConfigGroup);
		}
	}

	@Override
	public void addEntityBlueprintOrm(IEntityTypeBlueprint entityTypeBlueprint)
	{
		if (blueprintOrmProvider != null && blueprintDatabaseMapper != null)
		{
			Document ormDocument = blueprintOrmProvider.getOrmDocument(entityTypeBlueprint);
			IOrmConfigGroup ormConfigGroup = ormConfigGroupProvider.getOrmConfigGroup(new Document[] { ormDocument }, entityTypeProvider);
			readConfig(ormConfigGroup);
			blueprintDatabaseMapper.mapFields(ormConfigGroup);
		}
	}
}
