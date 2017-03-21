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

public class BlueprintValueObjectConfigReader extends ValueObjectConfigReader
		implements IStartingBean, IRuntimeBlueprintVomReader {
	@LogInstance
	private ILogger log;

	@Autowired(optional = true)
	protected IBlueprintVomProvider blueprintVomProvider;

	@Autowired(XmlBlueprintModule.JAVASSIST_ORM_ENTITY_TYPE_PROVIDER)
	protected JavassistOrmEntityTypeProvider entityTypeProvider;

	@Override
	public void afterPropertiesSet() {
		ormEntityTypeProvider = entityTypeProvider;
	}

	@Override
	public void afterStarted() throws Throwable {
		if (blueprintVomProvider != null) {
			Document[] docs = blueprintVomProvider.getVomDocuments();
			readAndConsumeDocs(docs);
		}
	}

	@Override
	public void addEntityBlueprintVom(String businessObjectType, String valueObjectType) {
		Document doc = blueprintVomProvider.getVomDocument(businessObjectType, valueObjectType);
		readAndConsumeDocs(new Document[] {doc});
	}

	protected void readAndConsumeDocs(Document[] docs) {
		HashMap<Class<?>, List<Element>> configsToConsume = readConfig(docs);
		for (Entry<Class<?>, List<Element>> entry : configsToConsume) {
			Class<?> entityType = entry.getKey();
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType, true);
			if (metaData == null) {
				if (log.isWarnEnabled()) {
					log.warn("Could not resolve entity meta data for '" + entityType.getName() + "'");
				}
			}
			else {
				consumeConfigs(metaData, entry.getValue());
			}
		}
	}

}
