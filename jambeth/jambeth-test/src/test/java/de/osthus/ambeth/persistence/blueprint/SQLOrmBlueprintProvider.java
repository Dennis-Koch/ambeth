package de.osthus.ambeth.persistence.blueprint;

import org.w3c.dom.Document;

import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.model.IAbstractEntity;
import de.osthus.ambeth.orm.blueprint.IBlueprintOrmProvider;
import de.osthus.ambeth.orm.blueprint.IBlueprintProvider;
import de.osthus.ambeth.orm.blueprint.IBlueprintVomProvider;
import de.osthus.ambeth.orm.blueprint.IEntityTypeBlueprint;

public class SQLOrmBlueprintProvider implements IBlueprintProvider, IInitializingBean, IBlueprintOrmProvider, IBlueprintVomProvider
{
	@Autowired
	protected ICache cache;

	@Autowired
	protected IOrmDocumentCreator ormDocumentCreator;

	@Autowired
	protected IVomDocumentCreator vomDocumentCreator;

	protected Document vomDocument;

	protected Document ormDocument;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		vomDocument = vomDocumentCreator
				.getVomDocument("de.osthus.ambeth.persistence.blueprint.TestClass", "de.osthus.ambeth.persistence.blueprint.TestClassV");
		ormDocument = ormDocumentCreator.getOrmDocument("de.osthus.ambeth.persistence.blueprint.TestClass", null);
	}

	@Override
	public IEntityTypeBlueprint resolveEntityTypeBlueprint(String entityTypeName)
	{
		return cache.getObject(IEntityTypeBlueprint.class, IEntityTypeBlueprint.NAME, entityTypeName);
	}

	@Override
	public Document[] getOrmDocuments()
	{

		return new Document[] { ormDocument };
	}

	@Override
	public Document[] getVomDocuments()
	{

		return new Document[] { vomDocument };
	}

	@Override
	public Class<?> getDefaultInterface()
	{
		return IAbstractEntity.class;
	}
}
