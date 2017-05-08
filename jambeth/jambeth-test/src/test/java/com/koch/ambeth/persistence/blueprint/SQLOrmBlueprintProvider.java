package com.koch.ambeth.persistence.blueprint;

/*-
 * #%L
 * jambeth-test
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
