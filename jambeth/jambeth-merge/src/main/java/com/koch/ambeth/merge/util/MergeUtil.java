package com.koch.ambeth.merge.util;

/*-
 * #%L
 * jambeth-merge
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

import com.koch.ambeth.merge.transfer.PrimitiveUpdateItem;
import com.koch.ambeth.merge.transfer.RelationUpdateItem;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.collections.IList;

public final class MergeUtil {
	public static PrimitiveUpdateItem createPrimitiveUpdateItem(String propertyName,
			Object newValue) {
		PrimitiveUpdateItem primitive = new PrimitiveUpdateItem();
		primitive.setMemberName(propertyName);
		primitive.setNewValue(newValue);
		return primitive;
	}

	public static RelationUpdateItem createRelationUpdateItem(String propertyName,
			IList<IObjRef> addedORIs, IList<IObjRef> removedORIs) {
		IObjRef[] addedORIsArray =
				(addedORIs != null && !addedORIs.isEmpty()) ? addedORIs.toArray(IObjRef.class) : null;
		IObjRef[] removedORIsArray =
				(removedORIs != null && !removedORIs.isEmpty()) ? removedORIs.toArray(IObjRef.class) : null;
		return createRelationUpdateItem(propertyName, addedORIsArray, removedORIsArray);
	}

	public static RelationUpdateItem createRelationUpdateItem(String propertyName,
			IObjRef[] addedORIs, IObjRef[] removedORIs) {
		RelationUpdateItem relation = new RelationUpdateItem();
		relation.setMemberName(propertyName);
		if (addedORIs != null) {
			relation.setAddedORIs(addedORIs);
		}
		if (removedORIs != null) {
			relation.setRemovedORIs(removedORIs);
		}

		return relation;
	}

	private MergeUtil() {
		// Intended blank
	}
}
