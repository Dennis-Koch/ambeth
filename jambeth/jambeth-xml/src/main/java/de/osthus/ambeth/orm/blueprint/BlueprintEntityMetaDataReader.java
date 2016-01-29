package de.osthus.ambeth.orm.blueprint;

import org.w3c.dom.Document;

import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.XmlBlueprintModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.config.AbstractEntityMetaDataReader;
import de.osthus.ambeth.orm.IOrmConfigGroup;

public class BlueprintEntityMetaDataReader extends AbstractEntityMetaDataReader implements IStartingBean, IRuntimeBlueprintEntityMetadataReader
{
	@LogInstance
	private ILogger log;

	@Autowired(optional = true)
	protected IBlueprintOrmProvider blueprintOrmProvider;

	@Autowired(XmlBlueprintModule.JAVASSIST_ORM_ENTITY_TYPE_PROVIDER)
	protected JavassistOrmEntityTypeProvider entityTypeProvider;

	@Override
	public void afterStarted() throws Throwable
	{
		if (blueprintOrmProvider != null)
		{
			Document[] ormDocuments = blueprintOrmProvider.getOrmDocuments();
			IOrmConfigGroup ormConfigGroup = ormConfigGroupProvider.getOrmConfigGroup(ormDocuments, entityTypeProvider);
			readConfig(ormConfigGroup);
		}
	}

	@Override
	public void addEntityBlueprintOrm(IEntityTypeBlueprint entityTypeBlueprint)
	{
		Document ormDocument = blueprintOrmProvider.getOrmDocument(entityTypeBlueprint);
		IOrmConfigGroup ormConfigGroup = ormConfigGroupProvider.getOrmConfigGroup(new Document[] { ormDocument }, entityTypeProvider);
		readConfig(ormConfigGroup);
	}
}
