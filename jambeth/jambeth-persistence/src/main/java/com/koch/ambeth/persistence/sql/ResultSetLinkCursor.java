package com.koch.ambeth.persistence.sql;

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
import com.koch.ambeth.util.ParamChecker;

public class ResultSetLinkCursor
		implements ILinkCursor, ILinkCursorItem, Iterator<ILinkCursorItem> {
	protected IResultSet resultSet;

	protected Iterator<Object[]> resultSetIter;

	protected Object fromId, toId;

	protected byte fromIdIndex, toIdIndex;

	public void afterPropertiesSet() {
		ParamChecker.assertNotNull(resultSet, "ResultSet");
	}

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

	public IResultSet getResultSet() {
		return resultSet;
	}

	public void setResultSet(IResultSet resultSet) {
		this.resultSet = resultSet;
	}

	@Override
	public Object getFromId() {
		return fromId;
	}

	@Override
	public Object getToId() {
		return toId;
	}

	@Override
	public boolean hasNext() {
		if (resultSetIter == null) {
			resultSetIter = resultSet.iterator();
		}
		return resultSetIter.hasNext();
	}

	@Override
	public ILinkCursorItem next() {
		if (resultSetIter == null) {
			resultSetIter = resultSet.iterator();
		}
		Object[] next = resultSetIter.next();
		if (next == null) {
			return null;
		}
		fromId = next[0];
		toId = next[1];
		return this;
	}

	@Override
	public void dispose() {
		if (resultSet != null) {
			resultSetIter = null;
			resultSet.dispose();
			resultSet = null;
		}
		fromId = null;
		toId = null;
	}

	@Override
	public Iterator<ILinkCursorItem> iterator() {
		return this;
	}
}
