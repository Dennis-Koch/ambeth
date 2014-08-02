package de.osthus.ambeth.cache;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import de.osthus.ambeth.cache.model.ILoadContainer;
import de.osthus.ambeth.cache.model.IObjRelation;
import de.osthus.ambeth.cache.model.IObjRelationResult;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.IdentityLinkedMap;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.ioc.annotation.Autowired;
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
import de.osthus.ambeth.util.IDisposable;
import de.osthus.ambeth.util.ParamChecker;

public class CacheRetrieverRegistry implements ICacheRetriever, ICacheRetrieverExtendable, ICacheServiceByNameExtendable
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final ClassExtendableContainer<ICacheRetriever> typeToCacheRetrieverMap = new ClassExtendableContainer<ICacheRetriever>("cacheRetriever",
			"entityType");

	protected final MapExtendableContainer<String, ICacheService> nameToCacheServiceMap = new MapExtendableContainer<String, ICacheService>("cacheService",
			"serviceName");

	@Autowired(optional = true)
	protected ICacheRetriever defaultCacheRetriever;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

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

		List<ILoadContainer> result = new ArrayList<ILoadContainer>(orisToLoad.size());

		ILinkedMap<Class<?>, IList<IObjRef>> sortedObjRefs = bucketSortObjRefs(orisToLoad);
		ILinkedMap<ICacheRetriever, IList<IObjRef>> assignedObjRefs = assignObjRefsToCacheRetriever(sortedObjRefs);

		getData(assignedObjRefs, result, new GetDataDelegate<IObjRef, ILoadContainer>()
		{
			@Override
			public List<ILoadContainer> invoke(ICacheRetriever cacheRetriever, List<IObjRef> objRefsForService)
			{
				return cacheRetriever.getEntities(objRefsForService);
			}
		});
		return result;
	}

	@Override
	public List<IObjRelationResult> getRelations(List<IObjRelation> objRelations)
	{
		ParamChecker.assertParamNotNull(objRelations, "objRelations");

		List<IObjRelationResult> result = new ArrayList<IObjRelationResult>(objRelations.size());

		ILinkedMap<Class<?>, IList<IObjRelation>> sortedObjRelations = bucketSortObjRels(objRelations);
		ILinkedMap<ICacheRetriever, IList<IObjRelation>> assignedObjRelations = assignObjRelsToCacheRetriever(sortedObjRelations);
		getData(assignedObjRelations, result, new GetDataDelegate<IObjRelation, IObjRelationResult>()
		{
			@Override
			public List<IObjRelationResult> invoke(ICacheRetriever cacheRetriever, List<IObjRelation> objRelationsForService)
			{
				return cacheRetriever.getRelations(objRelationsForService);
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

	protected <V extends IObjRef> ILinkedMap<Class<?>, IList<V>> bucketSortObjRefs(List<V> orisToLoad)
	{
		LinkedHashMap<Class<?>, IList<V>> sortedIObjRefs = new LinkedHashMap<Class<?>, IList<V>>();

		for (int i = orisToLoad.size(); i-- > 0;)
		{
			Class<?> type = orisToLoad.get(i).getRealType();
			if (!sortedIObjRefs.containsKey(type))
			{
				sortedIObjRefs.put(type, new ArrayList<V>());
			}
			sortedIObjRefs.get(type).add(orisToLoad.get(i));
		}
		return sortedIObjRefs;
	}

	protected ILinkedMap<Class<?>, IList<IObjRelation>> bucketSortObjRels(List<IObjRelation> orisToLoad)
	{
		LinkedHashMap<Class<?>, IList<IObjRelation>> sortedIObjRefs = new LinkedHashMap<Class<?>, IList<IObjRelation>>();

		for (int i = orisToLoad.size(); i-- > 0;)
		{
			IObjRelation orelToLoad = orisToLoad.get(i);
			Class<?> typeOfContainerBO = orelToLoad.getRealType();
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(typeOfContainerBO);
			Member relationMember = metaData.getMemberByName(orelToLoad.getMemberName());

			Class<?> type = relationMember.getElementType();
			IList<IObjRelation> objRefs = sortedIObjRefs.get(type);
			if (objRefs == null)
			{
				objRefs = new ArrayList<IObjRelation>();
				sortedIObjRefs.put(type, objRefs);
			}
			objRefs.add(orelToLoad);
		}
		return sortedIObjRefs;
	}

	protected <V> ILinkedMap<ICacheRetriever, IList<IObjRef>> assignObjRefsToCacheRetriever(IMap<Class<?>, IList<IObjRef>> sortedIObjRefs)
	{
		IdentityLinkedMap<ICacheRetriever, IList<IObjRef>> serviceToAssignedObjRefsDict = new IdentityLinkedMap<ICacheRetriever, IList<IObjRef>>();

		for (Entry<Class<?>, IList<IObjRef>> entry : sortedIObjRefs)
		{
			Class<?> type = entry.getKey();
			IList<IObjRef> objRefs = entry.getValue();
			ICacheRetriever cacheRetriever = getRetrieverForType(type);
			IList<IObjRef> assignedObjRefs = serviceToAssignedObjRefsDict.get(cacheRetriever);
			if (assignedObjRefs == null)
			{
				assignedObjRefs = new ArrayList<IObjRef>();
				serviceToAssignedObjRefsDict.put(cacheRetriever, assignedObjRefs);
			}
			assignedObjRefs.addAll(objRefs);
		}
		return serviceToAssignedObjRefsDict;
	}

	protected <V> ILinkedMap<ICacheRetriever, IList<IObjRelation>> assignObjRelsToCacheRetriever(IMap<Class<?>, IList<IObjRelation>> sortedIObjRefs)
	{
		IdentityLinkedMap<ICacheRetriever, IList<IObjRelation>> serviceToAssignedObjRefsDict = new IdentityLinkedMap<ICacheRetriever, IList<IObjRelation>>();

		for (Entry<Class<?>, IList<IObjRelation>> entry : sortedIObjRefs)
		{
			Class<?> type = entry.getKey();
			IList<IObjRelation> objRefs = entry.getValue();
			ICacheRetriever cacheRetriever = getRetrieverForType(type);
			IList<IObjRelation> assignedObjRefs = serviceToAssignedObjRefsDict.get(cacheRetriever);
			if (assignedObjRefs == null)
			{
				assignedObjRefs = new ArrayList<IObjRelation>();
				serviceToAssignedObjRefsDict.put(cacheRetriever, assignedObjRefs);
			}
			assignedObjRefs.addAll(objRefs);
		}
		return serviceToAssignedObjRefsDict;
	}

	protected <V, R> void getData(ILinkedMap<ICacheRetriever, IList<V>> assignedArguments, List<R> result, GetDataDelegate<V, R> getDataDelegate)
	{
		// Serialize GetEntities() requests
		Iterator<Entry<ICacheRetriever, IList<V>>> iter = assignedArguments.iterator();

		while (iter.hasNext())
		{
			Entry<ICacheRetriever, IList<V>> entry = iter.next();
			ICacheRetriever cacheRetriever = entry.getKey();
			IList<V> paramList = entry.getValue();
			iter.remove();

			List<R> partResult = getDataDelegate.invoke(cacheRetriever, paramList);
			for (int a = 0, size = partResult.size(); a < size; a++)
			{
				R partItem = partResult.get(a);
				result.add(partItem);
			}
			if (partResult instanceof IDisposable)
			{
				((IDisposable) partResult).dispose();
			}
		}
	}
}
