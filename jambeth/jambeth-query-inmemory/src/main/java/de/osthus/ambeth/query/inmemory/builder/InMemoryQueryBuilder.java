package de.osthus.ambeth.query.inmemory.builder;

import javax.persistence.criteria.JoinType;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.filter.IPagingQuery;
import de.osthus.ambeth.ioc.IBeanRuntime;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.query.IOperand;
import de.osthus.ambeth.query.IOperator;
import de.osthus.ambeth.query.IQuery;
import de.osthus.ambeth.query.IQueryBuilder;
import de.osthus.ambeth.query.ISqlJoin;
import de.osthus.ambeth.query.ISubQuery;
import de.osthus.ambeth.query.OrderByType;
import de.osthus.ambeth.query.inmemory.bool.AndOperator;
import de.osthus.ambeth.query.inmemory.bool.FalseOperator;
import de.osthus.ambeth.query.inmemory.bool.IsNotNullOperator;
import de.osthus.ambeth.query.inmemory.bool.IsNullOperator;
import de.osthus.ambeth.query.inmemory.bool.OrOperator;
import de.osthus.ambeth.query.inmemory.bool.TrueOperator;
import de.osthus.ambeth.query.inmemory.ordinal.IsGreaterThanOperator;
import de.osthus.ambeth.query.inmemory.ordinal.IsGreaterThanOrEqualToOperator;
import de.osthus.ambeth.query.inmemory.ordinal.IsLessThanOperator;
import de.osthus.ambeth.query.inmemory.ordinal.IsLessThanOrEqualToOperator;
import de.osthus.ambeth.query.inmemory.text.EndsWithOperator;
import de.osthus.ambeth.query.inmemory.text.IsEqualToOperator;
import de.osthus.ambeth.query.inmemory.text.IsNotEqualToOperator;
import de.osthus.ambeth.query.inmemory.text.StartsWithOperator;
import de.osthus.ambeth.util.IParamHolder;
import de.osthus.ambeth.util.ParamChecker;

