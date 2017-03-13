package com.koch.ambeth.cache;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;

import com.koch.ambeth.cache.service.ICacheRetriever;
import com.koch.ambeth.cache.service.ICacheRetrieverExtendable;
import com.koch.ambeth.cache.service.ICacheService;
import com.koch.ambeth.cache.service.ICacheServiceByNameExtendable;
import com.koch.ambeth.cache.service.IPrimitiveRetriever;
import com.koch.ambeth.cache.service.IPrimitiveRetrieverExtendable;
import com.koch.ambeth.cache.service.IRelationRetriever;
import com.koch.ambeth.cache.service.IRelationRetrieverExtendable;
import com.koch.ambeth.cache.transfer.ObjRelation;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.exception.ExtendableException;
import com.koch.ambeth.ioc.extendable.ClassExtendableContainer;
import com.koch.ambeth.ioc.extendable.MapExtendableContainer;
import com.koch.ambeth.ioc.util.IAggregrateResultHandler;
import com.koch.ambeth.ioc.util.IMultithreadingHelper;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.IObjRefHelper;
import com.koch.ambeth.service.cache.model.ILoadContainer;
import com.koch.ambeth.service.cache.model.IObjRelation;
import com.koch.ambeth.service.cache.model.IObjRelationResult;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.service.metadata.PrimitiveMember;
import com.koch.ambeth.util.IDisposable;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IdentityLinkedMap;
import com.koch.ambeth.util.threading.IBackgroundWorkerParamDelegate;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerParamDelegate;

