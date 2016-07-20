package de.osthus.ambeth.query.squery;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.filter.model.IPagingRequest;
import de.osthus.ambeth.filter.model.IPagingResponse;
import de.osthus.ambeth.filter.model.ISortDescriptor;
import de.osthus.ambeth.filter.model.SortDirection;
import de.osthus.ambeth.query.IOperand;
import de.osthus.ambeth.query.IQueryBuilder;
import de.osthus.ambeth.query.IQueryBuilderFactory;
import de.osthus.ambeth.query.OrderByType;

public final class QueryBuilderBean<T>
{
	protected final List<OperationBean> queryBeans;
	protected final Class<T> entityType;
	protected final List<ISortDescriptor> sorts;
	protected final String queryStr;

	public QueryBuilderBean(List<OperationBean> queryBeans, Class<T> entityType, List<ISortDescriptor> sorts, String queryStr)
	{
		this.queryBeans = queryBeans;
		this.entityType = entityType;
		this.sorts = sorts;
		this.queryStr = queryStr;
	}

	public Object createQueryBuilder(IQueryBuilderFactory qbf, Object[] params, Method method)
	{
		IQueryBuilder<T> queryBuilder = qbf.create(entityType);
		IOperand where;
		where = this.buildOperand(queryBuilder, params, method);

		// do orderBy

		for (ISortDescriptor isort : this.collectSorts(params))
		{
			OrderByType direction = isort.getSortDirection() == SortDirection.DESCENDING ? OrderByType.DESC : OrderByType.ASC;
			queryBuilder.orderBy(queryBuilder.property(isort.getMember()), direction);
		}
		// do query
		Class<?> returnType = method.getReturnType();
		if (IPagingResponse.class.isAssignableFrom(returnType))
		{
			IPagingRequest pagingRequest = this.getPageRequest(params);
			return queryBuilder.buildPaging(where).retrieve(pagingRequest);
		}
		else if (List.class.isAssignableFrom(returnType))
		{
			IPagingRequest pagingRequest = this.getPageRequest(params);
			if (pagingRequest != null)
			{
				return queryBuilder.buildPaging(where).retrieve(pagingRequest).getResult();
			}

			IList<T> list = queryBuilder.build(where).retrieve();
			if (IList.class.isAssignableFrom(returnType))
			{
				return list;
			}
			else
			{
				return new ArrayList<T>(list);
			}
		}
		else if (returnType.isAssignableFrom(this.entityType))
		{
			return queryBuilder.build(where).retrieveSingle();
		}
		else if (this.queryStr.startsWith("countBy"))
		{
			if (returnType == Long.class || returnType == long.class)
			{
				return queryBuilder.build(where).count();
			}
			else if (returnType == Integer.class || returnType == int.class)
			{
				return (int) queryBuilder.build(where).count();
			}
			else
			{
				throw new RuntimeException("the " + this.queryStr + " method must return Type is Long or Integer");
			}
		}
		else
		{
			throw new RuntimeException("the " + this.queryStr + " method must return Type is IPagingResponse, List, Long, Integer or entityType");
		}
	}

	private List<ISortDescriptor> collectSorts(Object[] params)
	{
		ISortDescriptor[] sortsFromParams = null;
		for (int i = params.length - 2; i < params.length; i++)
		{
			if (i < 0)
			{
				continue;
			}
			Object param = params[i];
			if (param instanceof ISortDescriptor)
			{
				sortsFromParams = new ISortDescriptor[] { (ISortDescriptor) param };
			}
			else if (param instanceof ISortDescriptor[])
			{
				sortsFromParams = (ISortDescriptor[]) param;
			}
		}

		List<ISortDescriptor> result = null;
		if (sortsFromParams == null)
		{
			result = this.sorts;
		}
		else
		{
			result = new ArrayList<ISortDescriptor>(this.sorts);
			for (ISortDescriptor iSortDescriptor : sortsFromParams)
			{
				result.add(iSortDescriptor);
			}
		}
		return result;
	}

	private IPagingRequest getPageRequest(Object[] params)
	{

		for (int i = params.length - 2; i < params.length; i++)
		{
			if (i < 0)
			{
				continue;
			}
			Object param = params[i];
			if (param instanceof IPagingRequest)
			{
				return (IPagingRequest) param;
			}
		}
		return null;
	}

	/**
	 * @param queryBuilder
	 * @param params
	 * @param method
	 * @return
	 * @throws java.lang.ArrayIndexOutOfBoundsException
	 *             if params have not enough element, then throw this Exception
	 */
	private IOperand buildOperand(IQueryBuilder<T> queryBuilder, Object[] params, Method method)
	{
		if (queryBeans == null || queryBeans.isEmpty())
		{
			return null;
		}

		List<IOperand> andList = new ArrayList<IOperand>();
		List<IOperand> orList = new ArrayList<IOperand>();
		int paramIndex = 0;
		for (OperationBean operationBean : queryBeans)
		{
			Object value = null;
			if (needValueBuildOperand(operationBean))
			{
				if (paramIndex >= params.length)
				{
					throw new IllegalArgumentException("the method [" + method + "] have not enough count of argument");
				}
				value = params[paramIndex++];
			}
			IOperand operand = operationBean.buildOperand(queryBuilder, value);
			if (operand != null)
			{
				andList.add(operand);
			}
			if (operationBean.getRelation() == Relation.OR)
			{
				collectAndOperand(queryBuilder, andList, orList);
				andList.clear();
			}
		}
		collectAndOperand(queryBuilder, andList, orList);

		if (orList.size() == 1)
		{
			return orList.get(0);
		}
		else if (orList.size() > 1)
		{
			IOperand[] orArray = orList.toArray(new IOperand[orList.size()]);
			return queryBuilder.or(orArray);
		}
		else
		{
			return null;
		}
	}

	private boolean needValueBuildOperand(OperationBean operationBean)
	{
		return operationBean.getCondition() != Condition.IS_NULL && operationBean.getCondition() != Condition.IS_NOT_NULL;
	}

	private void collectAndOperand(IQueryBuilder<T> queryBuilder, List<IOperand> andList, List<IOperand> orList)
	{
		if (andList.size() == 1)
		{
			orList.add(andList.get(0));
		}
		else if (andList.size() > 1)
		{
			IOperand[] andArray = andList.toArray(new IOperand[andList.size()]);
			orList.add(queryBuilder.and(andArray));
		}
	}
}
