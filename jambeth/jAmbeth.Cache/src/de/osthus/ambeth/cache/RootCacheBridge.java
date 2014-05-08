package de.osthus.ambeth.cache;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import de.osthus.ambeth.cache.model.ILoadContainer;
import de.osthus.ambeth.cache.model.IObjRelation;
import de.osthus.ambeth.cache.model.IObjRelationResult;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.ITransactionState;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.service.ICacheRetriever;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;
import de.osthus.ambeth.util.IInterningFeature;
import de.osthus.ambeth.util.ParamChecker;

public class RootCacheBridge implements IInitializingBean, ICacheRetriever
{
	protected static final Set<CacheDirective> committedRootCacheCD = EnumSet.of(CacheDirective.FailEarly, CacheDirective.ReturnMisses,
			CacheDirective.LoadContainerResult);

	protected IRootCache committedRootCache;

	protected IEntityMetaDataProvider entityMetaDataProvider;

	protected ICacheRetriever uncommittedCacheRetriever;

	protected IInterningFeature interningFeature;

	protected ITransactionState transactionState;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(committedRootCache, "CommittedRootCache");
		ParamChecker.assertNotNull(entityMetaDataProvider, "EntityMetaDataProvider");
		ParamChecker.assertNotNull(uncommittedCacheRetriever, "UncommittedCacheRetriever");
	}

	public void setCommittedRootCache(IRootCache committedRootCache)
	{
		this.committedRootCache = committedRootCache;
	}

	public void setEntityMetaDataProvider(IEntityMetaDataProvider entityMetaDataProvider)
	{
		this.entityMetaDataProvider = entityMetaDataProvider;
	}

	public void setInterningFeature(IInterningFeature interningFeature)
	{
		this.interningFeature = interningFeature;
	}

	public void setTransactionState(ITransactionState transactionState)
	{
		this.transactionState = transactionState;
	}

	public void setUncommittedCacheRetriever(ICacheRetriever uncommittedCacheRetriever)
	{
		this.uncommittedCacheRetriever = uncommittedCacheRetriever;
	}

	@Override
	public List<ILoadContainer> getEntities(List<IObjRef> orisToLoad)
	{
		boolean isTransaction = false;
		if (transactionState != null)
		{
			isTransaction = transactionState.isTransactionActive();
		}
		ArrayList<ILoadContainer> result = new ArrayList<ILoadContainer>();
		if (!isTransaction)
		{
			// Allow committed root cache only OUT OF transactions to retrieve data by itself
			IList<Object> loadContainers = committedRootCache.getObjects(orisToLoad, CacheDirective.loadContainerResult());
			for (int a = loadContainers.size(); a-- > 0;)
			{
				result.add((ILoadContainer) loadContainers.get(a));
			}
			internStrings(result);
			return result;
		}
		List<IObjRef> orisToLoadWithVersion = new ArrayList<IObjRef>();
		List<IObjRef> missedOris = new ArrayList<IObjRef>();
		for (int i = orisToLoad.size(); i-- > 0;)
		{
			IObjRef ori = orisToLoad.get(i);
			if (ori.getVersion() != null)
			{
				orisToLoadWithVersion.add(ori);
			}
			else
			{
				missedOris.add(ori);
			}
		}
		IList<Object> loadContainers = committedRootCache.getObjects(orisToLoadWithVersion, committedRootCacheCD);
		for (int a = loadContainers.size(); a-- > 0;)
		{
			ILoadContainer loadContainer = (ILoadContainer) loadContainers.get(a);
			if (loadContainer == null)
			{
				missedOris.add(orisToLoadWithVersion.get(a));
			}
			else
			{
				result.add(loadContainer);
			}
		}
		if (missedOris.size() > 0)
		{
			List<ILoadContainer> uncommittedLoadContainer = uncommittedCacheRetriever.getEntities(missedOris);
			result.addAll(uncommittedLoadContainer);
		}
		internStrings(result);
		return result;
	}

	@Override
	public List<IObjRelationResult> getRelations(List<IObjRelation> objRelations)
	{
		List<IObjRelation> orelToLoadWithVersion = new ArrayList<IObjRelation>();
		List<IObjRelation> missedOrels = new ArrayList<IObjRelation>();
		for (int i = objRelations.size(); i-- > 0;)
		{
			IObjRelation orel = objRelations.get(i);
			if (orel.getVersion() != null)
			{
				orelToLoadWithVersion.add(orel);
			}
			else
			{
				missedOrels.add(orel);
			}
		}
		IList<IObjRelationResult> relationResults = committedRootCache.getObjRelations(orelToLoadWithVersion, committedRootCacheCD);
		List<IObjRelationResult> result = new ArrayList<IObjRelationResult>(objRelations.size());
		for (int a = relationResults.size(); a-- > 0;)
		{
			IObjRelationResult relationResult = relationResults.get(a);
			if (relationResult == null)
			{
				missedOrels.add(orelToLoadWithVersion.get(a));
			}
			else
			{
				result.add(relationResult);
			}
		}
		if (missedOrels.size() > 0)
		{
			List<IObjRelationResult> uncommittedRelationResult = uncommittedCacheRetriever.getRelations(missedOrels);
			result.addAll(uncommittedRelationResult);
		}
		return result;
	}

	protected void internStrings(List<ILoadContainer> loadContainers)
	{
		if (interningFeature == null)
		{
			// Feature is optional
			return;
		}
		for (int a = loadContainers.size(); a-- > 0;)
		{
			ILoadContainer loadContainer = loadContainers.get(a);
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(loadContainer.getReference().getRealType());
			Object[] primitives = loadContainer.getPrimitives();
			internPrimitiveMember(metaData, primitives, metaData.getCreatedByMember());
			internPrimitiveMember(metaData, primitives, metaData.getUpdatedByMember());
		}
	}

	protected void internPrimitiveMember(IEntityMetaData metaData, Object[] primitives, ITypeInfoItem member)
	{
		if (member == null)
		{
			return;
		}
		int index = metaData.getIndexByPrimitive(member);
		Object value = primitives[index];
		if (value instanceof String)
		{
			Object internValue = interningFeature.intern(value);
			if (value != internValue)
			{
				primitives[index] = internValue;
			}
		}
	}
}
