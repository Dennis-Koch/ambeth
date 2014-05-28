package de.osthus.ambeth.persistence.jdbc;

public class ColumnEntry
{
	protected String fieldName;

	protected int columnIndex;

	protected int jdbcTypeIndex;

	protected String typeName;

	protected boolean nullable;

	protected int size;

	protected int digits;

	protected int radix;

	public ColumnEntry(String fieldName, int columnIndex, int jdbcTypeIndex, String typeName, boolean nullable, int size, int digits, int radix)
	{
		this.fieldName = fieldName;
		this.columnIndex = columnIndex;
		this.jdbcTypeIndex = jdbcTypeIndex;
		this.typeName = typeName;
		this.nullable = nullable;
		this.size = size;
		this.digits = digits;
		this.radix = radix;
	}

	public String getFieldName()
	{
		return fieldName;
	}

	public int getColumnIndex()
	{
		return columnIndex;
	}

	public int getJdbcTypeIndex()
	{
		return jdbcTypeIndex;
	}

	public String getTypeName()
	{
		return typeName;
	}

	public boolean isNullable()
	{
		return nullable;
	}

	public int getSize()
	{
		return size;
	}

	public int getDigits()
	{
		return digits;
	}

	public int getRadix()
	{
		return radix;
	}
}
