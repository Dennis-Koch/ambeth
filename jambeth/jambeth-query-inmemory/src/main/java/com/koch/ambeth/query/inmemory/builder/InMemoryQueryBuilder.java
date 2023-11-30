package com.koch.ambeth.query.inmemory.builder;

/*-
 * #%L
 * jambeth-query-inmemory
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.IOperator;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.query.IQueryBuilderAfterLeftOperand;
import com.koch.ambeth.query.ISqlJoin;
import com.koch.ambeth.query.ISubQuery;
import com.koch.ambeth.query.OrderByType;
import com.koch.ambeth.query.filter.IPagingQuery;
import com.koch.ambeth.query.inmemory.bool.AndOperator;
import com.koch.ambeth.query.inmemory.bool.FalseOperator;
import com.koch.ambeth.query.inmemory.bool.OrOperator;
import com.koch.ambeth.query.inmemory.bool.TrueOperator;
import com.koch.ambeth.util.IParamHolder;
import com.koch.ambeth.util.ParamChecker;
import jakarta.persistence.criteria.JoinType;

public class InMemoryQueryBuilder<T> implements IQueryBuilder<T> {
    @Autowired
    protected IServiceContext beanContext;

    @Property
    protected Class<?> entityType;

    @Override
    public void dispose() {
        // Intended blank
    }

    @Override
    public Class<?> getEntityType() {
        return entityType;
    }

    protected IOperator createUnaryOperator(Class<? extends IOperator> operatorType, Object operand, Boolean caseSensitive) {
        ParamChecker.assertParamNotNull(operatorType, "operatorType");
        ParamChecker.assertParamNotNull(operand, "operand");
        var operatorBC = beanContext.registerBean(operatorType).propertyValue("Operand", operand);
        if (caseSensitive != null) {
            operatorBC.propertyValue("CaseSensitive", caseSensitive);
        }
        return operatorBC.finish();
    }

    protected IOperator createBinaryOperator(Class<? extends IOperator> operatorType, IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive) {
        ParamChecker.assertParamNotNull(operatorType, "operatorType");
        ParamChecker.assertParamNotNull(leftOperand, "leftOperand");
        ParamChecker.assertParamNotNull(rightOperand, "rightOperand");
        var operatorBC = beanContext.registerBean(operatorType).propertyValue("LeftOperand", leftOperand).propertyValue("RightOperand", rightOperand);
        if (caseSensitive != null) {
            operatorBC.propertyValue("CaseSensitive", caseSensitive);
        }
        return operatorBC.finish();
    }

    protected IOperator createManyPlaceOperator(Class<? extends IOperator> operatorType, IOperand... operands) {
        ParamChecker.assertParamNotNull(operatorType, "operatorType");
        ParamChecker.assertParamNotNull(operands, "operands");
        var operatorBC = beanContext.registerBean(operatorType).propertyValue("Operands", operands);
        return operatorBC.finish();
    }

    @Override
    public IOperator and(IOperand leftOperand, IOperand rightOperand) {
        return createManyPlaceOperator(AndOperator.class, leftOperand, rightOperand);
    }

    @Override
    public IOperator and(IOperand... operands) {
        return createManyPlaceOperator(AndOperator.class, operands);
    }

    @Override
    public IQueryBuilderAfterLeftOperand let(Object leftOperand) {
        throw new IllegalStateException("Not yet supported");
    }

    @Override
    public IOperator or(IOperand leftOperand, IOperand rightOperand) {
        return createManyPlaceOperator(OrOperator.class, leftOperand, rightOperand);
    }

    @Override
    public IOperator or(IOperand... operands) {
        return createManyPlaceOperator(OrOperator.class, operands);
    }

    @Override
    public IOperand timeUnitMultipliedInterval(IOperand timeUnit, IOperand multiplicatedInterval) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IOperator trueOperator() {
        return beanContext.registerBean(TrueOperator.class).finish();
    }

    @Override
    public IOperator falseOperator() {
        return beanContext.registerBean(FalseOperator.class).finish();
    }

    @Override
    public IOperand property(String propertyName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IOperand property(String propertyName, JoinType joinType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IOperand property(String propertyName, JoinType joinType, IParamHolder<Class<?>> fieldType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IOperand regexpLike(IOperand sourceString, IOperand pattern) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IOperand regexpLike(IOperand sourceString, IOperand pattern, IOperand matchParameter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IOperand column(String columnName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IOperand column(String columnName, ISqlJoin joinClause) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IOperand difference(IOperand... operands) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IOperator fulltext(IOperand queryOperand) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IOperator fulltext(Class<?> entityType, IOperand queryOperand) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IQueryBuilder<T> limit(IOperand operand) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IOperand value(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IOperand valueName(String paramName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IOperand all() {
        return trueOperator();
    }

    @Override
    public IOperand function(String functionName, IOperand... parameters) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IQueryBuilder<T> groupBy(IOperand... operand) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IQueryBuilder<T> orderBy(IOperand column, OrderByType orderByType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int selectColumn(String columnName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int selectColumn(String columnName, ISqlJoin join) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int selectProperty(String propertyName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int select(IOperand operand) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ISqlJoin join(Class<?> entityType, IOperand columnBase, IOperand columnJoined, JoinType joinType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ISqlJoin join(Class<?> entityType, IOperator clause, JoinType joinType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ISqlJoin join(Class<?> entityType, IOperand columnBase, IOperand columnJoined) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ISqlJoin join(Class<?> entityType, IOperator clause) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S> IOperand subQuery(ISubQuery<S> subQuery, IOperand... selectedColumns) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IOperand sum(IOperand... summands) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IQuery<T> build() {
        return build(all());
    }

    @Override
    public IQuery<T> build(IOperand whereClause) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IQuery<T> build(IOperand whereClause, ISqlJoin... joinClauses) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IPagingQuery<T> buildPaging() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IPagingQuery<T> buildPaging(IOperand whereClause) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IPagingQuery<T> buildPaging(IOperand whereClause, ISqlJoin... joinClauses) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ISubQuery<T> buildSubQuery() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ISubQuery<T> buildSubQuery(IOperand whereClause) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ISubQuery<T> buildSubQuery(IOperand whereClause, ISqlJoin... joinClauses) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T plan() {
        throw new UnsupportedOperationException();
    }

    @Override
    public IOperand property(Object propertyProxy) {
        throw new UnsupportedOperationException();
    }
}
