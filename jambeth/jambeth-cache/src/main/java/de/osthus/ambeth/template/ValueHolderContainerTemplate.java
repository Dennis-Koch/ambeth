package de.osthus.ambeth.template;

import java.util.Arrays;
import java.util.Set;

import de.osthus.ambeth.cache.CacheDirective;
import de.osthus.ambeth.cache.ICacheIntern;
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
import de.osthus.ambeth.merge.IObjRefHelper;
import de.osthus.ambeth.merge.IProxyHelper;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.proxy.IValueHolderContainer;
import de.osthus.ambeth.typeinfo.IRelationInfoItem;
import de.osthus.ambeth.util.ICacheHelper;

public class ValueHolderContainerTemplate
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

	@Autowired
	protected IProxyHelper proxyHelper;

	public IObjRelation getSelf(Object entity, String memberName)
	{
		IList<IObjRef> allObjRefs = oriHelper.entityToAllObjRefs(entity);
		return new ObjRelation(allObjRefs.toArray(IObjRef.class), memberName);
	}

	public Object getValue(Object entity, IRelationInfoItem[] relationMembers, int indexOfProperty, ICacheIntern targetCache, IObjRef[] objRefs)
	{
		return getValue(entity, relationMembers[indexOfProperty], targetCache, objRefs, CacheDirective.none());
	}

	public Object getValue(Object entity, IRelationInfoItem relationMember, ICacheIntern targetCache, IObjRef[] objRefs, Set<CacheDirective> cacheDirective)
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
				IObjRelation self = getSelf(entity, relationMember.getName());
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
			else
			{
				results = targetCache.getObjects(new ArrayList<IObjRef>(objRefs), targetCache, cacheDirective);
			}
			value = cacheHelper.convertResultListToExpectedType(results, relationMember.getRealType(), relationMember.getElementType());
		}
		return value;
	}

	public Object getValue(Object vhc, IRelationInfoItem member)
	{
		return getValue(vhc, member, CacheDirective.none());
	}

	public Object getValue(Object vhc, IRelationInfoItem relationMember, Set<CacheDirective> cacheDirective)
	{
		IProxyHelper proxyHelper = this.proxyHelper;
		if (proxyHelper.isInitialized(vhc, relationMember))
		{
			return relationMember.getValue(vhc);
		}
		IValueHolderContainer cast = (IValueHolderContainer) vhc;
		IObjRef[] objRefs = proxyHelper.getObjRefs(vhc, relationMember);
		return getValue(cast, relationMember, cast.get__TargetCache(), objRefs, cacheDirective);
	}
}