public class CacheRetrieverRegistry implements ICacheRetriever, ICacheRetrieverExtendable, IPrimitiveRetrieverExtendable, IRelationRetrieverExtendable,
		ICacheServiceByNameExtendable
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final ClassExtendableContainer<ICacheRetriever> typeToCacheRetrieverMap = new ClassExtendableContainer<ICacheRetriever>("cacheRetriever",
			"entityType");

	protected final ClassExtendableContainer<HashMap<String, IRelationRetriever>> typeToRelationRetrieverEC = new ClassExtendableContainer<HashMap<String, IRelationRetriever>>(
			"relationRetriever", "handledType");

	protected final ClassExtendableContainer<HashMap<String, IPrimitiveRetriever>> typeToPrimitiveRetrieverEC = new ClassExtendableContainer<HashMap<String, IPrimitiveRetriever>>(
			"primitiveRetriever", "handledType");

	protected final MapExtendableContainer<String, ICacheService> nameToCacheServiceEC = new MapExtendableContainer<String, ICacheService>("cacheService",
			"serviceName");

	@Autowired(optional = true)
	protected ICacheRetriever defaultCacheRetriever;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IMultithreadingHelper multithreadingHelper;

	@Autowired
	protected IObjRefHelper objRefHelper;

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
	public void registerRelationRetriever(IRelationRetriever relationRetriever, Class<?> handledType, String propertyName)
	{
		registerPropertyRetriever(typeToRelationRetrieverEC, relationRetriever, handledType, propertyName);
	}

	@Override
	public void unregisterRelationRetriever(IRelationRetriever relationRetriever, Class<?> handledType, String propertyName)
	{
		unregisterPropertyRetriever(typeToRelationRetrieverEC, relationRetriever, handledType, propertyName);
	}

	@Override
	public void registerPrimitiveRetriever(IPrimitiveRetriever primitiveRetriever, Class<?> handledType, String propertyName)
	{
		registerPropertyRetriever(typeToPrimitiveRetrieverEC, primitiveRetriever, handledType, propertyName);
	}

	@Override
	public void unregisterPrimitiveRetriever(IPrimitiveRetriever primitiveRetriever, Class<?> handledType, String propertyName)
	{
		unregisterPropertyRetriever(typeToPrimitiveRetrieverEC, primitiveRetriever, handledType, propertyName);
	}

	protected <E> void registerPropertyRetriever(ClassExtendableContainer<HashMap<String, E>> extendableContainer, E extension, Class<?> handledType,
			String propertyName)
	{
		Lock writeLock = extendableContainer.getWriteLock();
		writeLock.lock();
		try
		{
			HashMap<String, E> map = extendableContainer.getExtension(handledType);
			if (map == null)
			{
				map = new HashMap<String, E>();
				extendableContainer.register(map, handledType);
			}
			if (!map.putIfNotExists(propertyName, extension))
			{
				throw new ExtendableException("Key '" + handledType.getName() + "." + propertyName + "' already added");
			}
		}
		finally
		{
			writeLock.unlock();
		}
	}

	protected <E> void unregisterPropertyRetriever(ClassExtendableContainer<HashMap<String, E>> extendableContainer, E extension, Class<?> handledType,
			String propertyName)
	{
		Lock writeLock = extendableContainer.getWriteLock();
		writeLock.lock();
		try
		{
			HashMap<String, E> map = extendableContainer.getExtension(handledType);
			if (map == null || !map.removeIfValue(propertyName, extension))
			{
				throw new ExtendableException("Provided extension is not registered at key '" + handledType + "." + propertyName + "'. Extension: " + extension);
			}
			if (map.size() == 0)
			{
				extendableContainer.unregister(map, handledType);
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
		nameToCacheServiceEC.register(cacheService, serviceName);
	}

	@Override
	public void unregisterCacheService(ICacheService cacheService, String serviceName)
	{
		nameToCacheServiceEC.unregister(cacheService, serviceName);
	}

	@Override
	public List<ILoadContainer> getEntities(List<IObjRef> orisToLoad)
	{
		ParamChecker.assertParamNotNull(orisToLoad, "orisToLoad");

		IList<ILoadContainer> result = getEntitiesIntern(orisToLoad);

		final IdentityLinkedMap<IObjRelation, IBackgroundWorkerParamDelegate<Object>> objRelToDelegateMap = new IdentityLinkedMap<IObjRelation, IBackgroundWorkerParamDelegate<Object>>();

		ILinkedMap<IPrimitiveRetriever, IList<IObjRelation>> fetchablePrimitives = bucketSortObjRelsForFetchablePrimitives(result, objRelToDelegateMap);

		if (fetchablePrimitives.size() == 0)
		{
			return result;
		}
		multithreadingHelper.invokeAndWait(fetchablePrimitives,
				new IResultingBackgroundWorkerParamDelegate<Object[], Entry<IPrimitiveRetriever, IList<IObjRelation>>>()
				{
					@Override
					public Object[] invoke(Entry<IPrimitiveRetriever, IList<IObjRelation>> item) throws Throwable
					{
						return item.getKey().getPrimitives(item.getValue());
					}
				}, new IAggregrateResultHandler<Object[], Entry<IPrimitiveRetriever, IList<IObjRelation>>>()
				{
					@Override
					public void aggregateResult(Object[] resultOfFork, Entry<IPrimitiveRetriever, IList<IObjRelation>> itemOfFork) throws Throwable
					{
						IList<IObjRelation> objRels = itemOfFork.getValue();

						for (int a = objRels.size(); a-- > 0;)
						{
							IObjRelation objRel = objRels.get(a);
							IBackgroundWorkerParamDelegate<Object> delegate = objRelToDelegateMap.get(objRel);
							delegate.invoke(resultOfFork[a]);
						}
					}
				});

		return result;
	}

	protected IList<ILoadContainer> getEntitiesIntern(List<IObjRef> orisToLoad)
	{
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

		ILinkedMap<IRelationRetriever, IList<IObjRelation>> assignedObjRelations = bucketSortObjRels(objRelations);

		final ArrayList<IObjRelationResult> result = new ArrayList<IObjRelationResult>(objRelations.size());
		multithreadingHelper.invokeAndWait(assignedObjRelations,
				new IResultingBackgroundWorkerParamDelegate<List<IObjRelationResult>, Entry<IRelationRetriever, IList<IObjRelation>>>()
				{
					@Override
					public List<IObjRelationResult> invoke(Entry<IRelationRetriever, IList<IObjRelation>> item) throws Throwable
					{
						IRelationRetriever retriever = item.getKey();
						return retriever.getRelations(item.getValue());
					}
				}, new IAggregrateResultHandler<List<IObjRelationResult>, Entry<IRelationRetriever, IList<IObjRelation>>>()
				{
					@Override
					public void aggregateResult(List<IObjRelationResult> resultOfFork, Entry<IRelationRetriever, IList<IObjRelation>> itemOfFork)
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

	protected <E> E getPropertyRetrieverForType(ClassExtendableContainer<HashMap<String, E>> extendableContainer, Class<?> type, String propertyName)
	{
		if (type == null)
		{
			return null;
		}
		HashMap<String, E> map = extendableContainer.getExtension(type);
		if (map == null)
		{
			return null;
		}
		return map.get(propertyName);
	}

	protected ILinkedMap<ICacheRetriever, IList<IObjRef>> bucketSortObjRefs(List<? extends IObjRef> orisToLoad)
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

	protected ILinkedMap<IRelationRetriever, IList<IObjRelation>> bucketSortObjRels(List<? extends IObjRelation> orisToLoad)
	{
		IdentityLinkedMap<IRelationRetriever, IList<IObjRelation>> retrieverToAssignedObjRelsDict = new IdentityLinkedMap<IRelationRetriever, IList<IObjRelation>>();

		for (int i = orisToLoad.size(); i-- > 0;)
		{
			IObjRelation orelToLoad = orisToLoad.get(i);
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(orelToLoad.getRealType());
			Member relationMember = metaData.getMemberByName(orelToLoad.getMemberName());

			// look first for a specific retriever for the requested property of the owning entity type
			IRelationRetriever relationRetriever = getPropertyRetrieverForType(typeToRelationRetrieverEC, metaData.getEntityType(), relationMember.getName());
			if (relationRetriever == null)
			{
				// fallback to retriever registered for the target entity type
				relationRetriever = getRetrieverForType(orelToLoad.getRealType());
			}
			IList<IObjRelation> assignedObjRefs = retrieverToAssignedObjRelsDict.get(relationRetriever);
			if (assignedObjRefs == null)
			{
				assignedObjRefs = new ArrayList<IObjRelation>();
				retrieverToAssignedObjRelsDict.put(relationRetriever, assignedObjRefs);
			}
			assignedObjRefs.add(orelToLoad);
		}
		return retrieverToAssignedObjRelsDict;
	}

	protected ILinkedMap<IPrimitiveRetriever, IList<IObjRelation>> bucketSortObjRelsForFetchablePrimitives(List<ILoadContainer> loadContainers,
			ILinkedMap<IObjRelation, IBackgroundWorkerParamDelegate<Object>> objRelToDelegateMap)
	{
		IdentityLinkedMap<IPrimitiveRetriever, IList<IObjRelation>> retrieverToAssignedObjRelsDict = new IdentityLinkedMap<IPrimitiveRetriever, IList<IObjRelation>>();

		for (int a = loadContainers.size(); a-- > 0;)
		{
			ILoadContainer loadContainer = loadContainers.get(a);
			IObjRef objRef = loadContainer.getReference();
			Class<?> entityType = objRef.getRealType();
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
			PrimitiveMember[] primitiveMembers = metaData.getPrimitiveMembers();
			final Object[] primitives = loadContainer.getPrimitives();
			for (int b = primitives.length; b-- > 0;)
			{
				Object primitive = primitives[b];
				if (primitive != null)
				{
					continue;
				}
				String memberName = primitiveMembers[b].getName();
				IPrimitiveRetriever primitiveRetriever = getPropertyRetrieverForType(typeToPrimitiveRetrieverEC, entityType, memberName);

				if (primitiveRetriever == null)
				{
					continue;
				}
				IList<IObjRelation> assignedObjRefs = retrieverToAssignedObjRelsDict.get(primitiveRetriever);
				if (assignedObjRefs == null)
				{
					assignedObjRefs = new ArrayList<IObjRelation>();
					retrieverToAssignedObjRelsDict.put(primitiveRetriever, assignedObjRefs);
				}
				IList<IObjRef> objRefs = objRefHelper.entityToAllObjRefs(loadContainer, metaData);
				ObjRelation objRel = new ObjRelation(objRefs.toArray(IObjRef.class), memberName);
				objRel.setRealType(entityType);
				objRel.setVersion(objRef.getVersion());
				assignedObjRefs.add(objRel);

				final int primitiveIndex = b;
				objRelToDelegateMap.put(objRel, new IBackgroundWorkerParamDelegate<Object>()
				{
					@Override
					public void invoke(Object fetchedPrimitive) throws Throwable
					{
						primitives[primitiveIndex] = fetchedPrimitive;
					}
				});
			}
		}
		return retrieverToAssignedObjRelsDict;
	}
}
