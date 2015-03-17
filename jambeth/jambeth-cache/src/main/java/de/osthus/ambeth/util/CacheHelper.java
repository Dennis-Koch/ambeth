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
import de.osthus.ambeth.collections.LinkedHashSet;
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
import de.osthus.ambeth.metadata.IMemberTypeProvider;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.metadata.PrimitiveMember;
import de.osthus.ambeth.metadata.RelationMember;
import de.osthus.ambeth.mixin.ValueHolderContainerMixin;
import de.osthus.ambeth.proxy.IEntityMetaDataHolder;
import de.osthus.ambeth.proxy.IObjRefContainer;
import de.osthus.ambeth.proxy.IValueHolderContainer;

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
	protected IMemberTypeProvider memberTypeProvider;

	@Autowired
	protected IObjRefHelper oriHelper;

	@Autowired
	protected ValueHolderContainerMixin valueHolderContainerTemplate;

	@Override
	public void buildCachePath(Class<?> entityType, String memberToInitialize, ISet<AppendableCachePath> cachePaths)
	{
		Class<?> currentType = entityType;
		String requestedMemberName = memberToInitialize;
		AppendableCachePath currentCachePath = null;
		ISet<AppendableCachePath> currentCachePaths = cachePaths;

		while (true)
		{
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(currentType);
			Member widenedMember = metaData.getWidenedMatchingMember(requestedMemberName);
			if (widenedMember == null)
			{
				throw new IllegalArgumentException("No member found to resolve path " + entityType.getName() + "." + memberToInitialize);
			}
			String widenedMemberName = widenedMember.getName();
			if (widenedMember instanceof PrimitiveMember)
			{
				if (widenedMemberName.equals(memberToInitialize))
				{
					// this member does not need to be prefetched
					return;
				}
				// widened member has been found but not the full path of the requested member name
				throw new IllegalArgumentException("No member found to resolve path " + entityType.getName() + "." + memberToInitialize);
			}
			AppendableCachePath childCachePath = null;
			if (currentCachePaths == null)
			{
				currentCachePaths = new LinkedHashSet<AppendableCachePath>();
				currentCachePath.children = currentCachePaths;
			}
			for (AppendableCachePath cachePath : currentCachePaths)
			{
				if (widenedMemberName.equals(cachePath.memberName))
				{
					childCachePath = cachePath;
					break;
				}
			}
			if (childCachePath == null)
			{
				int relationIndex = metaData.getIndexByRelation(widenedMember);
				childCachePath = new AppendableCachePath(widenedMember.getElementType(), relationIndex, widenedMemberName);
				currentCachePaths.add(childCachePath);
			}
			if (widenedMemberName.equals(requestedMemberName))
			{
				// we have travered the full path of the requested member name
				return;
			}
			requestedMemberName = requestedMemberName.substring(widenedMemberName.length() + 1);
			currentCachePath = childCachePath;
			currentType = currentCachePath.memberType;
			currentCachePaths = currentCachePath.children;
		}
	}

	@Override
	public IPrefetchConfig createPrefetch()
	{
		return beanContext.registerBean(PrefetchConfig.class).finish();
	}

	@Override
	public IPrefetchState ensureInitializedRelations(Object objects, ILinkedMap<Class<?>, CachePath[]> entityTypeToPrefetchSteps)
	{
		if (objects == null || entityTypeToPrefetchSteps == null || entityTypeToPrefetchSteps.size() == 0)
		{
			return null;
		}
		return ensureInitializedRelationsIntern(objects, entityTypeToPrefetchSteps);
	}

	@Override
	public IPrefetchState prefetch(Object objects)
	{
		return ensureInitializedRelationsIntern(objects, null);
	}

	protected CachePath[] mergeCachePaths(Class<?> entityType, CachePath[] baseCachePath, Map<Class<?>, CachePath[]> typeToMembersToInitialize)
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
		CachePath[] cachePathsOfType = typeToMembersToInitialize.get(metaData.getEntityType());
		if (cachePathsOfType == null)
		{
			return baseCachePath;
		}
		if (baseCachePath == null)
		{
			return cachePathsOfType;
		}
		CachePath[] cachePaths = new CachePath[baseCachePath.length + cachePathsOfType.length];
		System.arraycopy(baseCachePath, 0, cachePaths, 0, baseCachePath.length);
		System.arraycopy(cachePathsOfType, 0, cachePaths, baseCachePath.length, cachePathsOfType.length);
		return cachePaths;
	}

	protected IPrefetchState ensureInitializedRelationsIntern(Object objects, ILinkedMap<Class<?>, CachePath[]> entityTypeToPrefetchSteps)
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
				ValueHolderContainerMixin valueHolderContainerTemplate = this.valueHolderContainerTemplate;
				IdentityLinkedMap<ICacheIntern, ISet<IObjRef>> cacheToOrisLoadedHistory = new IdentityLinkedMap<ICacheIntern, ISet<IObjRef>>();
				IdentityLinkedMap<ICacheIntern, ISet<IObjRelation>> cacheToOrelsLoadedHistory = new IdentityLinkedMap<ICacheIntern, ISet<IObjRelation>>();
				IdentityLinkedMap<ICacheIntern, ISet<IObjRef>> cacheToOrisToLoad = new IdentityLinkedMap<ICacheIntern, ISet<IObjRef>>();
				IdentityLinkedMap<ICacheIntern, IMap<IObjRelation, Boolean>> cacheToOrelsToLoad = new IdentityLinkedMap<ICacheIntern, IMap<IObjRelation, Boolean>>();

				ArrayList<CascadeLoadItem> loadItems = new ArrayList<CascadeLoadItem>();

				if (objects instanceof Collection)
				{
					for (Object item : (Iterable<?>) objects)
					{
						if (item == null)
						{
							continue;
						}
						CachePath[] cachePaths = null;
						if (entityTypeToPrefetchSteps != null)
						{
							IEntityMetaData metaData = entityMetaDataProvider.getMetaData(item.getClass());

							cachePaths = entityTypeToPrefetchSteps.get(metaData.getEntityType());

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
					CachePath[] cachePaths = null;
					if (entityTypeToPrefetchSteps != null)
					{
						IEntityMetaData metaData = ((IEntityMetaDataHolder) objects).get__EntityMetaData();
						cachePaths = entityTypeToPrefetchSteps.get(metaData.getEntityType());

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
						DirectValueHolderRef valueHolder = cascadeLoadItem.valueHolder;
						CachePath[] cachePaths = cascadeLoadItem.cachePaths;

						// Merge the root prefetch path with the relative prefetch path
						cachePaths = mergeCachePaths(cascadeLoadItem.realType, cachePaths, entityTypeToPrefetchSteps);

						IObjRefContainer vhc = valueHolder.getVhc();
						RelationMember member = valueHolder.getMember();
						ICacheIntern targetCache;
						boolean doSetValue = false;
						if (valueHolder instanceof IndirectValueHolderRef)
						{
							IndirectValueHolderRef valueHolderKey = (IndirectValueHolderRef) valueHolder;
							targetCache = valueHolderKey.getRootCache();
						}
						else
						{
							targetCache = ((IValueHolderContainer) vhc).get__TargetCache();
							doSetValue = true;
						}
						int relationIndex = vhc.get__EntityMetaData().getIndexByRelation(member);
						IObjRef[] objRefs = vhc.get__ObjRefs(relationIndex);
						Object obj = valueHolderContainerTemplate.getValue(vhc, relationIndex, member, targetCache, objRefs, CacheDirective.failEarly());
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

	protected void loadAndAddOrels(ILinkedMap<ICacheIntern, IMap<IObjRelation, Boolean>> cacheToOrelsToLoad, List<Object> hardRefList,
			Map<ICacheIntern, ISet<IObjRelation>> cacheToOrelsLoadedHistory, ILinkedMap<ICacheIntern, ISet<IObjRef>> cacheToOrisToLoad)
	{
		Iterator<Entry<ICacheIntern, IMap<IObjRelation, Boolean>>> iter = cacheToOrelsToLoad.iterator();
		while (iter.hasNext())
		{
			Entry<ICacheIntern, IMap<IObjRelation, Boolean>> entry = iter.next();
			ICacheIntern cache = entry.getKey();
			IMap<IObjRelation, Boolean> orelsToLoad = entry.getValue();
			iter.remove();

			loadAndAddOrels(cache, orelsToLoad, hardRefList, cacheToOrelsLoadedHistory, cacheToOrisToLoad);
		}
	}

	protected void loadAndAddOrels(ICacheIntern cache, IMap<IObjRelation, Boolean> orelsToLoad, List<Object> hardRefList,
			Map<ICacheIntern, ISet<IObjRelation>> cacheToOrelsLoadedHistory, ILinkedMap<ICacheIntern, ISet<IObjRef>> cacheToOrisToLoad)
	{
		IList<IObjRelation> objRelList = orelsToLoad.keyList();
		IList<IObjRelationResult> objRelResults = cache.getObjRelations(objRelList, cache, CacheDirective.returnMisses());

		ISet<IObjRef> orisToLoad = null;
		for (int a = 0, size = objRelResults.size(); a < size; a++)
		{
			IObjRelation objRel = objRelList.get(a);
			if (orelsToLoad.get(objRel).booleanValue())
			{
				// fetch only the objRefs, not the objects themselves
				continue;
			}
			IObjRelationResult objRelResult = objRelResults.get(a);
			if (objRelResult == null)
			{
				continue;
			}
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
		orelsLoadedHistory.addAll(objRelList);
	}

	protected void ensureInitializedRelationsIntern(Object obj, CachePath[] cachePaths, Map<ICacheIntern, ISet<IObjRef>> cacheToOrisToLoad,
			Map<ICacheIntern, IMap<IObjRelation, Boolean>> cacheToOrelsToLoad, Map<ICacheIntern, ISet<IObjRef>> cacheToOrisLoadedHistory,
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
		RelationMember[] relationMembers = metaData.getRelationMembers();
		if (relationMembers.length == 0)
		{
			return;
		}
		IValueHolderContainer vhc = (IValueHolderContainer) obj;
		for (int a = cachePaths.length; a-- > 0;)
		{
			CachePath path = cachePaths[a];

			int relationIndex = path.memberIndex;
			RelationMember member = relationMembers[relationIndex];

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

	protected boolean handleValueHolder(DirectValueHolderRef vhr, CachePath[] cachePaths, Map<ICacheIntern, ISet<IObjRef>> cacheToOrisToLoad,
			Map<ICacheIntern, IMap<IObjRelation, Boolean>> cacheToOrelsToLoad, Map<ICacheIntern, ISet<IObjRef>> cacheToOrisLoadedHistory,
			Map<ICacheIntern, ISet<IObjRelation>> cacheToOrelsLoadedHistory, Set<AlreadyHandledItem> alreadyHandledSet, List<CascadeLoadItem> cascadeLoadItems)
	{
		RelationMember member = vhr.getMember();
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
					IMap<IObjRelation, Boolean> orelsToLoad = cacheToOrelsToLoad.get(rootCache);
					if (orelsToLoad == null)
					{
						orelsToLoad = new HashMap<IObjRelation, Boolean>();
						cacheToOrelsToLoad.put(rootCache, orelsToLoad);
					}
					orelsToLoad.put(self, Boolean.valueOf(vhr.isObjRefsOnly()));
					addCascadeLoadItem(member, vhr, cachePaths, cascadeLoadItems);
				}
				return false;
			}
			else if (!vhr.isObjRefsOnly() && rcvObjRefs.length > 0)
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
					addCascadeLoadItem(member, vhr, cachePaths, cascadeLoadItems);
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
					IMap<IObjRelation, Boolean> orelsToLoad = cacheToOrelsToLoad.get(cache);
					if (orelsToLoad == null)
					{
						orelsToLoad = new HashMap<IObjRelation, Boolean>();
						cacheToOrelsToLoad.put(cache, orelsToLoad);
					}
					orelsToLoad.put(self, vhr.isObjRefsOnly());
					addCascadeLoadItem(member, vhr, cachePaths, cascadeLoadItems);
				}
				return false;
			}
			objRefs = orelResult.getRelations();
			if (objRefs != null)
			{
				vhc.set__ObjRefs(relationIndex, objRefs);
			}
		}
		if (!vhr.isObjRefsOnly() && objRefs != null && objRefs.length > 0)
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
			addCascadeLoadItem(member, vhr, cachePaths, cascadeLoadItems);
			return false;
		}
		return true;
	}

	protected void addCascadeLoadItem(RelationMember member, DirectValueHolderRef vhr, CachePath[] cachePaths, List<CascadeLoadItem> cascadeLoadItems)
	{
		if (cachePaths != null || !vhr.isObjRefsOnly())
		{
			CascadeLoadItem cascadeLoadItem = new CascadeLoadItem(member.getElementType(), vhr, cachePaths);
			cascadeLoadItems.add(cascadeLoadItem);
		}
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
		Member[] primitiveMembers = metaData.getPrimitiveMembers();
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
				Member primitiveMember = primitiveMembers[a];

				Object primitiveValue = primitiveMember.getValue(obj, true);

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
		RelationMember[] relationMembers = metaData.getRelationMembers();

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
		Member member = memberTypeProvider.getMember(sourceEntityType, sourceToTargetEntityPropertyPath);

		// MetaDaten der Ziel-Entity ermitteln, da wir (generisch) den PK brauchen, um damit ein DISTINCT-Behavior durch
		// eine Map als Zwischenstruktur zu
		// erreichen
		IEntityMetaData targetMetaData = entityMetaDataProvider.getMetaData(member.getElementType());
		Member targetIdMember = targetMetaData.getIdMember();

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

	@Override
	public AppendableCachePath copyCachePathToAppendable(CachePath cachePath)
	{
		CachePath[] children = cachePath.children;
		LinkedHashSet<AppendableCachePath> clonedChildren = null;
		if (children != null)
		{
			clonedChildren = LinkedHashSet.create(children.length);
			for (int a = children.length; a-- > 0;)
			{
				clonedChildren.add(copyCachePathToAppendable(children[a]));
			}
		}
		AppendableCachePath clonedCachePath = new AppendableCachePath(cachePath.memberType, cachePath.memberIndex, cachePath.memberName);
		clonedCachePath.children = clonedChildren;
		return clonedCachePath;
	}

	@Override
	public CachePath[] copyAppendableToCachePath(ISet<AppendableCachePath> children)
	{
		if (children == null)
		{
			return null;
		}
		CachePath[] clonedChildren = new CachePath[children.size()];
		int index = 0;
		for (AppendableCachePath child : children)
		{
			clonedChildren[index] = copyAppendableToCachePath(child);
			index++;
		}
		return clonedChildren;
	}

	@Override
	public CachePath copyAppendableToCachePath(AppendableCachePath cachePath)
	{
		CachePath[] clonedChildren = copyAppendableToCachePath(cachePath.children);
		return new CachePath(cachePath.memberType, cachePath.memberIndex, cachePath.memberName, clonedChildren);
	}

	@Override
	public void unionCachePath(AppendableCachePath cachePath, AppendableCachePath other)
	{
		ISet<AppendableCachePath> otherChildren = other.children;
		if (otherChildren == null)
		{
			// fast case 1
			return;
		}
		ISet<AppendableCachePath> children = cachePath.children;
		if (children == null)
		{
			// fast case 2
			cachePath.children = otherChildren;
			return;
		}
		for (AppendableCachePath otherCachePath : otherChildren)
		{
			if (children.add(otherCachePath))
			{
				continue;
			}
			unionCachePath(children.get(otherCachePath), otherCachePath);
		}
	}
}
