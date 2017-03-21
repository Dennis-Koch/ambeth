package com.koch.ambeth.merge.orm;

/*-
 * #%L
 * jambeth-merge
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
