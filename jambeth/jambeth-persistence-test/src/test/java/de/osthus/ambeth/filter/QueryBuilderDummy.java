package de.osthus.ambeth.filter;

import java.util.Collections;

import javax.persistence.criteria.JoinType;

import de.osthus.ambeth.query.IOperand;
import de.osthus.ambeth.query.IOperator;
import de.osthus.ambeth.query.IQuery;
import de.osthus.ambeth.query.IQueryBuilder;
import de.osthus.ambeth.query.ISqlJoin;
import de.osthus.ambeth.query.ISubQuery;
import de.osthus.ambeth.query.OrderByType;
import de.osthus.ambeth.util.IParamHolder;

public class QueryBuilderDummy<T> implements IQueryBuilder<T>
{
	@Override
	public void dispose()
	{
	}

	@Override
	public Class<?> getEntityType()
	{
		return null;
	}

	@Override
	public IOperator and(IOperand leftOperand, IOperand rightOperand)
	{
		return new OperandDummy("and", leftOperand, rightOperand);
	}

	@Override
	public IOperator and(IOperand... operands)
	{
		return new OperandDummy("and", operands);
	}

	@Override
	public IOperator or(IOperand leftOperand, IOperand rightOperand)
	{
		return new OperandDummy("or", leftOperand, rightOperand);
	}

	@Override
	public IOperator or(IOperand... operands)
	{
		return new OperandDummy("or", operands);
	}

	@Override
	public IOperand timeUnitMultipliedInterval(IOperand timeUnit, IOperand multiplicatedInterval)
	{
		return null;
	}

	@Override
	public IOperator trueOperator()
	{
		return null;
	}

	@Override
	public IOperator falseOperator()
	{
		return null;
	}

	@Override
	public IOperand property(String propertyName)
	{
		return null;
	}

	@Override
	public IOperand property(String propertyName, JoinType joinType)
	{
		return null;
	}

	@Override
	public IOperand property(String propertyName, JoinType joinType, IParamHolder<Class<?>> fieldType)
	{
		return null;
	}

	@Override
	public IOperand column(String columnName)
	{
		return null;
	}

	@Override
	public IOperand column(String columnName, ISqlJoin joinClause)
	{
		return null;
	}

	@Override
	public IOperator contains(IOperand leftOperand, IOperand rightOperand)
	{
		return new OperandDummy("contains", leftOperand, rightOperand);
	}

	@Override
	public IOperator contains(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive)
	{
		return new OperandDummy("contains", Collections.singletonMap("caseSensitive", caseSensitive), leftOperand, rightOperand);
	}

	@Override
	public IOperator endsWith(IOperand leftOperand, IOperand rightOperand)
	{
		return new OperandDummy("endsWith", leftOperand, rightOperand);
	}

	@Override
	public IOperator endsWith(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive)
	{
		return new OperandDummy("endsWith", Collections.singletonMap("caseSensitive", caseSensitive), leftOperand, rightOperand);
	}

	@Override
	public IOperator fulltext(IOperand queryOperand)
	{
		return null;
	}

	@Override
	public IOperator fulltext(Class<?> entityType, IOperand queryOperand)
	{
		return null;
	}

	@Override
	public IOperand interval(IOperand lowerBoundary, IOperand upperBoundary)
	{
		return null;
	}

	@Override
	public IOperator isContainedIn(IOperand leftOperand, IOperand rightOperand)
	{
		return null;
	}

	@Override
	public IOperator isContainedIn(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive)
	{
		return null;
	}

	@Override
	public IOperator isIn(IOperand leftOperand, IOperand rightOperand)
	{
		return null;
	}

	@Override
	public IOperator isIn(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive)
	{
		return null;
	}

	@Override
	public IOperator isEqualTo(IOperand leftOperand, IOperand rightOperand)
	{
		return null;
	}

	@Override
	public IOperator isEqualTo(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive)
	{
		return new OperandDummy("isEqualTo", Collections.singletonMap("caseSensitive", caseSensitive), rightOperand, leftOperand);
	}

	@Override
	public IOperator isGreaterThan(IOperand leftOperand, IOperand rightOperand)
	{
		return null;
	}

	@Override
	public IOperator isGreaterThanOrEqualTo(IOperand leftOperand, IOperand rightOperand)
	{
		return null;
	}

	@Override
	public IOperator isLessThan(IOperand leftOperand, IOperand rightOperand)
	{
		return null;
	}

	@Override
	public IOperator isLessThanOrEqualTo(IOperand leftOperand, IOperand rightOperand)
	{
		return null;
	}

	@Override
	public IOperator isNotContainedIn(IOperand leftOperand, IOperand rightOperand)
	{
		return null;
	}

