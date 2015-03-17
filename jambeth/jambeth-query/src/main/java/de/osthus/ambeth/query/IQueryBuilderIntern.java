package de.osthus.ambeth.query;

import javax.persistence.criteria.JoinType;

import de.osthus.ambeth.ioc.factory.IBeanContextFactory;

public interface IQueryBuilderIntern<T> extends IQueryBuilder<T>
{
	IOperand column(String columnName, ISqlJoin joinClause, boolean checkFieldExistence);

	ISqlJoin joinIntern(String tableName, IOperand columnBase, IOperand columnJoined, JoinType joinType, IBeanContextFactory childContextFactory);
}
