package de.osthus.ambeth.query;

import javax.persistence.criteria.JoinType;

import de.osthus.ambeth.filter.IPagingQuery;
import de.osthus.ambeth.util.IDisposable;
import de.osthus.ambeth.util.IParamHolder;

public interface IQueryBuilder<T> extends IDisposable
{
	Class<?> getEntityType();

	IOperator and(IOperand leftOperand, IOperand rightOperand);

	IOperator and(IOperand... operands);

	IOperator or(IOperand leftOperand, IOperand rightOperand);

	IOperator or(IOperand... operands);

	IOperator trueOperator();

	IOperator falseOperator();

	/**
	 * The property name may be a simple name (e.g. "Name") or imply a join by using a dot (e.g. "User.Name").
	 * 
	 * @param propertyName
	 *            Name of the (cascaded) property.
	 * @return Operand to use in Statement
	 */
	IOperand property(String propertyName);

	IOperand property(String propertyName, JoinType joinType);

	IOperand property(String propertyName, JoinType joinType, IParamHolder<Class<?>> fieldType);

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

	IOperator contains(IOperand leftOperand, IOperand rightOperand);

	IOperator contains(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive);

	IOperator endsWith(IOperand leftOperand, IOperand rightOperand);

	IOperator endsWith(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive);

	IOperator fulltext(IOperand queryOperand);

	IOperator fulltext(Class<?> entityType, IOperand queryOperand);

	IOperator isContainedIn(IOperand leftOperand, IOperand rightOperand);

	IOperator isContainedIn(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive);

	IOperator isIn(IOperand leftOperand, IOperand rightOperand);

	IOperator isIn(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive);

	IOperator isEqualTo(IOperand leftOperand, IOperand rightOperand);

	IOperator isEqualTo(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive);

	IOperator isGreaterThan(IOperand leftOperand, IOperand rightOperand);

	IOperator isGreaterThanOrEqualTo(IOperand leftOperand, IOperand rightOperand);

	IOperator isLessThan(IOperand leftOperand, IOperand rightOperand);

	IOperator isLessThanOrEqualTo(IOperand leftOperand, IOperand rightOperand);

	IOperator isNotContainedIn(IOperand leftOperand, IOperand rightOperand);

	IOperator isNotContainedIn(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive);

	IOperator isNotIn(IOperand leftOperand, IOperand rightOperand);

	IOperator isNotIn(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive);

	IOperator isNotEqualTo(IOperand leftOperand, IOperand rightOperand);

	IOperator isNotEqualTo(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive);

	IOperator notContains(IOperand leftOperand, IOperand rightOperand);

	IOperator notContains(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive);

	IOperator notLike(IOperand leftOperand, IOperand rightOperand);

	IOperator notLike(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive);

	IOperator isNull(IOperand operand);

	IOperator isNotNull(IOperand operand);

	IOperator like(IOperand leftOperand, IOperand rightOperand);

	IOperator like(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive);

	IOperand regexpLike(IOperand sourceString, IOperand pattern);

	IOperand regexpLike(IOperand sourceString, IOperand pattern, IOperand matchParameter);

	IQueryBuilder<T> limit(IOperand operand);

	IOperator startsWith(IOperand leftOperand, IOperand rightOperand);

	IOperator startsWith(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive);

	IOperand value(Object value);

	IOperand valueName(String paramName);

	IOperand all();

	IOperand function(String functionName, IOperand... operands);

	IQueryBuilder<T> groupBy(IOperand... operand);

	IQueryBuilder<T> orderBy(IOperand operand, OrderByType orderByType);

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
