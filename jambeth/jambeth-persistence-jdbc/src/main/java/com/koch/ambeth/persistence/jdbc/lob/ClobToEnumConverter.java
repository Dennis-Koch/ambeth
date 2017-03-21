package com.koch.ambeth.persistence.jdbc.lob;

/*-
 * #%L
 * jambeth-persistence-jdbc
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

import java.sql.Clob;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.event.EntityMetaDataAddedEvent;
import com.koch.ambeth.merge.event.EntityMetaDataRemovedEvent;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.metadata.PrimitiveMember;
import com.koch.ambeth.util.IDedicatedConverterExtendable;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.SmartCopyMap;

public class ClobToEnumConverter extends ClobToAnythingConverter {
	public static final String HANDLE_ENTITY_META_DATA_ADDED_EVENT = "handleEntityMetaDataAddedEvent";

	public static final String HANDLE_ENTITY_META_DATA_REMOVED_EVENT =
			"handleEntityMetaDataRemovedEvent";

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IDedicatedConverterExtendable dedicatedConverterExtendable;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	protected final SmartCopyMap<Class<?>, Integer> propertyTypeToUsageCountMap =
			new SmartCopyMap<>(0.5f);

	protected HashMap<Class<?>, Runnable> deregisterRunnables = new HashMap<>();

	public void handleEntityMetaDataAddedEvent(EntityMetaDataAddedEvent evnt) {
		for (Class<?> entityType : evnt.getEntityTypes()) {
			final IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
			for (PrimitiveMember member : metaData.getPrimitiveMembers()) {
				Class<?> elementType = member.getElementType();
				if (!elementType.isEnum()) {
					continue;
				}
				Integer usageCount = propertyTypeToUsageCountMap.get(elementType);
				if (usageCount == null) {
					usageCount = Integer.valueOf(1);
					dedicatedConverterExtendable.registerDedicatedConverter(this, Clob.class, elementType);
					dedicatedConverterExtendable.registerDedicatedConverter(this, elementType, Clob.class);
				}
				else {
					usageCount = Integer.valueOf(usageCount.intValue() + 1);
				}
				propertyTypeToUsageCountMap.put(elementType, usageCount);
			}
			deregisterRunnables.put(entityType, new Runnable() {
				@Override
				public void run() {
					for (PrimitiveMember member : metaData.getPrimitiveMembers()) {
						Class<?> elementType = member.getElementType();
						if (!elementType.isEnum()) {
							continue;
						}
						Integer usageCount = propertyTypeToUsageCountMap.get(elementType);
						if (usageCount == null) {
							throw new IllegalStateException("Must never happen");
						}
						usageCount = Integer.valueOf(usageCount.intValue() - 1);
						if (usageCount.intValue() > 0) {
							dedicatedConverterExtendable.unregisterDedicatedConverter(ClobToEnumConverter.this,
									Clob.class, elementType);
							dedicatedConverterExtendable.unregisterDedicatedConverter(ClobToEnumConverter.this,
									elementType, Clob.class);
						}
						propertyTypeToUsageCountMap.put(elementType, usageCount);
					}
				}
			});
		}
	}

	public void handleEntityMetaDataRemovedEvent(EntityMetaDataRemovedEvent evnt) {
		for (Class<?> entityType : evnt.getEntityTypes()) {
			Runnable runnable = deregisterRunnables.get(entityType);
			if (runnable != null) {
				runnable.run();
			}
			deregisterRunnables.remove(entityType);
		}
	}
}
