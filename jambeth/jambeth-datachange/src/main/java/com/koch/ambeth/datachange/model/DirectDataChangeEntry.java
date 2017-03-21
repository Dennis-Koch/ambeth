package com.koch.ambeth.datachange.model;

/*-
 * #%L
 * jambeth-datachange
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

public class DirectDataChangeEntry implements IDataChangeEntry
{
	protected Object entry;

	public DirectDataChangeEntry(Object entry)
	{
		this.entry = entry;
	}

	public Object getEntry()
	{
		return entry;
	}

	@Override
	public Class<?> getEntityType()
	{
		return this.entry.getClass();
	}

	@Override
	public Object getId()
	{
		return null;
	}

	@Override
	public byte getIdNameIndex()
	{
		return -1;
	}

	@Override
	public Object getVersion()
	{
		return null;
	}

	@Override
	public String[] getTopics()
	{
		return null;
	}

	@Override
	public void setTopics(String[] topics)
	{
	}
}
