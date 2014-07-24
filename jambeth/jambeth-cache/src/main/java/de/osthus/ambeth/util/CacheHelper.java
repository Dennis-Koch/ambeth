package de.osthus.ambeth.util;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.osthus.ambeth.cache.CacheDirective;
import de.osthus.ambeth.cache.ICacheIntern;
import de.osthus.ambeth.cache.ICacheModification;
import de.osthus.ambeth.cache.ValueHolderState;
import de.osthus.ambeth.cache.model.IObjRelation;
import de.osthus.ambeth.cache.model.IObjRelationResult;
import de.osthus.ambeth.cache.rootcachevalue.RootCacheValue;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.ISet;
import de.osthus.ambeth.collections.IdentityLinkedMap;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.collections.ObservableArrayList;
import de.osthus.ambeth.collections.ObservableHashSet;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IObjRefHelper;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.proxy.IEntityMetaDataHolder;
import de.osthus.ambeth.proxy.IObjRefContainer;
import de.osthus.ambeth.proxy.IValueHolderContainer;
import de.osthus.ambeth.template.ValueHolderContainerTemplate;
import de.osthus.ambeth.typeinfo.IRelationInfoItem;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;
import de.osthus.ambeth.typeinfo.ITypeInfoProvider;

public class CacheHelper implements ICacheHelper, ICachePathHelper, IPrefetchHelper
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	private static final Object[] emptyObjectArray = new Object[0];

	private static final Set<CacheDirective> failEarlyReturnMisses = EnumSet.of(CacheDirective.FailEarly, CacheDirective.ReturnMisses);

	protected final ThreadLocal<ISet<AlreadyHandledItem>> alreadyHandledSetTL = new ThreadLocal<ISet<AlreadyHandledItem>>();

	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected ICacheModification cacheModification;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IObjRefHelper oriHelper;

	@Autowired
	protected ITypeInfoProvider typeInfoProvider;

	@Autowired
	protected ValueHolderContainerTemplate valueHolderContainerTemplate;

	@Override
	public void buildCachePath(Class<?> entityType, String memberToInitialize, List<CachePath> cachePaths)
	{
		String[] path = memberToInitialize.split("\\.");
		CachePath currentCachePath = null;
		Class<?> currentType = entityType;

		for (String pathItem : path)
		{
			if (currentCachePath == null)
			{
				currentCachePath = getOrCreateCachePath(cachePaths, currentType, pathItem);
			}
			else
			{
				if (currentCachePath.children == null)
				{
					currentCachePath.children = new ArrayList<CachePath>();
				}
				currentCachePath = getOrCreateCachePath(currentCachePath.children, currentType, pathItem);
			}
			currentType = currentCachePath.memberType;
		}
	}

	protected IList<CachePath> buildCachePath(Class<?> entityType, List<String> membersToInitialize)
	{
		ArrayList<CachePath> cachePaths = new ArrayList<CachePath>();
		for (int a = membersToInitialize.size(); a-- > 0;)
		{
			String memberName = membersToInitialize.get(a);
			buildCachePath(entityType, memberName, cachePaths);
		}
		return cachePaths;
	}

	protected CachePath getOrCreateCachePath(List<CachePath> cachePaths, Class<?> entityType, String memberName)
	{
		for (int a = cachePaths.size(); a-- > 0;)
		{
			CachePath cachePath = cachePaths.get(a);
			if (memberName.equals(cachePath.memberName))
			{
				return cachePath;
			}
		}
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);

		ITypeInfoItem member = metaData.getMemberByName(memberName);
		if (member == null)
		{
			throw new IllegalArgumentException("Member " + entityType.getName() + "." + memberName + " not found");
		}
		CachePath newCachePath = new CachePath(member.getElementType(), metaData.getIndexByRelationName(memberName), memberName);
		cachePaths.add(newCachePath);
		return newCachePath;
	}

	@Override
	public IPrefetchConfig createPrefetch()
	{
		return beanContext.registerAnonymousBean(PrefetchConfig.class).finish();
	}

	@Override
	public IPrefetchState prefetch(Object objects, IMap<Class<?>, List<String>> typeToMembersToInitialize)
	{
		if (objects == null || typeToMembersToInitialize == null || typeToMembersToInitialize.size() == 0)
		{
			return null;
		}
		HashMap<Class<?>, IList<CachePath>> typeToCachePathsDict = HashMap.create(typeToMembersToInitialize.size());
		for (Entry<Class<?>, List<String>> entry : typeToMembersToInitialize)
		{
			Class<?> entityType = entry.getKey();
			List<String> membersToInitialize = entry.getValue();
			typeToCachePathsDict.put(entityType, buildCachePath(entityType, membersToInitialize));
		}
		return ensureInitializedRelations(objects, typeToCachePathsDict);
	}

	@Override
	public <V extends List<CachePath>> IPrefetchState ensureInitializedRelations(Object objects, IMap<Class<?>, V> typeToMembersToInitialize)
	{
		if (objects == null || typeToMembersToInitialize == null || typeToMembersToInitialize.size() == 0)
		{
			return null;
		}
		return ensureInitializedRelationsIntern(objects, typeToMembersToInitialize);
	}

	@Override
	public IPrefetchState prefetch(Object objects)
	{
		return ensureInitializedRelationsIntern(objects, null);
	}

	protected <V extends List<CachePath>> List<CachePath> mergeCachePaths(Class<?> entityType, List<CachePath> baseCachePath,
			Map<Class<?>, V> typeToMembersToInitialize)
	{
		if (typeToMembersToInitialize == null)
		{
			return baseCachePath;
		}
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType, true);
		if (metaData == null)
		{
			return baseCachePath;
		}
		List<CachePath> cachePathsOfType = typeToMembersToInitialize.get(metaData.getEntityType());
		if (cachePathsOfType == null)
		{
			return baseCachePath;
		}
		if (baseCachePath == null)
		{
			return cachePathsOfType;
		}
		ArrayList<CachePath> cachePaths = new ArrayList<CachePath>(baseCachePath);
		cachePaths.addAll(cachePathsOfType);
		return cachePaths;
	}

	protected <V extends List<CachePath>> IPrefetchState ensureInitializedRelationsIntern(Object objects, Map<Class<?>, V> typeToMembersToInitialize)
	{
		if (objects == null)
		{
			return null;
		}
		ICacheModification cacheModification = this.cacheModification;
		boolean oldActive = cacheModification.isActive();
		if (!oldActive)
		{
			cacheModification.setActive(true);
		}
		try
		{
			boolean setCreated = false;
			ISet<AlreadyHandledItem> alreadyHandledSet = alreadyHandledSetTL.get();
			try
			{
				if (alreadyHandledSet == null)
				{
					alreadyHandledSet = new HashSet<AlreadyHandledItem>();
					alreadyHandledSetTL.set(alreadyHandledSet);
					setCreated = true;
				}

				IEntityMetaDataProvider entityMetaDataProvider = this.entityMetaDataProvider;
				ValueHolderContainerTemplate valueHolderContainerTemplate = this.valueHolderContainerTemplate;
				IdentityLinkedMap<ICacheIntern, ISet<IObjRef>> cacheToOrisLoadedHistory = new IdentityLinkedMap<ICacheIntern, ISet<IObjRef>>();
				IdentityLinkedMap<ICacheIntern, ISet<IObjRelation>> cacheToOrelsLoadedHistory = new IdentityLinkedMap<ICacheIntern, ISet<IObjRelation>>();
				IdentityLinkedMap<ICacheIntern, ISet<IObjRef>> cacheToOrisToLoad = new IdentityLinkedMap<ICacheIntern, ISet<IObjRef>>();
				IdentityLinkedMap<ICacheIntern, ISet<IObjRelation>> cacheToOrelsToLoad = new IdentityLinkedMap<ICacheIntern, ISet<IObjRelation>>();

				ArrayList<CascadeLoadItem> loadItems = new ArrayList<CascadeLoadItem>();

				if (objects instanceof Collection)
				{
					for (Object item : (Iterable<?>) objects)
					{
						if (item == null)
						{
							continue;
						}
						List<CachePath> cachePaths = null;
						if (typeToMembersToInitialize != null)
						{
							IEntityMetaData metaData = entityMetaDataProvider.getMetaData(item.getClass());

							cachePaths = typeToMembersToInitialize.get(metaData.getEntityType());

							if (cachePaths == null)
							{
								continue;
							}
						}
						ensureInitializedRelationsIntern(item, cachePaths, cacheToOrisToLoad, cacheToOrelsToLoad, cacheToOrisLoadedHistory,
								cacheToOrelsLoadedHistory, alreadyHandledSet, loadItems);
					}
				}
				else
				{
					List<CachePath> cachePaths = null;
					if (typeToMembersToInitialize != null)
					{
						IEntityMetaData metaData = entityMetaDataProvider.getMetaData(objects.getClass());
						cachePaths = typeToMembersToInitialize.get(metaData.getEntityType());

						if (cachePaths == null)
						{
							if (setCreated)
							{
								return new PrefetchState(alreadyHandledSet);
							}
							return null;
						}
					}
					ensureInitializedRelationsIntern(objects, cachePaths, cacheToOrisToLoad, cacheToOrelsToLoad, cacheToOrisLoadedHistory,
							cacheToOrelsLoadedHistory, alreadyHandledSet, loadItems);
				}
				// Remove all oris which have already been tried to load before
				if (cacheToOrisToLoad.isEmpty() && cacheToOrelsToLoad.isEmpty())
				{
					// No ori remaining which makes sense to try to load
					if (setCreated)
					{
						return new PrefetchState(alreadyHandledSet);
					}
					return null;
				}

				ArrayList<Object> hardRefList = new ArrayList<Object>();
				// Store hard-ref-list to global hard ref
				alreadyHandledSet.add(new AlreadyHandledItem(hardRefList, null));

				loadAndAddOrels(cacheToOrelsToLoad, hardRefList, cacheToOrelsLoadedHistory, cacheToOrisToLoad);
				loadAndAddOris(cacheToOrisToLoad, hardRefList, cacheToOrisLoadedHistory);

				while (!loadItems.isEmpty())
				{
					CascadeLoadItem[] currentLoadItems = loadItems.toArray(CascadeLoadItem.class);
					// Clear the items to be ready for cascaded items in new batch recursion step
					loadItems.clear();
					for (CascadeLoadItem cascadeLoadItem : currentLoadItems)
					{
						Object valueHolder = cascadeLoadItem.valueHolder;
						List<CachePath> cachePaths = cascadeLoadItem.cachePaths;

						// Merge the root prefetch path with the relative prefetch path
						cachePaths = mergeCachePaths(cascadeLoadItem.realType, cachePaths, typeToMembersToInitialize);

						IObjRefContainer vhc;
						ICacheIntern targetCache;
						IRelationInfoItem member;
						boolean doSetValue = false;
						Object obj;
						if (valueHolder instanceof IndirectValueHolderRef)
						{
							IndirectValueHolderRef valueHolderKey = (IndirectValueHolderRef) valueHolder;
							vhc = valueHolderKey.getVhc();
							targetCache = valueHolderKey.getRootCache();
							member = valueHolderKey.getMember();
						}
						else
						{
							DirectValueHolderRef valueHolderKey = (DirectValueHolderRef) valueHolder;
							IValueHolderContainer vhcTemp = (IValueHolderContainer) valueHolderKey.getVhc();
							vhc = vhcTemp;
							targetCache = vhcTemp.get__TargetCache();
							member = valueHolderKey.getMember();
							doSetValue = true;
						}
						int relationIndex = vhc.get__EntityMetaData().getIndexByRelation(member);
						IObjRef[] objRefs = vhc.get__ObjRefs(relationIndex);
						obj = valueHolderContainerTemplate.getValue(vhc, relationIndex, member, targetCache, objRefs, CacheDirective.failEarly());
						if (doSetValue && obj != null)
						{
							member.setValue(vhc, obj);
						}
						ensureInitializedRelationsIntern(obj, cachePaths, cacheToOrisToLoad, cacheToOrelsToLoad, cacheToOrisLoadedHistory,
								cacheToOrelsLoadedHistory, alreadyHandledSet, loadItems);
					}
					// Remove all oris which have already been tried to load before
					if (cacheToOrisToLoad.size() == 0 && cacheToOrelsToLoad.size() == 0)
					{
						// No ori remaining which makes sense to try to load
						if (setCreated)
						{
							return new PrefetchState(alreadyHandledSet);
						}
						return null;
					}
					loadAndAddOrels(cacheToOrelsToLoad, hardRefList, cacheToOrelsLoadedHistory, cacheToOrisToLoad);
					loadAndAddOris(cacheToOrisToLoad, hardRefList, cacheToOrisLoadedHistory);
				}
				if (setCreated)
				{
					return new PrefetchState(alreadyHandledSet);
				}
				return null;
			}
			finally
			{
				if (setCreated)
				{
					alreadyHandledSetTL.remove();
				}
			}
		}
		finally
		{
			if (!oldActive)
			{
				cacheModification.setActive(false);
			}
		}
	}

	protected void loadAndAddOris(ILinkedMap<ICacheIntern, ISet<IObjRef>> cacheToOrisToLoad, List<Object> hardRefList,
			Map<ICacheIntern, ISet<IObjRef>> cacheToOrisLoadedHistory)
	{
		Iterator<Entry<ICacheIntern, ISet<IObjRef>>> iter = cacheToOrisToLoad.iterator();
		while (iter.hasNext())
		{
			Entry<ICacheIntern, ISet<IObjRef>> entry = iter.next();
			ICacheIntern cache = entry.getKey();
			ISet<IObjRef> orisToLoad = entry.getValue();
			iter.remove();

			loadAndAddOris(cache, orisToLoad, hardRefList, cacheToOrisLoadedHistory);
		}
	}

	protected void loadAndAddOris(ICacheIntern cache, ISet<IObjRef> orisToLoad, List<Object> hardRefList,
			Map<ICacheIntern, ISet<IObjRef>> cacheToOrisLoadedHistory)
	{
		IList<Object> result = cache.getObjects(orisToLoad.toList(), cache, CacheDirective.none());
		hardRefList.add(result);
		ISet<IObjRef> orisLoadedHistory = cacheToOrisLoadedHistory.get(cache);
		if (orisLoadedHistory == null)
		{
			orisLoadedHistory = new HashSet<IObjRef>();
			cacheToOrisLoadedHistory.put(cache, orisLoadedHistory);
		}
		orisLoadedHistory.addAll(orisToLoad);
	}

	protected void loadAndAddOrels(ILinkedMap<ICacheIntern, ISet<IObjRelation>> cacheToOrelsToLoad, List<Object> hardRefList,
			Map<ICacheIntern, ISet<IObjRelation>> cacheToOrelsLoadedHistory, ILinkedMap<ICacheIntern, ISet<IObjRef>> cacheToOrisToLoad)
	{
		Iterator<Entry<ICacheIntern, ISet<IObjRelation>>> iter = cacheToOrelsToLoad.iterator();
		while (iter.hasNext())
		{
			Entry<ICacheIntern, ISet<IObjRelation>> entry = iter.next();
			ICacheIntern cache = entry.getKey();
			ISet<IObjRelation> orelsToLoad = entry.getValue();
			iter.remove();

			loadAndAddOrels(cache, orelsToLoad, hardRefList, cacheToOrelsLoadedHistory, cacheToOrisToLoad);
		}
	}

	protected void loadAndAddOrels(ICacheIntern cache, ISet<IObjRelation> orelsToLoad, List<Object> hardRefList,
			Map<ICacheIntern, ISet<IObjRelation>> cacheToOrelsLoadedHistory, ILinkedMap<ICacheIntern, ISet<IObjRef>> cacheToOrisToLoad)
	{
		IList<IObjRelationResult> objRelResults = cache.getObjRelations(orelsToLoad.toList(), cache, CacheDirective.none());

		ISet<IObjRef> orisToLoad = null;
		for (int a = 0, size = objRelResults.size(); a < size; a++)
		{
			IObjRelationResult objRelResult = objRelResults.get(a);
			for (IObjRef objRef : objRelResult.getRelations())
			{
				if (orisToLoad == null)
				{
					orisToLoad = cacheToOrisToLoad.get(cache);
					if (orisToLoad == null)
					{
						orisToLoad = new HashSet<IObjRef>();
						cacheToOrisToLoad.put(cache, orisToLoad);
					}
				}
				orisToLoad.add(objRef);
			}
		}
		ISet<IObjRelation> orelsLoadedHistory = cacheToOrelsLoadedHistory.get(cache);
		if (orelsLoadedHistory == null)
		{
			orelsLoadedHistory = new HashSet<IObjRelation>();
			cacheToOrelsLoadedHistory.put(cache, orelsLoadedHistory);
		}
		orelsLoadedHistory.addAll(orelsToLoad);
	}

	protected void ensureInitializedRelationsIntern(Object obj, List<CachePath> cachePaths, Map<ICacheIntern, ISet<IObjRef>> cacheToOrisToLoad,
			Map<ICacheIntern, ISet<IObjRelation>> cacheToOrelsToLoad, Map<ICacheIntern, ISet<IObjRef>> cacheToOrisLoadedHistory,
			Map<ICacheIntern, ISet<IObjRelation>> cacheToOrelsLoadedHistory, Set<AlreadyHandledItem> alreadyHandledSet, List<CascadeLoadItem> cascadeLoadItems)
	{
		if (obj == null)
		{
			return;
		}
		AlreadyHandledItem alreadyHandledItem = new AlreadyHandledItem(obj, cachePaths);
		if (!alreadyHandledSet.add(alreadyHandledItem))
		{
			return;
		}
		if (obj instanceof IndirectValueHolderRef)
		{
			IndirectValueHolderRef vhk = (IndirectValueHolderRef) obj;
			handleValueHolder(vhk, cachePaths, cacheToOrisToLoad, cacheToOrelsToLoad, cacheToOrisLoadedHistory, cacheToOrelsLoadedHistory, alreadyHandledSet,
					cascadeLoadItems);
			// Do nothing because this is only to prefetch RootCache entries
			return;
		}
		if (obj instanceof DirectValueHolderRef)
		{
			DirectValueHolderRef vhk = (DirectValueHolderRef) obj;
			if (!handleValueHolder(vhk, cachePaths, cacheToOrisToLoad, cacheToOrelsToLoad, cacheToOrisLoadedHistory, cacheToOrelsLoadedHistory,
					alreadyHandledSet, cascadeLoadItems))
			{
				return;
			}
			// force valueholder init. at this point we know that all related items are already in the cache. there will
			// be no roundtrip to the server
			obj = vhk.getMember().getValue(vhk.getVhc());
		}
		if (obj == null)
		{
			// this check is necessary because even if we create only instances of DirectValueHolderRef in cases where there is a not initalized relation
			// even then it might be possible that a concurrent thread initializes the valueholder to null (e.g. an empty to-one relation)
			return;
		}
		if (obj instanceof Iterable)
		{
			for (Object item : (Iterable<?>) obj)
			{
				if (item == null)
				{
					continue;
				}
				ensureInitializedRelationsIntern(item, cachePaths, cacheToOrisToLoad, cacheToOrelsToLoad, cacheToOrisLoadedHistory, cacheToOrelsLoadedHistory,
						alreadyHandledSet, cascadeLoadItems);
			}
			return;
		}
		if (cachePaths == null)
		{
			return;
		}
		IEntityMetaData metaData = ((IEntityMetaDataHolder) obj).get__EntityMetaData();
		IRelationInfoItem[] relationMembers = metaData.getRelationMembers();
		if (relationMembers.length == 0)
		{
			return;
		}
		IValueHolderContainer vhc = (IValueHolderContainer) obj;
		for (int a = cachePaths.size(); a-- > 0;)
		{
			CachePath path = cachePaths.get(a);

			int relationIndex = path.memberIndex;
			IRelationInfoItem member = relationMembers[relationIndex];

			if (ValueHolderState.INIT != vhc.get__State(relationIndex))
			{
				DirectValueHolderRef vhk = new DirectValueHolderRef(vhc, member);
				ensureInitializedRelationsIntern(vhk, path.children, cacheToOrisToLoad, cacheToOrelsToLoad, cacheToOrisLoadedHistory,
						cacheToOrelsLoadedHistory, alreadyHandledSet, cascadeLoadItems);
				continue;
			}
			Object memberValue = member.getValue(obj);
			if (memberValue == null)
			{
				continue;
			}
			ensureInitializedRelationsIntern(memberValue, path.children, cacheToOrisToLoad, cacheToOrelsToLoad, cacheToOrisLoadedHistory,
					cacheToOrelsLoadedHistory, alreadyHandledSet, cascadeLoadItems);
		}
	}

	protected boolean handleValueHolder(DirectValueHolderRef vhr, List<CachePath> cachePaths, Map<ICacheIntern, ISet<IObjRef>> cacheToOrisToLoad,
			Map<ICacheIntern, ISet<IObjRelation>> cacheToOrelsToLoad, Map<ICacheIntern, ISet<IObjRef>> cacheToOrisLoadedHistory,
			Map<ICacheIntern, ISet<IObjRelation>> cacheToOrelsLoadedHistory, Set<AlreadyHandledItem> alreadyHandledSet, List<CascadeLoadItem> cascadeLoadItems)
	{
		IRelationInfoItem member = vhr.getMember();
		boolean newOriToLoad = false;
		if (vhr instanceof IndirectValueHolderRef)
		{
			RootCacheValue rcv = (RootCacheValue) vhr.getVhc();
			ICacheIntern rootCache = ((IndirectValueHolderRef) vhr).getRootCache();
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(rcv.getEntityType());
			int relationIndex = metaData.getIndexByRelationName(member.getName());
			IObjRef[] rcvObjRefs = rcv.getRelation(relationIndex);
			if (rcvObjRefs == null)
			{
				IObjRelation self = valueHolderContainerTemplate.getSelf(rcv, member.getName());
				ISet<IObjRelation> orelsLoadedHistory = cacheToOrelsLoadedHistory.get(rootCache);
				if (orelsLoadedHistory == null || !orelsLoadedHistory.contains(self))
				{
					ISet<IObjRelation> orelsToLoad = cacheToOrelsToLoad.get(rootCache);
					if (orelsToLoad == null)
					{
						orelsToLoad = new HashSet<IObjRelation>();
						cacheToOrelsToLoad.put(rootCache, orelsToLoad);
					}
					orelsToLoad.add(self);

					CascadeLoadItem cascadeLoadItem = new CascadeLoadItem(member.getElementType(), vhr, cachePaths);
					cascadeLoadItems.add(cascadeLoadItem);
				}
				return false;
			}
			else if (rcvObjRefs.length > 0)
			{
				ISet<IObjRef> orisLoadedHistory = cacheToOrisLoadedHistory.get(rootCache);
				for (int b = rcvObjRefs.length; b-- > 0;)
				{
					IObjRef ori = rcvObjRefs[b];
					if (orisLoadedHistory != null && orisLoadedHistory.contains(ori))
					{
						// Object has been tried to load before but it is obviously not in the cache
						// So the load must have been failed somehow. It is assumed that the entity
						// is not persisted in the database anymore (deleted before) so the ORI is illegal.
						// We cleanup the ValueHolder so that future calls will not lead to
						// another unnecessary roundtrip to the server
						rcvObjRefs[b] = null;
						continue;
					}
					ISet<IObjRef> orisToLoad = cacheToOrisToLoad.get(rootCache);
					if (orisToLoad == null)
					{
						orisToLoad = new HashSet<IObjRef>();
						cacheToOrisToLoad.put(rootCache, orisToLoad);
					}
					orisToLoad.add(ori);
					newOriToLoad = true;
				}
				if (newOriToLoad)
				{
					CascadeLoadItem cascadeLoadItem = new CascadeLoadItem(member.getElementType(), vhr, cachePaths);
					cascadeLoadItems.add(cascadeLoadItem);
				}
			}
			return false;
		}
		IValueHolderContainer vhc = (IValueHolderContainer) vhr.getVhc();
		int relationIndex = vhc.get__EntityMetaData().getIndexByRelationName(member.getName());

		if (ValueHolderState.INIT == vhc.get__State(relationIndex))
		{
			return true;
		}
		ICacheIntern cache = vhc.get__TargetCache();
		IObjRef[] objRefs = vhc.get__ObjRefs(relationIndex);
		if (objRefs == null)
		{
			IObjRelation self = vhc.get__Self(relationIndex);
			ArrayList<IObjRelation> orels = new ArrayList<IObjRelation>();
			orels.add(self);
			IList<IObjRelationResult> orelResults = cache.getObjRelations(orels, cache, failEarlyReturnMisses);
			IObjRelationResult orelResult = orelResults.get(0);
			if (orelResult == null)
			{
				ISet<IObjRelation> orelsLoadedHistory = cacheToOrelsLoadedHistory.get(cache);
				if (orelsLoadedHistory == null || !orelsLoadedHistory.contains(self))
				{
					ISet<IObjRelation> orelsToLoad = cacheToOrelsToLoad.get(cache);
					if (orelsToLoad == null)
					{
						orelsToLoad = new HashSet<IObjRelation>();
						cacheToOrelsToLoad.put(cache, orelsToLoad);
					}
					orelsToLoad.add(self);

					CascadeLoadItem cascadeLoadItem = new CascadeLoadItem(member.getElementType(), vhr, cachePaths);
					cascadeLoadItems.add(cascadeLoadItem);
				}
				return false;
			}
			objRefs = orelResult.getRelations();
			if (objRefs != null)
			{
				vhc.set__ObjRefs(relationIndex, objRefs);
			}
		}
		if (objRefs != null && objRefs.length > 0)
		{
			List<Object> loadedObjects = cache.getObjects(new ArrayList<IObjRef>(objRefs), cache, failEarlyReturnMisses);
			try
			{
				for (int b = objRefs.length; b-- > 0;)
				{
					IObjRef ori = objRefs[b];
					Object loadedObject = loadedObjects.get(b);
					if (loadedObject != null)
					{
						continue;
					}
					ISet<IObjRef> orisLoadedHistory = cacheToOrisLoadedHistory.get(cache);
					if (orisLoadedHistory != null && orisLoadedHistory.contains(ori))
					{
						// Object has been tried to load before but it is obviously not in the cache
						// So the load must have been failed somehow. It is assumed that the entity
						// is not persisted in the database anymore (deleted before) so the ORI is illegal.
						// We cleanup the ValueHolder so that future calls will not lead to
						// another unnecessary roundtrip to the server
						objRefs[b] = null;
						continue;
					}
					ISet<IObjRef> orisToLoad = cacheToOrisToLoad.get(cache);
					if (orisToLoad == null)
					{
						orisToLoad = new HashSet<IObjRef>();
						cacheToOrisToLoad.put(cache, orisToLoad);
					}
					orisToLoad.add(ori);
					newOriToLoad = true;
				}
			}
			finally
			{
				loadedObjects.clear();
				loadedObjects = null;
			}
		}
		if (objRefs == null || newOriToLoad)
		{
			CascadeLoadItem cascadeLoadItem = new CascadeLoadItem(member.getElementType(), vhr, cachePaths);
			cascadeLoadItems.add(cascadeLoadItem);
			return false;
		}
		return true;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Collection createInstanceOfTargetExpectedType(Class<?> expectedType, Class<?> elementType)
	{
		// OneToMany or ManyToMany Relationship
		if (Iterable.class.isAssignableFrom(expectedType))
		{
			if (expectedType.isInterface())
			{
				if (Set.class.isAssignableFrom(expectedType))
				{
					return new ObservableHashSet();
				}
				return new ObservableArrayList();
			}
			try
			{
				return (Collection) expectedType.newInstance();
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Object convertResultListToExpectedType(List<Object> resultList, Class<?> expectedType, Class<?> elementType)
	{
		// OneToMany or ManyToMany Relationship
		if (Collection.class.isAssignableFrom(expectedType))
		{
			Collection targetCollection = createInstanceOfTargetExpectedType(expectedType, elementType);

			if (resultList != null)
			{
				((Collection<Object>) targetCollection).addAll(resultList);
			}
			return targetCollection;
		}
		if (resultList != null && !resultList.isEmpty())
		{
			return resultList.get(0);
		}

		return null;
	}

	@Override
	public Object[] extractPrimitives(IEntityMetaData metaData, Object obj)
	{
		ITypeInfoItem[] primitiveMembers = metaData.getPrimitiveMembers();
		Object[] primitives;

		if (primitiveMembers.length == 0)
		{
			primitives = emptyObjectArray;
		}
		else
		{
			primitives = new Object[primitiveMembers.length];
			for (int a = primitiveMembers.length; a-- > 0;)
			{
				ITypeInfoItem primitiveMember = primitiveMembers[a];

				Object primitiveValue = primitiveMember.getValue(obj, false);

				if (primitiveValue != null && java.util.Date.class.isAssignableFrom(primitiveValue.getClass()))
				{
					primitiveValue = ((java.util.Date) primitiveValue).getTime();
				}
				primitives[a] = primitiveValue;
			}
		}

		return primitives;
	}

	@Override
	public IObjRef[][] extractRelations(IEntityMetaData metaData, Object obj)
	{
		return extractRelations(metaData, obj, null);
	}

	@Override
	public IObjRef[][] extractRelations(IEntityMetaData metaData, Object obj, List<Object> relationValues)
	{
		IRelationInfoItem[] relationMembers = metaData.getRelationMembers();

		if (relationMembers.length == 0)
		{
			return ObjRef.EMPTY_ARRAY_ARRAY;
		}
		IValueHolderContainer vhc = (IValueHolderContainer) obj;
		IObjRef[][] relations = new IObjRef[relationMembers.length][];

		IObjRefHelper oriHelper = this.oriHelper;
		for (int a = relationMembers.length; a-- > 0;)
		{
			if (ValueHolderState.INIT != vhc.get__State(a))
			{
				relations[a] = vhc.get__ObjRefs(a);
				continue;
			}
			Object relationValue = relationMembers[a].getValue(obj, false);
			if (relationValue == null)
			{
				relations[a] = ObjRef.EMPTY_ARRAY;
				continue;
			}
			IList<IObjRef> oris = oriHelper.extractObjRefList(relationValue, null, null);
			if (relationValues != null)
			{
				relationValues.add(relationValue);
			}
			if (oris != null)
			{
				relations[a] = oris.toArray(IObjRef.class);
			}
		}

		return relations;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T, S> IList<T> extractTargetEntities(List<S> sourceEntities, String sourceToTargetEntityPropertyPath, Class<S> sourceEntityType)
	{
		// Einen Accessor ermitteln, der die gesamte Hierachie aus dem propertyPath ('A.B.C') selbststaendig traversiert
		ITypeInfoItem member = typeInfoProvider.getHierarchicMember(sourceEntityType, sourceToTargetEntityPropertyPath);

		// MetaDaten der Ziel-Entity ermitteln, da wir (generisch) den PK brauchen, um damit ein DISTINCT-Behavior durch
		// eine Map als Zwischenstruktur zu
		// erreichen
		IEntityMetaData targetMetaData = entityMetaDataProvider.getMetaData(member.getElementType());
		ITypeInfoItem targetIdMember = targetMetaData.getIdMember();

		// Damit bei der Traversion keine Initialisierungen mit DB-Roundtrips entstehen, machen wir vorher eine Prefetch
		// passend zum PropertyPath auf allen
		// uebergebenen Quell-Entities
		// Dadurch entstehen maximal 2 gebatchte SELECTs, egal wie gross die Liste ist
		IPrefetchHandle prefetch = createPrefetch().add(sourceEntityType, sourceToTargetEntityPropertyPath).build();
		@SuppressWarnings("unused")
		// Speichere das State-Result unbenutzt - wichtig fuer concurrent GC Aktivitaeten, um Verluste an
		// Entity-Referenzen zu verhindern
		IPrefetchState state = prefetch.prefetch(sourceEntities);

		LinkedHashMap<Object, T> targetDistinctMap = new LinkedHashMap<Object, T>();
		// Danach traversieren, wobei wir jetzt wissen, dass uns das keine Roundtrips kostet
		for (int a = 0, size = sourceEntities.size(); a < size; a++)
		{
			S sourceEntity = sourceEntities.get(a);
			if (sourceEntity == null)
			{
				continue;
			}
			Object targetEntities = member.getValue(sourceEntity);
			if (targetEntities == null)
			{
				continue;
			}
			// Ergebnismenge flexibel (bei *-To-Many) verarbeiten oder so lassen (bei *-To-One)
			if (targetEntities instanceof Iterable)
			{
				for (Object targetEntity : (Iterable<?>) targetEntities)
				{
					if (targetEntity == null)
					{
						continue;
					}
					Object targetId = targetIdMember.getValue(targetEntity);
					if (targetId == null)
					{
						// Falls die Entity keine ID hat, speichern wir sie ausnahmsweise selbst als Key
						targetId = targetEntity;
					}
					targetDistinctMap.put(targetId, (T) targetEntity);
				}
			}
			else
			{
				Object targetId = targetIdMember.getValue(targetEntities);
				if (targetId == null)
				{
					// Falls die Entity keine ID hat, speichern wir sie ausnahmsweise selbst als Key
					targetId = targetEntities;
				}
				targetDistinctMap.put(targetId, (T) targetEntities);
			}
		}
		// Alle values sind unsere eindeutigen Target Entities ohne Duplikate
		return targetDistinctMap.values();
	}
}
