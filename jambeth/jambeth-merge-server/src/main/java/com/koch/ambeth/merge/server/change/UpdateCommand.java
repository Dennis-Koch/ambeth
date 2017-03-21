package com.koch.ambeth.merge.server.change;

/*-
 * #%L
 * jambeth-merge-server
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

import java.util.Map.Entry;

import com.koch.ambeth.merge.model.IChangeContainer;
import com.koch.ambeth.merge.model.ICreateOrUpdateContainer;
import com.koch.ambeth.merge.model.IPrimitiveUpdateItem;
import com.koch.ambeth.persistence.api.IFieldMetaData;
import com.koch.ambeth.persistence.api.ITable;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.collections.EmptyMap;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.LinkedHashMap;
import com.koch.ambeth.util.collections.ReadOnlyMapWrapper;

public class UpdateCommand extends AbstractChangeCommand implements IUpdateCommand {
	private static final ILinkedMap<IFieldMetaData, Object> emptyItems =
			EmptyMap.<IFieldMetaData, Object>emptyMap();

	protected ILinkedMap<IFieldMetaData, Object> items = emptyItems;
	protected ILinkedMap<IFieldMetaData, Object> roItems = emptyItems;

	public UpdateCommand(IObjRef reference) {
		super(reference);
	}

	@Override
	public void configureFromContainer(IChangeContainer changeContainer, ITable table) {
		ICreateOrUpdateContainer container = (ICreateOrUpdateContainer) changeContainer;
		super.configureFromContainer(changeContainer, table);

		IPrimitiveUpdateItem[] fullPUIs = container.getFullPUIs();
		if (fullPUIs != null) {
			ensureWritableMap();
			repackPuis(fullPUIs, items);
		}
	}

	@Override
	public IChangeCommand addCommand(ICreateCommand other) {
		return other.addCommand(this);
	}

	@Override
	public IChangeCommand addCommand(IUpdateCommand other) {
		ILinkedMap<IFieldMetaData, Object> otherItems = other.getItems();
		if (otherItems != emptyItems) {
			for (Entry<IFieldMetaData, Object> entry : otherItems) {
				Object actualValue = items.get(entry.getKey());
				if (actualValue == null) {
					put(entry.getKey(), entry.getValue());
				}
				else if (entry.getValue() != null && !actualValue.equals(entry.getValue())) {
					throw new IllegalCommandException("Two different values for the same field ("
							+ entry.getKey() + ": '" + actualValue + "' <-> '" + entry.getValue() + "'");
				}
			}
		}

		return this;
	}

	@Override
	public IChangeCommand addCommand(IDeleteCommand other) {
		return other;
	}

	public void put(IFieldMetaData field, Object foreignKey) {
		ensureWritableMap();
		items.put(field, foreignKey);
	}

	@Override
	public ILinkedMap<IFieldMetaData, Object> getItems() {
		return roItems;
	}

	protected void ensureWritableMap() {
		if (items == emptyItems) {
			items = new LinkedHashMap<>();
			roItems = new ReadOnlyMapWrapper<>(items);
		}
	}
}
