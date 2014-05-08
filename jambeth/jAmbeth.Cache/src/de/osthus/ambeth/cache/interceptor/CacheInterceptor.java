package de.osthus.ambeth.cache.interceptor;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.sf.cglib.proxy.MethodProxy;
import de.osthus.ambeth.annotation.AnnotationCache;
import de.osthus.ambeth.cache.CacheDirective;
import de.osthus.ambeth.cache.Cached;
import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.cache.IServiceResultProcessor;
import de.osthus.ambeth.cache.IServiceResultProcessorRegistry;
import de.osthus.ambeth.cache.model.IServiceResult;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.interceptor.MergeInterceptor;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.service.ICacheService;
import de.osthus.ambeth.service.SyncToAsyncUtil;
import de.osthus.ambeth.threading.SensitiveThreadLocal;
import de.osthus.ambeth.transfer.ServiceDescription;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;
import de.osthus.ambeth.typeinfo.TypeInfoItemUtil;
import de.osthus.ambeth.util.EqualsUtil;
import de.osthus.ambeth.util.ImmutableTypeSet;
import de.osthus.ambeth.util.ListUtil;
import de.osthus.ambeth.util.ParamChecker;

public class CacheInterceptor extends MergeInterceptor
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	public static final ThreadLocal<Boolean> pauseCache = new SensitiveThreadLocal<Boolean>();

	protected final AnnotationCache<Cached> cachedAnnotationCache = new AnnotationCache<Cached>(Cached.class)
	{
		@Override
		protected boolean annotationEquals(Cached left, Cached right)
		{
			return EqualsUtil.equals(left.type(), right.type()) && EqualsUtil.equals(left.alternateIdName(), right.alternateIdName());
		}
	};

	protected ICacheService cacheService;

	protected ICache cache;

	protected IServiceResultProcessorRegistry serviceResultProcessorRegistry;

	@Override
	public void afterPropertiesSet()
	{
		super.afterPropertiesSet();
		ParamChecker.assertNotNull(cacheService, "CacheService");
		ParamChecker.assertNotNull(cache, "Cache");
		ParamChecker.assertNotNull(serviceResultProcessorRegistry, "ServiceResultProcessorRegistry");
	}

	public void setCacheService(ICacheService cacheService)
	{
		this.cacheService = cacheService;
	}

	public void setCache(ICache cache)
	{
		this.cache = cache;
	}

	public void setServiceResultProcessorRegistry(IServiceResultProcessorRegistry serviceResultProcessorRegistry)
	{
		this.serviceResultProcessorRegistry = serviceResultProcessorRegistry;
	}

	@Override
	protected Object interceptLoad(Object obj, Method method, Object[] args, MethodProxy proxy, Boolean isAsyncBegin) throws Throwable
	{
		ServiceDescription serviceDescription;
		IServiceResult serviceResult;

		Cached cached = cachedAnnotationCache.getAnnotation(method);
		if (cached == null && Boolean.TRUE.equals(pauseCache.get()))
		{
			return super.interceptLoad(obj, method, args, proxy, isAsyncBegin);
		}

		Class<?> returnType = method.getReturnType();
		if (ImmutableTypeSet.isImmutableType(returnType))
		{
			// No possible result which might been read by cache
			return super.interceptLoad(obj, method, args, proxy, isAsyncBegin);
		}
		if (cached == null)
		{
			serviceDescription = SyncToAsyncUtil.createServiceDescription(serviceName, method, args);
			serviceResult = cacheService.getORIsForServiceRequest(serviceDescription);
			return createResultObject(serviceResult, returnType, args);
		}

		if (args.length != 1)
		{
			throw new IllegalArgumentException("This annotation is only allowed on methods with exactly 1 argument. Please check your "
					+ Cached.class.toString() + " annotation on method " + method.toString());
		}
		Class<?> entityType = cached.type();
		if (entityType == null || void.class.equals(entityType))
		{
			entityType = TypeInfoItemUtil.getElementTypeUsingReflection(returnType, method.getGenericReturnType());
		}
		if (entityType == null || void.class.equals(entityType))
		{
			throw new IllegalArgumentException("Please specify a valid returnType for the " + Cached.class.getSimpleName() + " annotation on method "
					+ method.toString());
		}
		IEntityMetaData metaData = getSpecifiedMetaData(method, Cached.class, entityType);
		ITypeInfoItem member = getSpecifiedMember(method, Cached.class, metaData, cached.alternateIdName());

		byte idIndex;
		try
		{
			idIndex = metaData.getIdIndexByMemberName(member.getName());
		}
		catch (RuntimeException e)
		{
			throw new IllegalArgumentException(
					"Member "
							+ entityType.getName()
							+ "."
							+ cached.alternateIdName()
							+ " is not configured as an alternate ID member. There must be a single-column unique constraint on the respective table column. Please check your "
							+ Cached.class.toString() + " annotation on method " + method.toString(), e);
		}
		ArrayList<IObjRef> orisToGet = new ArrayList<IObjRef>();
		fillOrisToGet(orisToGet, args, entityType, idIndex);
		return createResultObject(orisToGet, returnType);
	}

	protected void fillOrisToGet(List<IObjRef> orisToGet, Object[] args, Class<?> entityType, byte idIndex)
	{
		Object argument = args[0];
		if (argument instanceof List)
		{
			List<?> list = (List<?>) argument;
			for (int a = 0, size = list.size(); a < size; a++)
			{
				Object id = list.get(a);
				ObjRef objRef = new ObjRef(entityType, idIndex, id, null);
				orisToGet.add(objRef);
			}
		}
		else if (argument instanceof Collection)
		{
			Iterator<?> iter = ((Collection<?>) argument).iterator();
			while (iter.hasNext())
			{
				Object id = iter.next();
				ObjRef objRef = new ObjRef(entityType, idIndex, id, null);
				orisToGet.add(objRef);
			}
		}

		else if (argument.getClass().isArray())
		{
			Object[] array = (Object[]) argument;
			for (int a = 0, size = array.length; a < size; a++)
			{
				Object id = array[a];
				ObjRef objRef = new ObjRef(entityType, idIndex, id, null);
				orisToGet.add(objRef);
			}
		}
		else
		{
			ObjRef objRef = new ObjRef(entityType, idIndex, argument, null);
			orisToGet.add(objRef);
		}
	}

	protected Object createResultObject(IServiceResult serviceResult, Class<?> expectedType, Object[] originalArgs)
	{
		List<IObjRef> objRefs = serviceResult.getObjRefs();
		IList<Object> syncObjects = cache.getObjects(objRefs, Collections.<CacheDirective> emptySet());
		return postProcessCacheResult(syncObjects, expectedType, serviceResult, originalArgs);
	}

	protected Object createResultObject(List<IObjRef> objRefs, Class<?> expectedType)
	{
		IList<Object> syncObjects = cache.getObjects(objRefs, Collections.<CacheDirective> emptySet());
		return postProcessCacheResult(syncObjects, expectedType, null, null);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Object postProcessCacheResult(IList<Object> cacheResult, Class<?> expectedType, IServiceResult serviceResult, Object[] originalArgs)
	{
		int cacheResultSize = cacheResult.size();
		if (Collection.class.isAssignableFrom(expectedType))
		{
			Collection targetCollection = ListUtil.createCollectionOfType(expectedType, cacheResultSize);

			for (int a = 0; a < cacheResultSize; a++)
			{
				targetCollection.add(cacheResult.get(a));
			}
			return targetCollection;
		}
		else if (expectedType.isArray())
		{
			Object[] array = (Object[]) Array.newInstance(expectedType.getComponentType(), cacheResultSize);
			for (int a = 0; a < cacheResultSize; a++)
			{
				array[a] = cacheResult.get(a);
			}
			return array;
		}
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(expectedType, true);
		if (metaData != null)
		{
			// It is a simple entity which can be returned directly
			if (cacheResultSize == 0)
			{
				return null;
			}
			else if (cacheResultSize == 1)
			{
				return cacheResult.get(0);
			}
		}
		Object additionalInformation = serviceResult != null ? serviceResult.getAdditionalInformation() : null;
		if (additionalInformation == null)
		{
			throw new IllegalStateException("Can not convert list of " + cacheResultSize + " results from cache to type " + expectedType.getName());
		}
		IServiceResultProcessor serviceResultProcessor = serviceResultProcessorRegistry.getServiceResultProcessor(expectedType);
		return serviceResultProcessor.processServiceResult(additionalInformation, cacheResult, expectedType, originalArgs);
	}

	@Override
	protected void buildObjRefs(Class<?> entityType, byte idIndex, Class<?> idType, Object ids, List<IObjRef> objRefs)
	{
		if (ids == null)
		{
			return;
		}
		List<Object> idsList = ListUtil.anyToList(ids);

		for (int a = idsList.size(); a-- > 0;)
		{
			Object id = idsList.get(a);
			Object convertedId = conversionHelper.convertValueToType(idType, id);

			objRefs.add(new ObjRef(entityType, idIndex, convertedId, null));
		}

		IList<?> objects = cache.getObjects(objRefs, CacheDirective.returnMisses());
		for (int a = objects.size(); a-- > 0;)
		{
			Object obj = objects.get(a);
			IObjRef objRef = objRefs.get(a);
			if (obj == null)
			{
				throw new IllegalStateException("Could not retrieve object " + objRef);
			}
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(obj.getClass());
			Object version = metaData.getVersionMember().getValue(obj, false);
			objRef.setVersion(version);
		}
	}
}
