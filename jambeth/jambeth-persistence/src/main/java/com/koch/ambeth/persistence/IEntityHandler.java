package com.koch.ambeth.persistence;

import java.util.List;

import com.koch.ambeth.merge.model.IPrimitiveUpdateItem;
import com.koch.ambeth.merge.model.IRelationUpdateItem;
import com.koch.ambeth.persistence.util.IAlreadyLinkedCache;
import com.koch.ambeth.util.IParamHolder;

public interface IEntityHandler
{
	Class<?> getEntityType();

	void queueDelete(Object id, Object version);

	Object queueInsert(Object id, IParamHolder<Object> newId, List<IPrimitiveUpdateItem> puis, List<IRelationUpdateItem> ruis);

	Object queueUpdate(Object id, Object version, List<IPrimitiveUpdateItem> puis, List<IRelationUpdateItem> ruis);

	void postProcessInsertAndUpdate(Object id, Object version, List<IPrimitiveUpdateItem> puis, List<IRelationUpdateItem> ruis,
			IAlreadyLinkedCache alreadyLinkedCache);

	Object[] acquireIds(int count);

	Object read(Object id, Object version);
}
