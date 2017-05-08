package com.koch.ambeth.persistence.api.sql;

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

import java.util.List;

import com.koch.ambeth.util.appendable.IAppendable;

public interface ISqlBuilder {
	String escapeName(CharSequence symbolName);

	IAppendable escapeName(CharSequence symbolName, IAppendable sb);

	String escapeSchemaAndSymbolName(CharSequence schemaName, CharSequence symbolName);

	IAppendable appendNameValue(CharSequence name, Object value, IAppendable sb);

	IAppendable appendNameValues(CharSequence name, List<Object> values, IAppendable sb);

	IAppendable appendName(CharSequence name, IAppendable sb);

	IAppendable appendValue(Object value, IAppendable sb);

	String escapeValue(CharSequence value);

	IAppendable escapeValue(CharSequence value, IAppendable sb);

	boolean isUnescapedType(Class<?> type);

	String[] getSchemaAndTableName(String tableName);
}
