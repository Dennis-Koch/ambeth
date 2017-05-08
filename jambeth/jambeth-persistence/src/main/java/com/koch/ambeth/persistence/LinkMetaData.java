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

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.persistence.api.IDirectedLinkMetaData;
import com.koch.ambeth.persistence.api.IFieldMetaData;
import com.koch.ambeth.persistence.api.ILinkMetaData;
import com.koch.ambeth.persistence.api.ITableMetaData;
import com.koch.ambeth.util.ParamChecker;

public class LinkMetaData implements ILinkMetaData, IInitializingBean {
	protected ITableMetaData fromTable;

	protected ITableMetaData toTable;

	protected boolean nullable = false;

	protected IDirectedLinkMetaData directedLink;

	protected IDirectedLinkMetaData reverseDirectedLink;

	protected String name;

	protected String tableName;

	protected String archiveTableName;

	@Override
	public void afterPropertiesSet() {
		ParamChecker.assertNotNull(name, "name");
		ParamChecker.assertNotNull(fromTable, "fromTable");
		ParamChecker.assertNotNull(directedLink, "directedLink");
		ParamChecker.assertTrue(toTable != null || directedLink.getToMember() != null,
				"toTable or toMember");
		ParamChecker.assertNotNull(reverseDirectedLink, "reverseDirectedLink");
	}

	@Override
	public ITableMetaData getFromTable() {
		return fromTable;
	}

	public void setFromTable(ITableMetaData fromTable) {
		this.fromTable = fromTable;
	}

	@Override
	public ITableMetaData getToTable() {
		return toTable;
	}

	public void setToTable(ITableMetaData toTable) {
		this.toTable = toTable;
	}

	@Override
	public IFieldMetaData getFromField() {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public IFieldMetaData getToField() {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public boolean isNullable() {
		return nullable;
	}

	@Override
	public boolean hasLinkTable() {
		return directedLink.isStandaloneLink() && reverseDirectedLink.isStandaloneLink();
	}

	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}

	@Override
	public IDirectedLinkMetaData getDirectedLink() {
		return directedLink;
	}

	public void setDirectedLink(IDirectedLinkMetaData directedLink) {
		this.directedLink = directedLink;
	}

	@Override
	public IDirectedLinkMetaData getReverseDirectedLink() {
		return reverseDirectedLink;
	}

	public void setReverseDirectedLink(IDirectedLinkMetaData reverseDirectedLink) {
		this.reverseDirectedLink = reverseDirectedLink;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getTableName() {
		if (hasLinkTable()) {
			return name;
		}
		else {
			return tableName;
		}
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	@Override
	public String getFullqualifiedEscapedTableName() {
		return getTableName();
	}

	@Override
	public String getArchiveTableName() {
		return archiveTableName;
	}

	public void setArchiveTableName(String archiveTableName) {
		this.archiveTableName = archiveTableName;
	}

	@Override
	public String toString() {
		return "Link: " + getName();
	}
}
