package de.osthus.ambeth.persistence.blueprint;

import java.util.List;

import org.w3c.dom.Document;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.model.IAbstractEntity;
import de.osthus.ambeth.orm.blueprint.IBlueprintOrmProvider;
import de.osthus.ambeth.orm.blueprint.IBlueprintProvider;
import de.osthus.ambeth.orm.blueprint.IBlueprintVomProvider;
import de.osthus.ambeth.orm.blueprint.IEntityTypeBlueprint;
import de.osthus.ambeth.util.ReadWriteLock;

public class SQLOrmBlueprintProvider implements IBlueprintProvider, IBlueprintOrmProvider, IBlueprintVomProvider
{
	@Autowired
	protected InitialEntityTypeBluePrintLoadingService entityTypeBluePrintService;

	@Autowired
	protected IOrmDocumentCreator ormDocumentCreator;

	@Autowired
	protected IVomDocumentCreator vomDocumentCreator;

	protected ReadWriteLock lock = new ReadWriteLock();

	protected Document vomDocument;

	protected Document ormDocument;

	protected void prepareDocuments()
	{
		if (vomDocument == null)
		{
			lock.getWriteLock().lock();
			try
			{
				if (vomDocument == null)
				{
					List<EntityTypeBlueprint> allBluePrints = entityTypeBluePrintService.getAll();
					vomDocument = vomDocumentCreator.getVomDocument(allBluePrints.get(0).getName(), allBluePrints.get(0).getName() + "V");
					ormDocument = ormDocumentCreator.getOrmDocument(allBluePrints.get(0).getName(), null);
				}
			}
			finally
			{
				lock.releaseAllLocks();
			}
		}
	}

	@Override
	public IEntityTypeBlueprint resolveEntityTypeBlueprint(String entityTypeName)
	{
		return entityTypeBluePrintService.findByName(entityTypeName);
	}

	@Override
	public Document[] getOrmDocuments()
	{
		prepareDocuments();
		return new Document[] { ormDocument };
	}

	@Override
	public Document[] getVomDocuments()
	{
		prepareDocuments();
		return new Document[] { vomDocument };
	}

	@Override
	public Class<?> getDefaultInterface()
	{
		return IAbstractEntity.class;
	}
}
