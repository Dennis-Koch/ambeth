package com.koch.ambeth.persistence.jdbc;

/*-
 * #%L
 * jambeth-persistence-jdbc
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

import com.koch.ambeth.persistence.IColumnEntry;

public class ColumnEntry implements IColumnEntry
{
	protected String fieldName;

	protected int columnIndex;

	protected Class<?> javaType;

	protected String typeName;

	protected boolean nullable;

	protected int radix;

	protected boolean expectsMapping;

	public ColumnEntry(String fieldName, int columnIndex, Class<?> javaType, String typeName, boolean nullable, int radix, boolean expectsMapping)
	{
		this.fieldName = fieldName;
		this.columnIndex = columnIndex;
		this.javaType = javaType;
		this.typeName = typeName;
		this.nullable = nullable;
		this.radix = radix;
		this.expectsMapping = expectsMapping;
	}

	@Override
	public String getFieldName()
	{
		return fieldName;
	}

	@Override
	public int getColumnIndex()
	{
		return columnIndex;
	}

	@Override
	public Class<?> getJavaType()
	{
		return javaType;
	}

	@Override
	public String getTypeName()
	{
		return typeName;
	}

	@Override
	public boolean isNullable()
	{
		return nullable;
	}

	@Override
	public int getRadix()
	{
		return radix;
	}

	@Override
	public boolean expectsMapping()
	{
		return expectsMapping;
	}
}
