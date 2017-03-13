package com.koch.ambeth.merge.orm;

public class MemberConfig extends AbstractMemberConfig
{
	private String columnName;

	public MemberConfig(String name)
	{
		this(name, null);
	}

	public MemberConfig(String name, String columnName)
	{
		super(name);
		this.columnName = columnName;
	}

	public String getColumnName()
	{
		return columnName;
	}

	public void setColumnName(String columnName)
	{
		this.columnName = columnName;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof MemberConfig)
		{
			return equals((AbstractMemberConfig) obj);
		}
		else
		{
			return false;
		}
	}

	@Override
	public int hashCode()
	{
		return getClass().hashCode() ^ getName().hashCode();
	}

	@Override
	public String toString()
	{
		return getName();
	}
}
