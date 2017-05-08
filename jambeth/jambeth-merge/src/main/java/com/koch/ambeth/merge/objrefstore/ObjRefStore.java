package com.koch.ambeth.merge.objrefstore;

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

import com.koch.ambeth.service.merge.model.IObjRef;

public abstract class ObjRefStore implements IObjRef {
	public static final int UNDEFINED_USAGE = -1;

	private ObjRefStore nextEntry;

	private int usageCount;

	public boolean isEqualTo(Class<?> entityType, byte idIndex, Object id) {
		return getId().equals(id) && getRealType().equals(entityType) && getIdNameIndex() == idIndex;
	}

	public ObjRefStore getNextEntry() {
		return nextEntry;
	}

	public void setNextEntry(ObjRefStore nextEntry) {
		this.nextEntry = nextEntry;
	}

	public void incUsageCount() {
		usageCount++;
	}

	public void decUsageCount() {
		usageCount--;
	}

	public void setUsageCount(int usageCount) {
		this.usageCount = usageCount;
	}

	public int getUsageCount() {
		return usageCount;
	}
}
