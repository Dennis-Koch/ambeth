package de.osthus.ambeth.query.interceptor;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.sf.cglib.proxy.MethodProxy;
import de.osthus.ambeth.annotation.AnnotationCache;
import de.osthus.ambeth.annotation.Find;
import de.osthus.ambeth.annotation.NoProxy;
import de.osthus.ambeth.annotation.QueryResultType;
import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.filter.IFilterToQueryBuilder;
import de.osthus.ambeth.filter.IPagingQuery;
import de.osthus.ambeth.filter.model.FilterDescriptor;
import de.osthus.ambeth.filter.model.IFilterDescriptor;
import de.osthus.ambeth.filter.model.IPagingRequest;
import de.osthus.ambeth.filter.model.IPagingResponse;
import de.osthus.ambeth.filter.model.ISortDescriptor;
import de.osthus.ambeth.filter.model.SortDescriptor;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IObjRefHelper;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.proxy.CascadedInterceptor;

public class QueryInterceptor extends CascadedInterceptor
{
	protected static final AnnotationCache<Find> findCache = new AnnotationCache<Find>(Find.class)
	{
		@Override
		protected boolean annotationEquals(Find left, Find right)
		{
			return left.equals(right);
		}
	};

	protected static final AnnotationCache<NoProxy> noProxyCache = new AnnotationCache<NoProxy>(NoProxy.class)
	{
		@Override
		protected boolean annotationEquals(NoProxy left, NoProxy right)
		{
			return left.equals(right);
		}
	};

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected ICache cache;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IFilterToQueryBuilder filterToQueryBuilder;

	@Autowired
	protected IObjRefHelper oriHelper;

	@Override
	protected Object interceptIntern(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable
	{
		if (noProxyCache.getAnnotation(method) != null)
		{
			return invokeTarget(obj, method, args, proxy);
		}
		String methodName = method.getName().toLowerCase();
		Boolean isAsyncBegin = null;
		if (methodName.startsWith("begin"))
		{
			isAsyncBegin = Boolean.TRUE;
			methodName = methodName.substring(5);
		}
		else if (methodName.startsWith("end"))
		{
			isAsyncBegin = Boolean.FALSE;
			methodName = methodName.substring(3);
		}
		return intercept(obj, method, args, proxy, methodName, isAsyncBegin);
	}

	protected Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy, String methodName, Boolean isAsyncBegin) throws Throwable
	{
		if (findCache.getAnnotation(method) != null || methodName.startsWith("retrieve") || methodName.startsWith("read") || methodName.startsWith("find")
				|| methodName.startsWith("get"))
		{
			if (args.length == 3 && IPagingResponse.class.isAssignableFrom(method.getReturnType()))
			{
				return interceptQuery(obj, method, args, proxy, isAsyncBegin);
			}
			// if (args.length == 1)
			// {
			// return interceptLoad(obj, method, args, proxy, isAsyncBegin);
			// }
		}
		return invokeTarget(obj, method, args, proxy);
	}

	protected Object interceptQuery(Object obj, Method method, Object[] args, MethodProxy proxy, Boolean isAsyncBegin) throws Throwable
	{
		Find findAnnotation = method.getAnnotation(Find.class);
		QueryResultType resultType;
		if (findAnnotation == null)
		{
			resultType = QueryResultType.REFERENCES;
		}
		else
		{
			resultType = findAnnotation.resultType();
		}

		IPagingRequest pagingRequest = (IPagingRequest) args[0];
		IFilterDescriptor<?> filterDescriptor = (FilterDescriptor<?>) args[1];
		ISortDescriptor[] sortDescriptors = (SortDescriptor[]) args[2];

		IPagingQuery<?> pagingQuery = filterToQueryBuilder.buildQuery(filterDescriptor, sortDescriptors);

		IPagingResponse<?> pagingResponse = null;
		if (QueryResultType.ENTITIES == resultType || QueryResultType.BOTH == resultType)
		{
			pagingResponse = pagingQuery.retrieve(pagingRequest);
		}
		if (QueryResultType.REFERENCES == resultType)
		{
			pagingResponse = pagingQuery.retrieveRefs(pagingRequest);
		}
		if (QueryResultType.BOTH == resultType)
		{
			List<?> result = pagingResponse.getResult();
			int size = result.size();
			List<IObjRef> oris = new ArrayList<IObjRef>(size);
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(filterDescriptor.getEntityType());
			for (int i = 0; i < size; i++)
			{
				Object entity = result.get(i);
				IObjRef ori = oriHelper.entityToObjRef(entity, ObjRef.PRIMARY_KEY_INDEX, metaData, true);
				oris.add(ori);
			}
			pagingResponse.setRefResult(oris);
		}
		return pagingResponse;
	}

	protected Object interceptLoad(Object obj, Method method, Object[] args, MethodProxy proxy, Boolean isAsyncBegin)
	{
		Class<?> entityType = method.getReturnType();
		if (entityType.isArray())
		{
			entityType = entityType.getComponentType();
		}
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType, true);
		if (metaData == null)
		{
			Type genericReturnType = method.getGenericReturnType();
			if (!(genericReturnType instanceof ParameterizedType))
			{
				throw new IllegalArgumentException("Cannot identify return type");
			}
			ParameterizedType castedType = (ParameterizedType) genericReturnType;
			Type[] actualTypeArguments = castedType.getActualTypeArguments();
			if (actualTypeArguments.length != 1)
			{
				throw new IllegalArgumentException("Generic return type with more than one generic type");
			}
			entityType = (Class<?>) actualTypeArguments[0];
			metaData = entityMetaDataProvider.getMetaData(entityType, true);
			if (metaData == null)
			{
				throw new IllegalArgumentException("Cannot identify return type");
			}
		}

		Object idsRaw = args[0];
		Class<?> idsClass = idsRaw.getClass();
		if (List.class.isAssignableFrom(idsClass))
		{
			List<?> ids = (List<?>) idsRaw;
			return cache.getObjects(entityType, ids);
		}
		else if (Set.class.isAssignableFrom(idsClass))
		{
			List<?> ids = new ArrayList<Object>((Set<?>) idsRaw);
			return cache.getObjects(entityType, ids);
		}
		else if (idsClass.isArray())
		{
			throw new IllegalArgumentException("Array of IDs not yet supported");
		}
		else
		{
			return cache.getObject(entityType, idsRaw);
		}
	}
}