public class InMemoryQueryBuilder<T> implements IQueryBuilder<T>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IServiceContext beanContext;

	@Property
	protected Class<?> entityType;

	@Override
	public void dispose()
	{
		// Intended blank
	}

	@Override
	public Class<?> getEntityType()
	{
		return entityType;
	}

	protected IOperator createUnaryOperator(Class<? extends IOperator> operatorType, Object operand, Boolean caseSensitive)
	{
		ParamChecker.assertParamNotNull(operatorType, "operatorType");
		ParamChecker.assertParamNotNull(operand, "operand");
		try
		{
			IBeanRuntime<? extends IOperator> operatorBC = beanContext.registerBean(operatorType).propertyValue("Operand", operand);
			if (caseSensitive != null)
			{
				operatorBC.propertyValue("CaseSensitive", caseSensitive);
			}
			return operatorBC.finish();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected IOperator createBinaryOperator(Class<? extends IOperator> operatorType, Object leftOperand, Object rightOperand, Boolean caseSensitive)
	{
		ParamChecker.assertParamNotNull(operatorType, "operatorType");
		ParamChecker.assertParamNotNull(leftOperand, "leftOperand");
		ParamChecker.assertParamNotNull(rightOperand, "rightOperand");
		try
		{
			IBeanRuntime<? extends IOperator> operatorBC = beanContext.registerBean(operatorType).propertyValue("LeftOperand", leftOperand)
					.propertyValue("RightOperand", rightOperand);
			if (caseSensitive != null)
			{
				operatorBC.propertyValue("CaseSensitive", caseSensitive);
			}
			return operatorBC.finish();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected IOperator createManyPlaceOperator(Class<? extends IOperator> operatorType, IOperand... operands)
	{
		ParamChecker.assertParamNotNull(operatorType, "operatorType");
		ParamChecker.assertParamNotNull(operands, "operands");
		try
		{
			IBeanRuntime<? extends IOperator> operatorBC = beanContext.registerBean(operatorType).propertyValue("Operands", operands);
			return operatorBC.finish();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public IOperator and(IOperand leftOperand, IOperand rightOperand)
	{
		return createManyPlaceOperator(AndOperator.class, leftOperand, rightOperand);
	}

	@Override
	public IOperator and(IOperand... operands)
	{
		return createManyPlaceOperator(AndOperator.class, operands);
	}

	@Override
	public IOperator or(IOperand leftOperand, IOperand rightOperand)
	{
		return createManyPlaceOperator(OrOperator.class, leftOperand, rightOperand);
	}

	@Override
	public IOperator or(IOperand... operands)
	{
		return createManyPlaceOperator(OrOperator.class, operands);
	}

	@Override
	public IOperator trueOperator()
	{
		return beanContext.registerBean(TrueOperator.class).finish();
	}

	@Override
	public IOperator falseOperator()
	{
		return beanContext.registerBean(FalseOperator.class).finish();
	}

	@Override
	public IOperand property(String propertyName)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IOperand property(String propertyName, JoinType joinType)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IOperand property(String propertyName, JoinType joinType, IParamHolder<Class<?>> fieldType)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IOperand column(String columnName)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IOperand column(String columnName, ISqlJoin joinClause)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IOperator contains(IOperand leftOperand, IOperand rightOperand)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IOperator contains(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IOperator endsWith(IOperand leftOperand, IOperand rightOperand)
	{
		return createBinaryOperator(EndsWithOperator.class, leftOperand, rightOperand, null);
	}

	@Override
	public IOperator endsWith(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive)
	{
		return createBinaryOperator(EndsWithOperator.class, leftOperand, rightOperand, caseSensitive);
	}

	@Override
	public IOperator fulltext(IOperand queryOperand)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IOperator fulltext(Class<?> entityType, IOperand queryOperand)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IOperator isContainedIn(IOperand leftOperand, IOperand rightOperand)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IOperator isContainedIn(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IOperator isIn(IOperand leftOperand, IOperand rightOperand)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IOperator isIn(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IOperator isEqualTo(IOperand leftOperand, IOperand rightOperand)
	{
		return createBinaryOperator(IsEqualToOperator.class, leftOperand, rightOperand, null);
	}

	@Override
	public IOperator isEqualTo(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive)
	{
		return createBinaryOperator(IsEqualToOperator.class, leftOperand, rightOperand, caseSensitive);
	}

	@Override
	public IOperator isGreaterThan(IOperand leftOperand, IOperand rightOperand)
	{
		return createBinaryOperator(IsGreaterThanOperator.class, leftOperand, rightOperand, null);
	}

	@Override
	public IOperator isGreaterThanOrEqualTo(IOperand leftOperand, IOperand rightOperand)
	{
		return createBinaryOperator(IsGreaterThanOrEqualToOperator.class, leftOperand, rightOperand, null);
	}

	@Override
	public IOperator isLessThan(IOperand leftOperand, IOperand rightOperand)
	{
		return createBinaryOperator(IsLessThanOperator.class, leftOperand, rightOperand, null);
	}

	@Override
	public IOperator isLessThanOrEqualTo(IOperand leftOperand, IOperand rightOperand)
	{
		return createBinaryOperator(IsLessThanOrEqualToOperator.class, leftOperand, rightOperand, null);
	}

	@Override
	public IOperator isNotContainedIn(IOperand leftOperand, IOperand rightOperand)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IOperator isNotContainedIn(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IOperator isNotIn(IOperand leftOperand, IOperand rightOperand)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IOperator isNotIn(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IOperator isNotEqualTo(IOperand leftOperand, IOperand rightOperand)
	{
		return createBinaryOperator(IsNotEqualToOperator.class, leftOperand, rightOperand, null);
	}

	@Override
	public IOperator isNotEqualTo(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive)
	{
		return createBinaryOperator(IsNotEqualToOperator.class, leftOperand, rightOperand, caseSensitive);
	}

	@Override
	public IOperator notContains(IOperand leftOperand, IOperand rightOperand)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IOperator notContains(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IOperator notLike(IOperand leftOperand, IOperand rightOperand)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IOperator notLike(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IOperator isNull(IOperand operand)
	{
		return createUnaryOperator(IsNullOperator.class, operand, null);
	}

	@Override
	public IOperator isNotNull(IOperand operand)
	{
		return createUnaryOperator(IsNotNullOperator.class, operand, null);
	}

	@Override
	public IOperator like(IOperand leftOperand, IOperand rightOperand)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IOperator like(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IOperand regexpLike(IOperand sourceString, IOperand pattern)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IOperand regexpLike(IOperand sourceString, IOperand pattern, IOperand matchParameter)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IQueryBuilder<T> limit(IOperand operand)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IOperator startsWith(IOperand leftOperand, IOperand rightOperand)
	{
		return createBinaryOperator(StartsWithOperator.class, leftOperand, rightOperand, null);
	}

	@Override
	public IOperator startsWith(IOperand leftOperand, IOperand rightOperand, Boolean caseSensitive)
	{
		return createBinaryOperator(StartsWithOperator.class, leftOperand, rightOperand, caseSensitive);
	}

	@Override
	public IOperand value(Object value)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IOperand valueName(String paramName)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IOperand all()
	{
		return trueOperator();
	}

	@Override
	public IOperand function(String functionName, IOperand... parameters)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IQueryBuilder<T> orderBy(IOperand column, OrderByType orderByType)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public int selectColumn(String columnName)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public int selectColumn(String columnName, ISqlJoin join)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public int selectProperty(String propertyName)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public ISqlJoin join(Class<?> entityType, IOperand columnBase, IOperand columnJoined, JoinType joinType)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public ISqlJoin join(Class<?> entityType, IOperator clause, JoinType joinType)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public ISqlJoin join(Class<?> entityType, IOperand columnBase, IOperand columnJoined)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public ISqlJoin join(Class<?> entityType, IOperator clause)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <S> IOperand subQuery(ISubQuery<S> subQuery, IOperand... selectedColumns)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IQuery<T> build()
	{
		return build(all());
	}

	@Override
	public IQuery<T> build(IOperand whereClause)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IQuery<T> build(IOperand whereClause, ISqlJoin... joinClauses)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IPagingQuery<T> buildPaging()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IPagingQuery<T> buildPaging(IOperand whereClause)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IPagingQuery<T> buildPaging(IOperand whereClause, ISqlJoin... joinClauses)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public ISubQuery<T> buildSubQuery()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public ISubQuery<T> buildSubQuery(IOperand whereClause)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public ISubQuery<T> buildSubQuery(IOperand whereClause, ISqlJoin... joinClauses)
	{
		throw new UnsupportedOperationException();
	}
}
