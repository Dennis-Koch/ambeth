package com.koch.ambeth.persistence;

import java.util.Iterator;

/*-
 * #%L
 * jambeth-persistence
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

import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.query.persistence.IEntityCursor;
import com.koch.ambeth.query.persistence.IVersionCursor;
import com.koch.ambeth.query.persistence.IVersionItem;

public class EntityCursor<T> implements IEntityCursor<T>, Iterator<T> {
	protected IVersionCursor cursor;

	protected Iterator<IVersionItem> cursorIter;

	protected Class<T> entityType;

	protected ICache cache;

	protected IServiceUtil serviceUtil;

	public EntityCursor(IVersionCursor cursor, Class<T> entityType, ICache cache) {
		this.cursor = cursor;
		this.entityType = entityType;
		this.cache = cache;
	}

	public EntityCursor(IVersionCursor cursor, Class<T> entityType, IServiceUtil serviceUtil) {
		this.cursor = cursor;
		this.entityType = entityType;
		this.serviceUtil = serviceUtil;
	}

	@Override
	public boolean hasNext() {
		if (cursorIter == null) {
			cursorIter = cursor.iterator();
		}
		return cursorIter.hasNext();
	}

	@Override
	public T next() {
		if (cursorIter == null) {
			cursorIter = cursor.iterator();
		}
		IVersionItem item = this.cursorIter.next();
		if (item == null) {
			return null;
		}
		if (this.serviceUtil != null) {
			return this.serviceUtil.loadObject(this.entityType, item);
		}
		return this.cache.getObject(this.entityType, item);
	}

	@Override
	public void dispose() {
		if (cursor != null) {
			cursor.dispose();
			cursor = null;
		}
		cache = null;
	}

	@Override
	public Iterator<T> iterator() {
		return this;
	}
}
