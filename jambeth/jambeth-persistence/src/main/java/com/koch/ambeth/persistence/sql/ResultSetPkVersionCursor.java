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

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.query.persistence.IVersionCursor;
import com.koch.ambeth.query.persistence.IVersionItem;
import com.koch.ambeth.util.IDisposable;
import com.koch.ambeth.util.ParamChecker;

public class ResultSetPkVersionCursor implements IVersionCursor, IVersionItem, IDisposable, IInitializingBean
{
	protected IResultSet resultSet;

	protected Object id, version;

	protected boolean containsVersion = true;

	@Override
	public void afterPropertiesSet()
	{
		ParamChecker.assertNotNull(resultSet, "ResultSet");
	}

	public void setContainsVersion(boolean containsVersion)
	{
		this.containsVersion = containsVersion;
	}

	public void setResultSet(IResultSet resultSet)
	{
		this.resultSet = resultSet;
	}

	@Override
	public Object getId()
	{
		return id;
	}

	@Override
	public Object getId(int idIndex)
	{
		if (idIndex == ObjRef.PRIMARY_KEY_INDEX)
		{
			return getId();
		}
		throw new UnsupportedOperationException("No alternate ids have been fetched");
	}

	@Override
	public Object getVersion()
	{
		return version;
	}

	@Override
	public int getAlternateIdCount()
	{
		return 0;
	}

	@Override
	public IVersionItem getCurrent()
	{
		return this;
	}

	@Override
	public boolean moveNext()
	{
		IResultSet resultSet = this.resultSet;
		if (resultSet.moveNext())
		{
			Object[] current = resultSet.getCurrent();
			id = current[0];
			if (containsVersion)
			{
				version = current[1];
			}
			return true;
		}
		return false;
	}

	@Override
	public void dispose()
	{
		if (resultSet != null)
		{
			resultSet.dispose();
			resultSet = null;
		}
	}
}
