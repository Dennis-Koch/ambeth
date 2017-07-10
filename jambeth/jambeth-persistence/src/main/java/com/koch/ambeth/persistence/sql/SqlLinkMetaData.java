package com.koch.ambeth.persistence.sql;

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

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.persistence.IPersistenceHelper;
import com.koch.ambeth.persistence.LinkMetaData;
import com.koch.ambeth.persistence.api.IFieldMetaData;
import com.koch.ambeth.persistence.api.sql.ISqlBuilder;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;

public class SqlLinkMetaData extends LinkMetaData {
	protected String constraintName;

	protected IFieldMetaData fromField;

	protected IFieldMetaData toField;

	@Autowired
	protected IPersistenceHelper persistenceHelper;

	@Autowired
	protected ISqlConnection sqlConnection;

	@Autowired
	protected ISqlBuilder sqlBuilder;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected IConversionHelper conversionHelper;

	protected String fullqualifiedEscapedTableName;

	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();

		ParamChecker.assertTrue(fromField != null || toField != null, "FromField or ToField");
	}

	public void setConstraintName(String constraintName) {
		this.constraintName = constraintName;
	}

	@Override
	public String getFullqualifiedEscapedTableName() {
		return fullqualifiedEscapedTableName;
	}

	public void setFullqualifiedEscapedTableName(String fullqualifiedEscapedTableName) {
		this.fullqualifiedEscapedTableName = fullqualifiedEscapedTableName;
	}

	public String getConstraintName() {
		return constraintName;
	}

	@Override
	public IFieldMetaData getFromField() {
		return fromField;
	}

	public void setFromField(IFieldMetaData fromField) {
		this.fromField = fromField;
	}

	@Override
	public IFieldMetaData getToField() {
		return toField;
	}

	public void setToField(IFieldMetaData toField) {
		this.toField = toField;
	}
}
