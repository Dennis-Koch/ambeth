package com.koch.ambeth.persistence.jdbc;

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
