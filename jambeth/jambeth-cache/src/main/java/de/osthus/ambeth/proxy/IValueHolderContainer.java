package de.osthus.ambeth.proxy;

import de.osthus.ambeth.cache.ICacheIntern;
import de.osthus.ambeth.cache.ValueHolderState;
import de.osthus.ambeth.cache.model.IObjRelation;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.typeinfo.IRelationInfoItem;

public interface IValueHolderContainer
{
	IObjRelation get__Self(IRelationInfoItem member);

	IObjRelation get__Self(String memberName);

	ICacheIntern get__TargetCache();

	void set__TargetCache(ICacheIntern targetCache);

	ValueHolderState get__State(IRelationInfoItem member);

	IObjRef[] get__ObjRefs(IRelationInfoItem member);
}
