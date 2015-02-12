package de.osthus.ambeth.mixin;

import java.util.Arrays;
import java.util.Set;

import de.osthus.ambeth.cache.CacheDirective;
import de.osthus.ambeth.cache.ICacheIntern;
import de.osthus.ambeth.cache.ValueHolderState;
import de.osthus.ambeth.cache.model.IObjRelation;
import de.osthus.ambeth.cache.model.IObjRelationResult;
import de.osthus.ambeth.cache.transfer.ObjRelation;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.EmptyList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.ILightweightTransaction;
import de.osthus.ambeth.merge.IObjRefHelper;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.metadata.RelationMember;
import de.osthus.ambeth.proxy.IObjRefContainer;
import de.osthus.ambeth.proxy.IValueHolderContainer;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.ambeth.util.ICacheHelper;

public class ValueHolderContainerMixin
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected ICacheHelper cacheHelper;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IObjRefHelper oriHelper;

	@Autowired(optional = true)
	protected ILightweightTransaction transaction;

	public IObjRelation getSelf(IObjRefContainer entity, String memberName)
	{
		IList<IObjRef> allObjRefs = oriHelper.entityToAllObjRefs(entity);
		return new ObjRelation(allObjRefs.toArray(IObjRef.class), memberName);
	}

	public IObjRelation getSelf(IObjRefContainer entity, int relationIndex)
	{
		String memberName = entity.get__EntityMetaData().getRelationMembers()[relationIndex].getName();
		IList<IObjRef> allObjRefs = oriHelper.entityToAllObjRefs(entity);
		return new ObjRelation(allObjRefs.toArray(IObjRef.class), memberName);
	}

	public Object getValue(IObjRefContainer entity, RelationMember[] relationMembers, int relationIndex, ICacheIntern targetCache, IObjRef[] objRefs)
	{
		return getValue(entity, relationIndex, relationMembers[relationIndex], targetCache, objRefs, CacheDirective.none());
	}

	public Object getValue(IObjRefContainer entity, int relationindex, RelationMember relationMember, final ICacheIntern targetCache, IObjRef[] objRefs,
			final Set<CacheDirective> cacheDirective)
	{
		Object value;
		if (targetCache == null)
		{
			// This happens if an entity gets newly created and immediately called for relations (e.g. collections to add sth)
			value = cacheHelper.createInstanceOfTargetExpectedType(relationMember.getRealType(), relationMember.getElementType());
		}
		else
		{
			IList<Object> results;
			if (objRefs == null)
			{
				final IObjRelation self = getSelf(entity, relationMember.getName());

				if (transaction != null)
				{
					results = transaction.runInLazyTransaction(new IResultingBackgroundWorkerDelegate<IList<Object>>()
					{
						@Override
						public IList<Object> invoke() throws Throwable
						{
							IList<IObjRelationResult> objRelResults = targetCache.getObjRelations(Arrays.asList(self), targetCache, cacheDirective);
							if (objRelResults.size() == 0)
							{
								return EmptyList.getInstance();
							}
							else
							{
								IObjRelationResult objRelResult = objRelResults.get(0);
								return targetCache.getObjects(new ArrayList<IObjRef>(objRelResult.getRelations()), targetCache, cacheDirective);
							}
						}
					});
				}
				else
				{
					IList<IObjRelationResult> objRelResults = targetCache.getObjRelations(Arrays.asList(self), targetCache, cacheDirective);
					if (objRelResults.size() == 0)
					{
						results = EmptyList.getInstance();
					}
					else
					{
						IObjRelationResult objRelResult = objRelResults.get(0);
						results = targetCache.getObjects(new ArrayList<IObjRef>(objRelResult.getRelations()), targetCache, cacheDirective);
					}
				}
			}
			else
			{
				results = targetCache.getObjects(new ArrayList<IObjRef>(objRefs), targetCache, cacheDirective);
			}
			value = cacheHelper.convertResultListToExpectedType(results, relationMember.getRealType(), relationMember.getElementType());
		}
		return value;
	}

	public Object getValue(IValueHolderContainer vhc, int relationIndex)
	{
		return getValue(vhc, relationIndex, CacheDirective.none());
	}

	public Object getValue(IValueHolderContainer vhc, int relationIndex, Set<CacheDirective> cacheDirective)
	{
		IEntityMetaData metaData = vhc.get__EntityMetaData();
		RelationMember relationMember = metaData.getRelationMembers()[relationIndex];
		if (ValueHolderState.INIT == vhc.get__State(relationIndex))
		{
			return relationMember.getValue(vhc);
		}
		IObjRef[] objRefs = vhc.get__ObjRefs(relationIndex);
		return getValue(vhc, relationIndex, relationMember, vhc.get__TargetCache(), objRefs, cacheDirective);
	}
}