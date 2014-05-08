package de.osthus.ambeth.merge;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;

public interface IObjRefHelper
{
	IList<IObjRef> extractObjRefList(Object objValue, MergeHandle mergeHandle);

	IList<IObjRef> extractObjRefList(Object objValue, MergeHandle mergeHandle, IList<IObjRef> targetOriList);

	IList<IObjRef> extractObjRefList(Object objValue, MergeHandle mergeHandle, IList<IObjRef> targetOriList, EntityCallback entityCallback);

	IList<IObjRef> extractObjRefList(Object objValue, IObjRefProvider oriProvider, IList<IObjRef> targetOriList, EntityCallback entityCallback);

	IObjRef getCreateObjRef(Object obj, IObjRefProvider oriProvider);

	IObjRef getCreateObjRef(Object obj, MergeHandle mergeHandle);

	IObjRef entityToObjRef(Object entity);

	IObjRef entityToObjRef(Object entity, boolean forceOri);

	IObjRef entityToObjRef(Object entity, byte idIndex);

	IObjRef entityToObjRef(Object entity, IEntityMetaData metaData);

	IObjRef entityToObjRef(Object entity, byte idIndex, IEntityMetaData metaData);

	IObjRef entityToObjRef(Object entity, byte idIndex, IEntityMetaData metaData, boolean forceOri);

	/**
	 * Returns all valid (key != null) references for the given entity.
	 * 
	 * @param entity
	 * @return
	 */
	IList<IObjRef> entityToAllObjRefs(Object id, Object version, Object[] primitives, IEntityMetaData metaData);

	/**
	 * Returns all valid (key != null) references for the given entity.
	 * 
	 * @param entity
	 * @return
	 */
	IList<IObjRef> entityToAllObjRefs(Object entity);

	/**
	 * Returns all valid (key != null) references for the given entity.
	 * 
	 * @param entity
	 * @param metaData
	 * @return
	 */
	IList<IObjRef> entityToAllObjRefs(Object entity, IEntityMetaData metaData);
}