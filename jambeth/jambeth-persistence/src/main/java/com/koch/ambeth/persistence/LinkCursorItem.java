package com.koch.ambeth.persistence;

import com.koch.ambeth.persistence.api.ILinkCursorItem;

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

public class LinkCursorItem implements ILinkCursorItem {

	protected Object fromId, toId;

	public LinkCursorItem(Object fromId, Object toId) {
		this.fromId = fromId;
		this.toId = toId;
	}

	@Override
	public Object getFromId() {
		return fromId;
	}

	public void setFromId(Object fromId) {
		this.fromId = fromId;
	}

	@Override
	public Object getToId() {
		return toId;
	}

	public void setToId(Object toId) {
		this.toId = toId;
	}
}
