package com.koch.ambeth.persistence;

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

public class LinkCursor extends BasicEnumerator<ILinkCursorItem>
		implements ILinkCursor, ILinkCursorItem {
	protected IList<LinkCursorItem> items;
	protected int currentIndex = -1;

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

	public void setItems(IList<LinkCursorItem> items) {
		this.items = items;
	}

	@Override
	public ILinkCursorItem getCurrent() {
		if (currentIndex == -1) {
			return null;
		}
		else {
			return this;
		}
	}

	@Override
	public boolean moveNext() {
		if (items.size() == currentIndex + 1) {
			return false;
		}
		currentIndex++;
		return true;
	}

	@Override
	public void reset() {
		currentIndex = -1;
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

}
