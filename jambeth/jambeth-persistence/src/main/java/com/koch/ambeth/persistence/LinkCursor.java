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

import com.koch.ambeth.persistence.api.ILinkCursor;
import com.koch.ambeth.persistence.api.ILinkCursorItem;
import com.koch.ambeth.util.collections.IList;

public class LinkCursor implements ILinkCursor, ILinkCursorItem, Iterator<ILinkCursorItem> {
	protected IList<? extends ILinkCursorItem> items;
	protected int currentIndex;

	protected byte fromIdIndex, toIdIndex;

	@Override
	public byte getFromIdIndex() {
		return fromIdIndex;
	}

	public void setFromIdIndex(byte fromIdIndex) {
		this.fromIdIndex = fromIdIndex;
	}

	@Override
	public byte getToIdIndex() {
		return toIdIndex;
	}

	public void setToIdIndex(byte toIdIndex) {
		this.toIdIndex = toIdIndex;
	}

	public void setItems(IList<? extends ILinkCursorItem> items) {
		this.items = items;
	}

	@Override
	public boolean hasNext() {
		return items.size() > currentIndex;
	}

	@Override
	public ILinkCursorItem next() {
		currentIndex++;
		return items.get(currentIndex);
	}

	@Override
	public void dispose() {
		items = null;
	}

	@Override
	public Object getFromId() {
		return items.get(currentIndex).getFromId();
	}

	@Override
	public Object getToId() {
		return items.get(currentIndex).getToId();
	}

	@Override
	public Iterator<ILinkCursorItem> iterator() {
		return this;
	}
}
