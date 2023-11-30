package com.koch.ambeth.query;

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.util.collections.IList;

import java.util.Map;

public interface IQueryBuilderExtension {
    IOperand applyOnWhereClause(IServiceContext queryBeanContext, IQueryBuilderIntern<?> queryBuilder, IOperand whereClause, IList<ISqlJoin> joinClauses, QueryType queryType);

    void applyOnQuery(Map<Object, Object> nameToValueMap, IList<Object> parameters, IList<String> additionalSelectColumnList);
}
