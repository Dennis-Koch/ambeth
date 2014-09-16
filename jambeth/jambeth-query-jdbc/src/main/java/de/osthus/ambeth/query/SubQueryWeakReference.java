package de.osthus.ambeth.query;

import java.util.List;
import java.util.Map;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.query.sql.ITableAliasProvider;
import de.osthus.ambeth.query.sql.SqlJoinOperator;
import de.osthus.ambeth.query.sql.SqlSubselectOperand;
import de.osthus.ambeth.util.IDisposable;

public class SubQueryWeakReference<T> implements ISubQuery<T>, IDisposable
{
	protected final IList<ISqlJoin> joinOperands = new ArrayList<ISqlJoin>();

	protected final IList<SqlSubselectOperand> subQueries = new ArrayList<SqlSubselectOperand>();

	protected ISubQuery<T> query;

	public SubQueryWeakReference(ISubQuery<T> query)
	{
		this.query = query;
	}

	@Override
	protected void finalize() throws Throwable
	{
		if (query != null && query instanceof IDisposable)
		{
			((IDisposable) query).dispose();
			query = null;
		}
		joinOperands.clear();
		subQueries.clear();
	}

	@Override
	public void dispose()
	{
		if (query != null && query instanceof IDisposable)
		{
			((IDisposable) query).dispose();
			query = null;
		}
		joinOperands.clear();
		subQueries.clear();
	}

	public void setJoinOperands(IList<ISqlJoin> joinOperands)
	{
		this.joinOperands.addAll(joinOperands);
	}

	public void setSubQueries(IList<SqlSubselectOperand> subQueries)
	{
		this.subQueries.addAll(subQueries);
	}

	@Override
	public Class<?> getEntityType()
	{
		return query.getEntityType();
	}

	@Override
	public String getMainTableAlias()
	{
		return query.getMainTableAlias();
	}

	@Override
	public void setMainTableAlias(String alias)
	{
		query.setMainTableAlias(alias);
	}

	@Override
	public String[] getSqlParts(Map<Object, Object> nameToValueMap, List<Object> parameters, List<String> additionalSelectColumnList)
	{
		return query.getSqlParts(nameToValueMap, parameters, additionalSelectColumnList);
	}

	public void reAlias(ITableAliasProvider tableAliasProvider)
	{
		query.setMainTableAlias(tableAliasProvider.getNextSubQueryAlias());

		for (int i = 0, size = joinOperands.size(); i < size; i++)
		{
			ISqlJoin join = joinOperands.get(i);
			if (join instanceof SqlJoinOperator)
			{
				SqlJoinOperator sqlJoin = (SqlJoinOperator) join;
				sqlJoin.setTableAlias(tableAliasProvider.getNextJoinAlias());
			}
		}
		for (int i = 0, size = subQueries.size(); i < size; i++)
		{
			SqlSubselectOperand subselectOperand = subQueries.get(i);
			ISubQuery<?> subQuery = subselectOperand.getSubQuery();
			if (subQuery instanceof SubQueryWeakReference)
			{
				SubQueryWeakReference<?> subReference = (SubQueryWeakReference<?>) subQuery;
				subReference.reAlias(tableAliasProvider);
			}
		}
	}
}
