package com.koch.ambeth.persistence.blueprint;

import java.util.List;

import org.w3c.dom.Document;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.orm.blueprint.IBlueprintOrmProvider;
import com.koch.ambeth.merge.orm.blueprint.IBlueprintProvider;
import com.koch.ambeth.merge.orm.blueprint.IBlueprintVomProvider;
import com.koch.ambeth.merge.orm.blueprint.IEntityTypeBlueprint;
import com.koch.ambeth.model.IAbstractEntity;
import com.koch.ambeth.util.ReadWriteLock;

public class SQLOrmBlueprintProvider
		implements IBlueprintProvider, IBlueprintOrmProvider, IBlueprintVomProvider {
	// @Autowired
	// protected InitialEntityTypeBluePrintLoadingService entityTypeBluePrintService;

	@Autowired
	protected IOrmDocumentCreator ormDocumentCreator;

	@Autowired
	protected IVomDocumentCreator vomDocumentCreator;

	@Autowired
	protected EntityTypeBluePrintService entityTypeBluePrintService;

	protected ReadWriteLock lock = new ReadWriteLock();

	protected Document vomDocument;

	protected Document ormDocument;

	protected void prepareDocuments() {
		if (vomDocument == null) {
			lock.getWriteLock().lock();
			try {
				if (vomDocument == null) {
					List<EntityTypeBlueprint> allBluePrints = entityTypeBluePrintService.getAll();
					vomDocument = vomDocumentCreator.getVomDocument(allBluePrints.get(0).getName(),
							allBluePrints.get(0).getName() + "V");
					ormDocument = ormDocumentCreator.getOrmDocument(allBluePrints.get(0).getName(), null);
				}
			}
			finally {
				lock.releaseAllLocks();
			}
		}
	}

	@Override
	public IEntityTypeBlueprint resolveEntityTypeBlueprint(String entityTypeName) {
		return entityTypeBluePrintService.findByName(entityTypeName);
	}

	@Override
	public Document[] getOrmDocuments() {
		prepareDocuments();
		return new Document[] {ormDocument};
	}

	@Override
	public Document[] getVomDocuments() {
		prepareDocuments();
		return new Document[] {vomDocument};
	}

	@Override
	public Class<?> getDefaultInterface() {
		return IAbstractEntity.class;
	}

	@Override
	public Document getVomDocument(String businessObjectType, String valueObjectType) {
		return null;
	}

	@Override
	public Document getOrmDocument(IEntityTypeBlueprint entityTypeBlueprint) {
		return ormDocumentCreator.getOrmDocument(entityTypeBlueprint.getName(), null);
	}

	@Override
	public List<? extends IEntityTypeBlueprint> getAll() {
		return entityTypeBluePrintService.getAll();
	}
}
