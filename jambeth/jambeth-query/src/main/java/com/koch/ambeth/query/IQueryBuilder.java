package com.koch.ambeth.query;

/*-
 * #%L
 * jambeth-query
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

import javax.persistence.criteria.JoinType;

import com.koch.ambeth.query.filter.IPagingQuery;
import com.koch.ambeth.util.IDisposable;
import com.koch.ambeth.util.IParamHolder;

public interface IQueryBuilder<T> extends IDisposable {
	Class<?> getEntityType();

	IOperator and(IOperand leftOperand, IOperand rightOperand);

	IOperator and(IOperand... operands);

	IOperator or(IOperand leftOperand, IOperand rightOperand);

	IOperator or(IOperand... operands);

	IOperand timeUnitMultipliedInterval(IOperand timeUnit, IOperand multiplicatedInterval);

	IOperator trueOperator();

	IOperator falseOperator();

	/**
	 * The property name may be a simple name (e.g. "Name") or imply a join by using a dot (e.g.
	 * "User.Name").
	 *
	 * @param propertyName
	 *          Name of the (cascaded) property.
	 * @return Operand to use in Statement
	 */
	IOperand property(String propertyName);

	IOperand property(String propertyName, JoinType joinType);

	IOperand property(String propertyName, JoinType joinType, IParamHolder<Class<?>> fieldType);

	IOperand property(Object propertyProxy);

	/**
	 * Returns a stub looking like the queried entity type. All relations on this stub can be accessed
	 * (to-one/to-many) in a cascaded manner in order to use this in a declarative manner when
	 * building a queried property. The traversal is internally tracked. For to-many relations there
	 * is always exactly one entity stub in the collection available for valid traversal.<br>
	 * <br>
	 * Example:<br>
	 * <code>
	 * IQueryBuilder&lt;MyType&gt; qb = queryBuilderFactory.create(MyType.class);<br>
	 * IQuery&lt;MyType&gt; query = qb.build(qb.isEqualTo(qb.plan().getMyRelations().get(0).getId(), 2));<br>
	 * </code><br>
	 * Does exactly the same as:<br>
	 * <code>
	 * IQueryBuilder&lt;MyType&gt; qb = queryBuilderFactory.create(MyType.class);<br>
	 * IQuery&lt;MyType&gt; query = qb.build(qb.isEqualTo(qb.property("MyRelations"), qb.value(2)));<br>
	 * </code><br>
	 * The major difference is that the stub traversal is supported via code completion of our chosen
	 * IDE and eagerly compiled to detect typos immediately. The latter one instead could be loaded
	 * e.g. from a configuration file or generic string concatenation with ease.
	 *
	 * @return A stub of the queried entity type
	 */
	T plan();

	/**
	 * Please use property() instead
	 */
	@Deprecated
	IOperand column(String columnName);

	/**
	 * Please use property() instead
	 */
	@Deprecated
	IOperand column(String columnName, ISqlJoin joinClause);

	IOperator contains(Object leftOperand, Object rightOperand);

	IOperator contains(Object leftOperand, Object rightOperand, Boolean caseSensitive);

	IOperand difference(IOperand... diffOperands);

	IOperator endsWith(Object leftOperand, Object rightOperand);

	IOperator endsWith(Object leftOperand, Object rightOperand, Boolean caseSensitive);

	IOperator fulltext(IOperand queryOperand);

	IOperator fulltext(Class<?> entityType, IOperand queryOperand);

	IOperator isContainedIn(Object leftOperand, Object rightOperand);

	IOperator isContainedIn(Object leftOperand, Object rightOperand, Boolean caseSensitive);

	IOperator isIn(Object leftOperand, Object rightOperand);

	IOperator isIn(Object leftOperand, Object rightOperand, Boolean caseSensitive);

	IOperator isEqualTo(Object leftOperand, Object rightOperand);

	IOperator isEqualTo(Object leftOperand, Object rightOperand, Boolean caseSensitive);

	IOperator isGreaterThan(Object leftOperand, Object rightOperand);

	IOperator isGreaterThanOrEqualTo(Object leftOperand, Object rightOperand);

	IOperator isLessThan(Object leftOperand, Object rightOperand);

	IOperator isLessThanOrEqualTo(Object leftOperand, Object rightOperand);

	IOperator isNotContainedIn(Object leftOperand, Object rightOperand);

	IOperator isNotContainedIn(Object leftOperand, Object rightOperand, Boolean caseSensitive);

	IOperator isNotIn(Object leftOperand, Object rightOperand);

	IOperator isNotIn(Object leftOperand, Object rightOperand, Boolean caseSensitive);

	IOperator isNotEqualTo(Object leftOperand, Object rightOperand);

	IOperator isNotEqualTo(Object leftOperand, Object rightOperand, Boolean caseSensitive);

	IOperator notContains(Object leftOperand, Object rightOperand);

	IOperator notContains(Object leftOperand, Object rightOperand, Boolean caseSensitive);

	IOperator notLike(Object leftOperand, Object rightOperand);

	IOperator notLike(Object leftOperand, Object rightOperand, Boolean caseSensitive);

	IOperator isNull(IOperand operand);

	IOperator isNotNull(IOperand operand);

	IOperator like(Object leftOperand, Object rightOperand);

	IOperator like(Object leftOperand, Object rightOperand, Boolean caseSensitive);

	IOperand regexpLike(IOperand sourceString, IOperand pattern);

	IOperand regexpLike(IOperand sourceString, IOperand pattern, IOperand matchParameter);

	IQueryBuilder<T> limit(IOperand operand);

	IOperator startsWith(Object leftOperand, Object rightOperand);

	IOperator startsWith(Object leftOperand, Object rightOperand, Boolean caseSensitive);

	IOperand value(Object value);

	IOperand valueName(String paramName);

	IOperand all();

	IOperand function(String functionName, IOperand... operands);

	IQueryBuilder<T> groupBy(IOperand... operand);

	IOperand interval(IOperand lowerBoundary, IOperand upperBoundary);

	IQueryBuilder<T> orderBy(IOperand operand, OrderByType orderByType);

	IOperand overlaps(Object leftOperand, Object rightOperand);

	/**
	 * Please use selectProperty() instead
	 */
	@Deprecated
	int selectColumn(String columnName);

	/**
	 * Please use selectProperty() instead
	 */
	@Deprecated
	int selectColumn(String columnName, ISqlJoin join);

	int selectProperty(String propertyName);

	int select(IOperand operand);

	ISqlJoin join(Class<?> entityType, IOperand columnBase, IOperand columnJoined, JoinType joinType);

	ISqlJoin join(Class<?> entityType, IOperator clause, JoinType joinType);

	ISqlJoin join(Class<?> entityType, IOperand columnBase, IOperand columnJoined);

	ISqlJoin join(Class<?> entityType, IOperator clause);

	<S> IOperand subQuery(ISubQuery<S> subQuery, IOperand... selectedColumns);

	IOperand sum(IOperand... summands);

	IQuery<T> build();

	IQuery<T> build(IOperand whereClause);

	IQuery<T> build(IOperand whereClause, ISqlJoin... joinClauses);

	IPagingQuery<T> buildPaging();

	IPagingQuery<T> buildPaging(IOperand whereClause);

	IPagingQuery<T> buildPaging(IOperand whereClause, ISqlJoin... joinClauses);

	ISubQuery<T> buildSubQuery();

	ISubQuery<T> buildSubQuery(IOperand whereClause);

	ISubQuery<T> buildSubQuery(IOperand whereClause, ISqlJoin... joinClauses);

}
