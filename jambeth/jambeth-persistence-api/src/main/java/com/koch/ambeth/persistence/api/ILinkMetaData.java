package com.koch.ambeth.persistence.api;

/*-
 * #%L
 * jambeth-persistence-api
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

public interface ILinkMetaData {
	ITableMetaData getFromTable();

	ITableMetaData getToTable();

	IFieldMetaData getFromField();

	IFieldMetaData getToField();

	boolean isNullable();

	boolean hasLinkTable();

	IDirectedLinkMetaData getDirectedLink();

	IDirectedLinkMetaData getReverseDirectedLink();

	String getName();

	String getTableName();

	/**
	 * Getter for the table name which is in quotes to allow to include the value directly in a query
	 * string
	 *
	 * @return
	 */
	String getFullqualifiedEscapedTableName();

	String getArchiveTableName();
}
