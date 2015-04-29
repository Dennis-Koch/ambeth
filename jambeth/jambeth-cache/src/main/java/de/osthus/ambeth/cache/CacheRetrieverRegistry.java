package de.osthus.ambeth.cache;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.locks.Lock;

import de.osthus.ambeth.cache.model.ILoadContainer;
import de.osthus.ambeth.cache.model.IObjRelation;
import de.osthus.ambeth.cache.model.IObjRelationResult;
import de.osthus.ambeth.cache.transfer.ObjRelation;
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
import de.osthus.ambeth.merge.IObjRefHelper;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.metadata.PrimitiveMember;
import de.osthus.ambeth.service.ICacheRetriever;
import de.osthus.ambeth.service.ICacheRetrieverExtendable;
import de.osthus.ambeth.service.ICacheService;
import de.osthus.ambeth.service.ICacheServiceByNameExtendable;
import de.osthus.ambeth.service.IPrimitiveRetriever;
import de.osthus.ambeth.service.IPrimitiveRetrieverExtendable;
import de.osthus.ambeth.service.IRelationRetriever;
import de.osthus.ambeth.service.IRelationRetrieverExtendable;
import de.osthus.ambeth.threading.IBackgroundWorkerParamDelegate;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerParamDelegate;
import de.osthus.ambeth.util.IAggregrateResultHandler;
import de.osthus.ambeth.util.IDisposable;
import de.osthus.ambeth.util.IMultithreadingHelper;
import de.osthus.ambeth.util.ParamChecker;

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

		ILinkedMap<Object, IList<IObjRelation>> assignedObjRelations = bucketSortObjRels(objRelations);

		final ArrayList<IObjRelationResult> result = new ArrayList<IObjRelationResult>(objRelations.size());
		multithreadingHelper.invokeAndWait(assignedObjRelations,
				new IResultingBackgroundWorkerParamDelegate<List<IObjRelationResult>, Entry<Object, IList<IObjRelation>>>()
				{
					@Override
					public List<IObjRelationResult> invoke(Entry<Object, IList<IObjRelation>> item) throws Throwable
					{
						Object retriever = item.getKey();
						if (retriever instanceof IRelationRetriever)
						{
							return ((IRelationRetriever) retriever).getRelations(item.getValue());
						}
						return ((ICacheRetriever) retriever).getRelations(item.getValue());
					}
				}, new IAggregrateResultHandler<List<IObjRelationResult>, Entry<Object, IList<IObjRelation>>>()
				{
					@Override
					public void aggregateResult(List<IObjRelationResult> resultOfFork, Entry<Object, IList<IObjRelation>> itemOfFork)
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

	protected ILinkedMap<Object, IList<IObjRelation>> bucketSortObjRels(List<IObjRelation> orisToLoad)
	{
		IdentityLinkedMap<Object, IList<IObjRelation>> retrieverToAssignedObjRelsDict = new IdentityLinkedMap<Object, IList<IObjRelation>>();

		for (int i = orisToLoad.size(); i-- > 0;)
		{
			IObjRelation orelToLoad = orisToLoad.get(i);
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(orelToLoad.getRealType());
			Member relationMember = metaData.getMemberByName(orelToLoad.getMemberName());

			// look first for a specific retriever for the requested property of the owning entity type
			Object relationRetriever = getPropertyRetrieverForType(typeToRelationRetrieverEC, metaData.getEntityType(), relationMember.getName());
			if (relationRetriever == null)
			{
				// fallback to retriever registered for the target entity type
				relationRetriever = getRetrieverForType(relationMember.getElementType());
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
