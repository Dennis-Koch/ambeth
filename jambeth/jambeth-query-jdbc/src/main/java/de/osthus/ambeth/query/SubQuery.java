package de.osthus.ambeth.query;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.query.sql.ITableAliasProvider;
import de.osthus.ambeth.query.sql.SqlJoinOperator;
import de.osthus.ambeth.query.sql.SqlSubselectOperand;
import de.osthus.ambeth.util.IDisposable;

public class SubQuery<T> implements ISubQuery<T>, IDisposable
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
