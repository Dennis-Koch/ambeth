package com.koch.ambeth.query;

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;

public interface IQueryBuilderExtension {
	IOperand applyOnWhereClause(IServiceContext queryBeanContext,
			IQueryBuilderIntern<?> queryBuilder, IOperand whereClause, IList<ISqlJoin> joinClauses,
			QueryType queryType);

	void applyOnQuery(IMap<Object, Object> nameToValueMap, IList<Object> parameters,
			IList<String> additionalSelectColumnList);
}
