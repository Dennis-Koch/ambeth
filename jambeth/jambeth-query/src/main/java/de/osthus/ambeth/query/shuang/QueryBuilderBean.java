package de.osthus.ambeth.query.shuang;

import java.util.ArrayList;
import java.util.List;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.filter.IPagingQuery;
import de.osthus.ambeth.filter.model.IPagingRequest;
import de.osthus.ambeth.filter.model.IPagingResponse;
import de.osthus.ambeth.filter.model.ISortDescriptor;
import de.osthus.ambeth.filter.model.SortDirection;
import de.osthus.ambeth.query.IOperand;
import de.osthus.ambeth.query.IQuery;
import de.osthus.ambeth.query.IQueryBuilder;
import de.osthus.ambeth.query.IQueryBuilderFactory;
import de.osthus.ambeth.query.OrderByType;

public final class QueryBuilderBean<T>
{
	protected final List<OperationBean> queryBeans;
	protected final Class<T> entityType;
	protected final List<ISortDescriptor> sorts;
	protected final String queryStr;
	protected final List<String> paramNames;

	public QueryBuilderBean(List<OperationBean> queryBeans, Class<T> entityType, List<ISortDescriptor> sorts, String queryStr)
	{
		this.queryBeans = queryBeans;
		this.entityType = entityType;
		this.sorts = sorts;
		this.queryStr = queryStr;
		paramNames = extractParamNames();
	}

	public Object createQueryBuilder(IQueryBuilderFactory qbf, Object[] params, Class<?>[] paramType, Class<?> returnType)
	{
		IQueryBuilder<T> queryBuilder = qbf.create(entityType);
		IOperand where = this.buildOperand(queryBuilder);
		// do orderBy
		for (ISortDescriptor sort : sorts)
		{
			OrderByType direction = sort.getSortDirection() == SortDirection.DESCENDING ? OrderByType.DESC : OrderByType.ASC;
			queryBuilder.orderBy(queryBuilder.property(sort.getMember()), direction);
		}
		ISortDescriptor[] isorts = this.getSort(params, paramType);
		for (ISortDescriptor isort : isorts)
		{
			OrderByType direction = isort.getSortDirection() == SortDirection.DESCENDING ? OrderByType.DESC : OrderByType.ASC;
			queryBuilder.orderBy(queryBuilder.property(isort.getMember()), direction);
		}
		// do query
		if (IPagingResponse.class.isAssignableFrom(returnType))
		{
			IPagingRequest pagingRequest = this.getPageRequest(params, paramType);
			return doQueryPage(params, paramType, pagingRequest, queryBuilder, where);
		}
		else if (List.class.isAssignableFrom(returnType))
		{
			IPagingRequest pagingRequest = this.getPageRequest(params, paramType);
			if (pagingRequest != null)
			{
				return doQueryPage(params, paramType, pagingRequest, queryBuilder, where).getResult();
			}

			IList<T> list = this.prepareQuery(params, paramType, queryBuilder, where).retrieve();
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
			return this.prepareQuery(params, paramType, queryBuilder, where).retrieveSingle();
		}
		else if (this.queryStr.startsWith("countBy"))
		{
			if (returnType == Long.class || returnType == long.class)
			{
				return this.prepareQuery(params, paramType, queryBuilder, where).count();
			}
			else if (returnType == Integer.class || returnType == int.class)
			{
				return (int) this.prepareQuery(params, paramType, queryBuilder, where).count();
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

	private IPagingResponse<T> doQueryPage(Object[] params, Class<?>[] paramType, IPagingRequest pagingRequest, IQueryBuilder<T> queryBuilder, IOperand where)
	{
		IPagingQuery<T> pagingQuery = queryBuilder.buildPaging(where);
		for (int i = 0; i < paramNames.size(); i++)
		{
			String name = paramNames.get(i);
			Object value = params[i];
			pagingQuery.param(name, value);
		}
		return pagingQuery.retrieve(pagingRequest);
	}

	private IQuery<T> prepareQuery(Object[] params, Class<?>[] paramType, IQueryBuilder<T> queryBuilder, IOperand where)
	{
		IQuery<T> query = queryBuilder.build(where);
		for (int i = 0; i < paramNames.size(); i++)
		{
			String name = paramNames.get(i);
			Object value = params[i];
			query = query.param(name, value);
		}
		return query;
	}

	private ISortDescriptor[] getSort(Object[] params, Class<?>[] paramType)
	{
		Class<ISortDescriptor> sortClazz = ISortDescriptor.class;
		Class<ISortDescriptor[]> sortArrayClazz = ISortDescriptor[].class;
		ISortDescriptor[] result = null;
		for (int i = paramType.length - 2; i < paramType.length; i++)
		{
			if (i < 0)
			{
				continue;
			}
			Class<?> clazz = paramType[i];
			if (sortClazz.isAssignableFrom(clazz))
			{
				result = new ISortDescriptor[] { (ISortDescriptor) params[i] };
			}
			else if (sortArrayClazz.isAssignableFrom(clazz))
			{
				result = (ISortDescriptor[]) params[i];
			}
		}
		if (result == null)
		{
			result = new ISortDescriptor[0];
		}
		return result;
	}

	private IPagingRequest getPageRequest(Object[] params, Class<?>[] paramType)
	{

		for (int i = paramType.length - 2; i < paramType.length; i++)
		{
			if (i < 0)
			{
				continue;
			}
			if (IPagingRequest.class.isAssignableFrom(paramType[i]))
			{
				return (IPagingRequest) params[i];
			}
		}
		return null;
	}

	@SuppressWarnings("unused")
	private boolean havaSortParam(Class<?>[] paramTypes)
	{
		Class<ISortDescriptor> sortClazz = ISortDescriptor.class;
		Class<ISortDescriptor[]> sortArrayClazz = ISortDescriptor[].class;
		for (Class<?> clazz : paramTypes)
		{
			if (sortClazz.isAssignableFrom(clazz) || sortArrayClazz.isAssignableFrom(clazz))
			{
				return true;
			}
		}
		return false;
	}

	private IOperand buildOperand(IQueryBuilder<T> queryBuilder)
	{
		if (queryBeans == null || queryBeans.isEmpty())
		{
			return null;
		}

		List<IOperand> andList = new ArrayList<IOperand>();
		List<IOperand> orList = new ArrayList<IOperand>();
		for (OperationBean operationBean : queryBeans)
		{
			andList.add(operationBean.getOperand(queryBuilder));
			if (operationBean.getRelation() == Relation.OR)
			{
				if (andList.size() == 1)
				{
					orList.add(andList.get(0));
				}
				else
				{
					IOperand[] andArray = andList.toArray(new IOperand[andList.size()]);
					orList.add(queryBuilder.and(andArray));
				}
				andList = new ArrayList<IOperand>();
			}
		}
		if (andList.size() == 1)
		{
			orList.add(andList.get(0));
		}
		else if (andList.size() > 1)
		{
			IOperand[] andArray = andList.toArray(new IOperand[andList.size()]);
			orList.add(queryBuilder.and(andArray));
		}

		if (orList.size() == 1)
		{
			return orList.get(0);
		}
		else
		{
			IOperand[] orArray = orList.toArray(new IOperand[orList.size()]);
			return queryBuilder.or(orArray);
		}
	}

	private List<String> extractParamNames()
	{
		List<String> result = new ArrayList<String>();
		for (OperationBean ob : this.queryBeans)
		{
			if (ob.getCondition() != Condition.IS_NULL && ob.getCondition() != Condition.IS_NOT_NULL)
			{
				result.add(ob.getNestFieldName());
			}
		}
		return result;
	}
}
