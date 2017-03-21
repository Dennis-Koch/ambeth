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

import java.util.List;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.util.IDisposable;
import com.koch.ambeth.util.ParamChecker;

public class CompositeResultSet implements IResultSet, IInitializingBean, IDisposable
{
	protected List<IResultSetProvider> resultSetProviderStack;
	protected IResultSet resultSet;

	@Override
	public void afterPropertiesSet()
	{
		ParamChecker.assertNotNull(this.resultSetProviderStack, "ResultSetProviderStack");
	}

	@Override
	public void dispose()
	{
		if (this.resultSet != null)
		{
			this.resultSet.dispose();
			this.resultSet = null;
		}
		if (this.resultSetProviderStack != null)
		{
			for (IResultSetProvider resultSetProvider : this.resultSetProviderStack)
			{
				resultSetProvider.skipResultSet();
			}
			this.resultSetProviderStack = null;
		}
	}

	public List<IResultSetProvider> getResultSetProviderStack()
	{
		return resultSetProviderStack;
	}

	public void setResultSetProviderStack(List<IResultSetProvider> resultSetProviderStack)
	{
		this.resultSetProviderStack = resultSetProviderStack;
	}

	@Override
	public boolean moveNext()
	{
		IResultSet resultSet = this.resultSet;
		if (resultSet != null)
		{
			if (resultSet.moveNext())
			{
				return true;
			}
			resultSet.dispose();
			this.resultSet = null;
		}
		List<IResultSetProvider> resultSetProviderStack = this.resultSetProviderStack;
		if (resultSetProviderStack != null)
		{
			int providerIndex = resultSetProviderStack.size() - 1;
			IResultSetProvider resultSetProvider = resultSetProviderStack.get(providerIndex);
			resultSetProviderStack.remove(providerIndex);
			if (providerIndex == 0)
			{
				this.resultSetProviderStack = null;
			}
			this.resultSet = resultSetProvider.getResultSet();
			return moveNext();
		}
		return false;
	}

	@Override
	public Object[] getCurrent()
	{
		if (this.resultSet != null)
		{
			return this.resultSet.getCurrent();
		}
		return null;
	}
}
