package com.koch.ambeth.query;

import com.koch.ambeth.ioc.IServiceContext;

import java.util.List;
import java.util.Map;

public interface IQueryBuilderExtension {
    IOperand applyOnWhereClause(IServiceContext queryBeanContext, IQueryBuilderIntern<?> queryBuilder, IOperand whereClause, List<ISqlJoin> joinClauses, QueryType queryType);

    void applyOnQuery(Map<Object, Object> nameToValueMap, List<Object> parameters, List<String> additionalSelectColumnList);
}
