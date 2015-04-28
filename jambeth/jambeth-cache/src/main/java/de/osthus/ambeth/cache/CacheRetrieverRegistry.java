package de.osthus.ambeth.cache;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;

import de.osthus.ambeth.cache.model.ILoadContainer;
import de.osthus.ambeth.cache.model.IObjRelation;
import de.osthus.ambeth.cache.model.IObjRelationResult;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IdentityLinkedMap;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.exception.ExtendableException;
import de.osthus.ambeth.ioc.extendable.ClassExtendableContainer;
import de.osthus.ambeth.ioc.extendable.MapExtendableContainer;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.service.ICacheRetriever;
import de.osthus.ambeth.service.ICacheRetrieverExtendable;
import de.osthus.ambeth.service.ICacheService;
import de.osthus.ambeth.service.ICacheServiceByNameExtendable;
import de.osthus.ambeth.service.IPropertyCacheRetriever;
import de.osthus.ambeth.service.IPropertyCacheRetrieverExtendable;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerParamDelegate;
import de.osthus.ambeth.util.IAggregrateResultHandler;
import de.osthus.ambeth.util.IDisposable;
import de.osthus.ambeth.util.IMultithreadingHelper;
import de.osthus.ambeth.util.ParamChecker;

public class CacheRetrieverRegistry implements ICacheRetriever, ICacheRetrieverExtendable, IPropertyCacheRetrieverExtendable, ICacheServiceByNameExtendable
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final ClassExtendableContainer<ICacheRetriever> typeToCacheRetrieverMap = new ClassExtendableContainer<ICacheRetriever>("cacheRetriever",
			"entityType");

	protected final ClassExtendableContainer<HashMap<String, IPropertyCacheRetriever>> typeToPropertyCacheRetrieverMap = new ClassExtendableContainer<HashMap<String, IPropertyCacheRetriever>>(
			"cacheRetriever", "entityType");

	protected final MapExtendableContainer<String, ICacheService> nameToCacheServiceMap = new MapExtendableContainer<String, ICacheService>("cacheService",
			"serviceName");

	@Autowired(optional = true)
	protected ICacheRetriever defaultCacheRetriever;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IMultithreadingHelper multithreadingHelper;

	@Override
	public void registerCacheRetriever(ICacheRetriever cacheRetriever, Class<?> handledType)
	{
		typeToCacheRetrieverMap.register(cacheRetriever, handledType);
	}

	@Override
	public void unregisterCacheRetriever(ICacheRetriever cacheRetriever, Class<?> handledType)
	{
		typeToCacheRetrieverMap.unregister(cacheRetriever, handledType);
	}

	@Override
	public void registerPropertyCacheRetriever(IPropertyCacheRetriever propertyCacheRetriever, Class<?> handledType, String propertyName)
	{
		Lock writeLock = typeToPropertyCacheRetrieverMap.getWriteLock();
		writeLock.lock();
		try
		{
			HashMap<String, IPropertyCacheRetriever> map = typeToPropertyCacheRetrieverMap.getExtension(handledType);
			if (map == null)
			{
				map = new HashMap<String, IPropertyCacheRetriever>();
				typeToPropertyCacheRetrieverMap.register(map, handledType);
			}
			if (!map.putIfNotExists(propertyName, propertyCacheRetriever))
			{
				throw new ExtendableException("Key '" + handledType.getName() + "." + propertyName + "' already added");
			}
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public void unregisterPropertyCacheRetriever(IPropertyCacheRetriever propertyCacheRetriever, Class<?> handledType, String propertyName)
	{
		Lock writeLock = typeToPropertyCacheRetrieverMap.getWriteLock();
		writeLock.lock();
		try
		{
			HashMap<String, IPropertyCacheRetriever> map = typeToPropertyCacheRetrieverMap.getExtension(handledType);
			if (map == null || !map.removeIfValue(propertyName, propertyCacheRetriever))
			{
				throw new ExtendableException("Provided extension is not registered at key '" + handledType + "." + propertyName + "'. Extension: "
						+ propertyCacheRetriever);
			}
			if (map.size() == 0)
			{
				typeToPropertyCacheRetrieverMap.unregister(map, handledType);
			}
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public void registerCacheService(ICacheService cacheService, String serviceName)
	{
		nameToCacheServiceMap.register(cacheService, serviceName);
	}

	@Override
	public void unregisterCacheService(ICacheService cacheService, String serviceName)
	{
		nameToCacheServiceMap.unregister(cacheService, serviceName);
	}

	@Override
	public List<ILoadContainer> getEntities(List<IObjRef> orisToLoad)
	{
		ParamChecker.assertParamNotNull(orisToLoad, "orisToLoad");

		final ArrayList<ILoadContainer> result = new ArrayList<ILoadContainer>(orisToLoad.size());

		ILinkedMap<ICacheRetriever, IList<IObjRef>> assignedObjRefs = bucketSortObjRefs(orisToLoad);

		multithreadingHelper.invokeAndWait(assignedObjRefs,
				new IResultingBackgroundWorkerParamDelegate<List<ILoadContainer>, Entry<ICacheRetriever, IList<IObjRef>>>()
				{
					@Override
					public List<ILoadContainer> invoke(Entry<ICacheRetriever, IList<IObjRef>> item) throws Throwable
					{
						return item.getKey().getEntities(item.getValue());
					}
				}, new IAggregrateResultHandler<List<ILoadContainer>, Entry<ICacheRetriever, IList<IObjRef>>>()
				{
					@Override
					public void aggregateResult(List<ILoadContainer> resultOfFork, Entry<ICacheRetriever, IList<IObjRef>> itemOfFork)
					{
						for (int a = 0, size = resultOfFork.size(); a < size; a++)
						{
							ILoadContainer partItem = resultOfFork.get(a);
							result.add(partItem);
						}
						if (resultOfFork instanceof IDisposable)
						{
							((IDisposable) resultOfFork).dispose();
						}
					}
				});
		return result;
	}

	@Override
	public List<IObjRelationResult> getRelations(List<IObjRelation> objRelations)
	{
		ParamChecker.assertParamNotNull(objRelations, "objRelations");

		ILinkedMap<IPropertyCacheRetriever, IList<IObjRelation>> assignedObjRelations = bucketSortObjRels(objRelations);

		final ArrayList<IObjRelationResult> result = new ArrayList<IObjRelationResult>(objRelations.size());
		multithreadingHelper.invokeAndWait(assignedObjRelations,
				new IResultingBackgroundWorkerParamDelegate<List<IObjRelationResult>, Entry<IPropertyCacheRetriever, IList<IObjRelation>>>()
				{
					@Override
					public List<IObjRelationResult> invoke(Entry<IPropertyCacheRetriever, IList<IObjRelation>> item) throws Throwable
					{
						return item.getKey().getRelations(item.getValue());
					}
				}, new IAggregrateResultHandler<List<IObjRelationResult>, Entry<IPropertyCacheRetriever, IList<IObjRelation>>>()
				{
					@Override
					public void aggregateResult(List<IObjRelationResult> resultOfFork, Entry<IPropertyCacheRetriever, IList<IObjRelation>> itemOfFork)
					{
						for (int a = 0, size = resultOfFork.size(); a < size; a++)
						{
							IObjRelationResult partItem = resultOfFork.get(a);
							result.add(partItem);
						}
						if (resultOfFork instanceof IDisposable)
						{
							((IDisposable) resultOfFork).dispose();
						}
					}
				});
		return result;
	}

	protected ICacheRetriever getRetrieverForType(Class<?> type)
	{
		if (type == null)
		{
			return null;
		}

		ICacheRetriever cacheRetriever = typeToCacheRetrieverMap.getExtension(type);
		if (cacheRetriever == null)
		{
			if (defaultCacheRetriever != null && defaultCacheRetriever != this)
			{
				cacheRetriever = defaultCacheRetriever;
			}
			else
			{
				throw new IllegalStateException("No cache retriever found to handle entity type '" + type.getName() + "'");
			}
		}

		return cacheRetriever;
	}

	protected IPropertyCacheRetriever getPropertyRetrieverForType(Class<?> type, String propertyName)
	{
		if (type == null)
		{
			return null;
		}
		HashMap<String, IPropertyCacheRetriever> map = typeToPropertyCacheRetrieverMap.getExtension(type);
		if (map == null)
		{
			return null;
		}
		return map.get(propertyName);
	}

	protected ILinkedMap<ICacheRetriever, IList<IObjRef>> bucketSortObjRefs(List<IObjRef> orisToLoad)
	{
		IdentityLinkedMap<ICacheRetriever, IList<IObjRef>> serviceToAssignedObjRefsDict = new IdentityLinkedMap<ICacheRetriever, IList<IObjRef>>();

		for (int i = orisToLoad.size(); i-- > 0;)
		{
			IObjRef objRef = orisToLoad.get(i);
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(objRef.getRealType());

			ICacheRetriever cacheRetriever = getRetrieverForType(metaData.getEntityType());
			IList<IObjRef> assignedObjRefs = serviceToAssignedObjRefsDict.get(cacheRetriever);
			if (assignedObjRefs == null)
			{
				assignedObjRefs = new ArrayList<IObjRef>();
				serviceToAssignedObjRefsDict.put(cacheRetriever, assignedObjRefs);
			}
			assignedObjRefs.add(objRef);
		}
		return serviceToAssignedObjRefsDict;
	}

	protected ILinkedMap<IPropertyCacheRetriever, IList<IObjRelation>> bucketSortObjRels(List<IObjRelation> orisToLoad)
	{
		IdentityLinkedMap<IPropertyCacheRetriever, IList<IObjRelation>> serviceToAssignedObjRefsDict = new IdentityLinkedMap<IPropertyCacheRetriever, IList<IObjRelation>>();

		for (int i = orisToLoad.size(); i-- > 0;)
		{
			IObjRelation orelToLoad = orisToLoad.get(i);
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(orelToLoad.getRealType());
			Member relationMember = metaData.getMemberByName(orelToLoad.getMemberName());

			// look first for a specific retriever for the requested property of the owning entity type
			IPropertyCacheRetriever cacheRetriever = getPropertyRetrieverForType(metaData.getEntityType(), relationMember.getName());
			if (cacheRetriever == null)
			{
				// fallback to retriever registered for the target entity type
				cacheRetriever = getRetrieverForType(relationMember.getElementType());
			}
			IList<IObjRelation> assignedObjRefs = serviceToAssignedObjRefsDict.get(cacheRetriever);
			if (assignedObjRefs == null)
			{
				assignedObjRefs = new ArrayList<IObjRelation>();
				serviceToAssignedObjRefsDict.put(cacheRetriever, assignedObjRefs);
			}
			assignedObjRefs.add(orelToLoad);
		}
		return serviceToAssignedObjRefsDict;
	}
}