	@Override
	public IOperator isNotContainedIn(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive)
	{
		return null;
	}

	@Override
	public IOperator isNotIn(IOperand leftOperand, IOperand rightOperand)
	{
		return null;
	}

	@Override
	public IOperator isNotIn(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive)
	{
		return null;
	}

	@Override
	public IOperator isNotEqualTo(IOperand leftOperand, IOperand rightOperand)
	{
		return null;
	}

	@Override
	public IOperator isNotEqualTo(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive)
	{
		return null;
	}

	@Override
	public IOperator notContains(IOperand leftOperand, IOperand rightOperand)
	{
		return null;
	}

	@Override
	public IOperator notContains(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive)
	{
		return null;
	}

	@Override
	public IOperator notLike(IOperand leftOperand, IOperand rightOperand)
	{
		return null;
	}

	@Override
	public IOperator notLike(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive)
	{
		return null;
	}

	@Override
	public IOperator isNull(IOperand operand)
	{
		return null;
	}

	@Override
	public IOperator isNotNull(IOperand operand)
	{
		return null;
	}

	@Override
	public IOperator like(IOperand leftOperand, IOperand rightOperand)
	{
		return null;
	}

	@Override
	public IOperator like(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive)
	{
		return null;
	}

	@Override
	public IOperand regexpLike(IOperand sourceString, IOperand pattern)
	{
		return null;
	}

	@Override
	public IOperand regexpLike(IOperand sourceString, IOperand pattern, IOperand matchParameter)
	{
		return null;
	}

	@Override
	public IQueryBuilder<T> limit(IOperand operand)
	{
		return null;
	}

	@Override
	public IOperator startsWith(IOperand leftOperand, IOperand rightOperand)
	{
		return null;
	}

	@Override
	public IOperator startsWith(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive)
	{
		return null;
	}

	@Override
	public IOperand value(Object value)
	{
		return new OperandDummy("value", Collections.singletonMap("value", value));
	}

	@Override
	public IOperand valueName(String paramName)
	{
		return null;
	}

	@Override
	public IOperand all()
	{
		return null;
	}

	@Override
	public IOperand function(String functionName, IOperand... operands)
	{
		return null;
	}

	@Override
	public IQueryBuilder<T> groupBy(IOperand... operand)
	{
		return null;
	}

	@Override
	public IQueryBuilder<T> orderBy(IOperand operand, OrderByType orderByType)
	{
		return null;
	}

	@Override
	public IOperand overlaps(IOperand leftOperand, IOperand rightOperand)
	{
		return null;
	}

	@Override
	public int selectColumn(String columnName)
	{
		return 0;
	}

	@Override
	public int selectColumn(String columnName, ISqlJoin join)
	{
		return 0;
	}

	@Override
	public int selectProperty(String propertyName)
	{
		return 0;
	}

	@Override
	public int select(IOperand operand)
	{
		return 0;
	}

	@Override
	public ISqlJoin join(Class<?> entityType, IOperand columnBase, IOperand columnJoined, JoinType joinType)
	{
		return null;
	}

	@Override
	public ISqlJoin join(Class<?> entityType, IOperator clause, JoinType joinType)
	{
		return null;
	}

	@Override
	public ISqlJoin join(Class<?> entityType, IOperand columnBase, IOperand columnJoined)
	{
		return null;
	}

	@Override
	public ISqlJoin join(Class<?> entityType, IOperator clause)
	{
		return null;
	}

	@Override
	public <S> IOperand subQuery(ISubQuery<S> subQuery, IOperand... selectedColumns)
	{
		return null;
	}

	@Override
	public IOperand sum(IOperand... summands)
	{
		return null;
	}

	@Override
	public IQuery<T> build()
	{
		return null;
	}

	@Override
	public IQuery<T> build(IOperand whereClause)
	{
		return null;
	}

	@Override
	public IQuery<T> build(IOperand whereClause, ISqlJoin... joinClauses)
	{
		return null;
	}

	@Override
	public IPagingQuery<T> buildPaging()
	{
		return null;
	}

	@Override
	public IPagingQuery<T> buildPaging(IOperand whereClause)
	{
		return null;
	}

	@Override
	public IPagingQuery<T> buildPaging(IOperand whereClause, ISqlJoin... joinClauses)
	{
		return null;
	}

	@Override
	public ISubQuery<T> buildSubQuery()
	{
		return null;
	}

	@Override
	public ISubQuery<T> buildSubQuery(IOperand whereClause)
	{
		return null;
	}

	@Override
	public ISubQuery<T> buildSubQuery(IOperand whereClause, ISqlJoin... joinClauses)
	{
		return null;
	}
}