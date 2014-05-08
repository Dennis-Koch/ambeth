package de.osthus.ambeth.persistence;

import java.util.List;

import de.osthus.ambeth.merge.model.IPrimitiveUpdateItem;
import de.osthus.ambeth.merge.model.IRelationUpdateItem;
import de.osthus.ambeth.util.IAlreadyLinkedCache;
import de.osthus.ambeth.util.IParamHolder;

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
