package de.osthus.ambeth.proxy;

import de.osthus.ambeth.cache.ICacheIntern;
import de.osthus.ambeth.cache.ValueHolderState;
import de.osthus.ambeth.cache.model.IObjRelation;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.typeinfo.IRelationInfoItem;

public interface IValueHolderContainer
{
	IObjRelation getSelf(IRelationInfoItem member);

	IObjRelation getSelf(String memberName);

	ICacheIntern get__TargetCache();

	void set__TargetCache(ICacheIntern targetCache);

	ValueHolderState getState(IRelationInfoItem member);

	IObjRef[] getObjRefs(IRelationInfoItem member);
}
