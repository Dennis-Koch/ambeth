package de.osthus.ambeth.query;

import javax.persistence.criteria.JoinType;

import de.osthus.ambeth.ioc.factory.IBeanContextFactory;

public interface IQueryBuilderIntern<T> extends IQueryBuilder<T>
{
	@Override
	IOperand column(String columnName);

	@Override
	IOperand column(String columnName, ISqlJoin joinClause);

	ISqlJoin joinIntern(String tableName, IOperand columnBase, IOperand columnJoined, JoinType joinType, IBeanContextFactory childContextFactory);
}
