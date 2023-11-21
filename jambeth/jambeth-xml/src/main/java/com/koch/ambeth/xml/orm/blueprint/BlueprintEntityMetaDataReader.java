package com.koch.ambeth.xml.orm.blueprint;

/*-
 * #%L
 * jambeth-xml
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

public class BlueprintEntityMetaDataReader extends AbstractEntityMetaDataReader
		implements IStartingBean, IRuntimeBlueprintEntityMetadataReader {
	@LogInstance
	private ILogger log;

	@Autowired(optional = true)
	protected IBlueprintOrmProvider blueprintOrmProvider;

	@Autowired(XmlBlueprintModule.JAVASSIST_ORM_ENTITY_TYPE_PROVIDER)
	protected JavassistOrmEntityTypeProvider entityTypeProvider;

	@Autowired(optional = true)
	protected IOrmDatabaseMapper blueprintDatabaseMapper;

	@Override
	public void afterStarted() throws Throwable {
		if (blueprintOrmProvider != null && blueprintDatabaseMapper != null) {
			var ormDocuments = blueprintOrmProvider.getOrmDocuments();
			var ormConfigGroup = ormConfigGroupProvider.getOrmConfigGroup(ormDocuments, entityTypeProvider);
			readConfig(ormConfigGroup);
			blueprintDatabaseMapper.mapFields(ormConfigGroup);
		}
	}

	@Override
	public void addEntityBlueprintOrm(IEntityTypeBlueprint entityTypeBlueprint) {
		if (blueprintOrmProvider != null && blueprintDatabaseMapper != null) {
			var ormDocument = blueprintOrmProvider.getOrmDocument(entityTypeBlueprint);
			var ormConfigGroup = ormConfigGroupProvider.getOrmConfigGroup(new Document[] {ormDocument}, entityTypeProvider);
			readConfig(ormConfigGroup);
			blueprintDatabaseMapper.mapFields(ormConfigGroup);
		}
	}
}
