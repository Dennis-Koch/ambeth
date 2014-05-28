package de.osthus.ambeth.sql;

import java.util.List;

import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.util.IDisposable;
import de.osthus.ambeth.util.ParamChecker;

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
