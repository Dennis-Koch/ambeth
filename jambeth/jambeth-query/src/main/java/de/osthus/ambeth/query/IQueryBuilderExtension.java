package de.osthus.ambeth.query;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;

public interface IQueryBuilderExtension
{
	IBeanConfiguration applyOnWhereClause(IBeanContextFactory queryBeanContextFactory, IQueryBuilderIntern<?> queryBuilder, IOperand whereClause,
			IList<ISqlJoin> joinClauses, QueryType queryType);

	void applyOnQuery(IMap<Object, Object> nameToValueMap, IList<Object> parameters, IList<String> additionalSelectColumnList);
}
