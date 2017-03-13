package com.koch.ambeth.query.jdbc;

import com.koch.ambeth.query.ISqlJoin;
import com.koch.ambeth.query.ISubQuery;
import com.koch.ambeth.query.jdbc.sql.ITableAliasProvider;
import com.koch.ambeth.query.jdbc.sql.SqlJoinOperator;
import com.koch.ambeth.query.jdbc.sql.SqlSubselectOperand;
import com.koch.ambeth.util.IDisposable;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;

public class SubQuery<T> implements ISubQuery<T>, ISubQueryIntern, IDisposable
{
	protected final ISqlJoin[] joinOperands;

	protected final SqlSubselectOperand[] subQueries;

	protected final ISubQuery<T> subQuery;

	public SubQuery(ISubQuery<T> subQuery, ISqlJoin[] joinOperands, SqlSubselectOperand[] subQueries)
	{
		this.subQuery = subQuery;
		this.joinOperands = joinOperands;
		this.subQueries = subQueries;
	}

	@Override
	public void dispose()
	{
		subQuery.dispose();
	}

	@Override
	public Class<?> getEntityType()
	{
		return subQuery.getEntityType();
	}

	@Override
	public String getMainTableAlias()
	{
		return subQuery.getMainTableAlias();
	}

	@Override
	public void setMainTableAlias(String alias)
	{
		subQuery.setMainTableAlias(alias);
	}

	@Override
	public String[] getSqlParts(IMap<Object, Object> nameToValueMap, IList<Object> parameters, IList<String> additionalSelectColumnList)
	{
		return subQuery.getSqlParts(nameToValueMap, parameters, additionalSelectColumnList);
	}

	@Override
	public void reAlias(ITableAliasProvider tableAliasProvider)
	{
		subQuery.setMainTableAlias(tableAliasProvider.getNextSubQueryAlias());

		for (ISqlJoin join : joinOperands)
		{
			if (join instanceof SqlJoinOperator)
			{
				SqlJoinOperator sqlJoin = (SqlJoinOperator) join;
				sqlJoin.setTableAlias(tableAliasProvider.getNextJoinAlias());
			}
		}
		for (SqlSubselectOperand subselectOperand : subQueries)
		{
			ISubQuery<?> subQuery = subselectOperand.getSubQuery();
			if (subQuery instanceof SubQuery)
			{
				SubQuery<?> subReference = (SubQuery<?>) subQuery;
				subReference.reAlias(tableAliasProvider);
			}
		}
	}
}
