package com.koch.ambeth.query.squery;

import java.util.ArrayList;
import java.util.List;

import com.koch.ambeth.filter.IPagingRequest;
import com.koch.ambeth.filter.IPagingResponse;
import com.koch.ambeth.filter.ISortDescriptor;
import com.koch.ambeth.filter.SortDirection;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.query.OrderByType;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.collections.IList;

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

	public Object createQueryBuilder(IQueryBuilderFactory qbf, IConversionHelper conversionHelper, Object[] params, Class<?> expectedReturnType)
	{
		IQueryBuilder<T> queryBuilder = qbf.create(entityType);
		IOperand where;
		where = buildOperand(queryBuilder, params);

		// do orderBy

		for (ISortDescriptor isort : collectSorts(params))
		{
			OrderByType direction = isort.getSortDirection() == SortDirection.DESCENDING ? OrderByType.DESC : OrderByType.ASC;
			queryBuilder.orderBy(queryBuilder.property(isort.getMember()), direction);
		}
		// do query
		if (IPagingResponse.class.isAssignableFrom(expectedReturnType))
		{
			IPagingRequest pagingRequest = getPageRequest(params);
			return queryBuilder.buildPaging(where).retrieve(pagingRequest);
		}
		else if (List.class.isAssignableFrom(expectedReturnType))
		{
			IPagingRequest pagingRequest = getPageRequest(params);
			if (pagingRequest != null)
			{
				return queryBuilder.buildPaging(where).retrieve(pagingRequest).getResult();
			}

			IList<T> list = queryBuilder.build(where).retrieve();
			if (IList.class.isAssignableFrom(expectedReturnType))
			{
				return list;
			}
			else
			{
				return new ArrayList<T>(list);
			}
		}
		else if (expectedReturnType.isAssignableFrom(this.entityType))
		{
			if (queryStr.startsWith("findAll"))
			{
				return queryBuilder.build(where).retrieve();
			}
			return queryBuilder.build(where).retrieveSingle();
		}
		else if (queryStr.startsWith("countBy"))
		{
			if (Number.class.isAssignableFrom(expectedReturnType) || expectedReturnType.isPrimitive())
			{
				return conversionHelper.convertValueToType(expectedReturnType, queryBuilder.build(where).count());
			}
			else if (expectedReturnType == Integer.class || expectedReturnType == int.class)
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
	private IOperand buildOperand(IQueryBuilder<T> queryBuilder, Object[] params)
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
					throw new IllegalArgumentException("not enough arguments");
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
