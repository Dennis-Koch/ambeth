package com.koch.ambeth.merge;

import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.collections.IList;

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

	IObjRef entityToObjRef(Object entity, int idIndex);

	IObjRef entityToObjRef(Object entity, IEntityMetaData metaData);

	IObjRef entityToObjRef(Object entity, int idIndex, IEntityMetaData metaData);

	IObjRef entityToObjRef(Object entity, int idIndex, IEntityMetaData metaData, boolean forceOri);

	/**
	 * Returns all valid (key != null) references for the given values.
	 * 
	 * @param id
	 *            Primary ID
	 * @param version
	 *            Entity version
	 * @param primitives
	 *            Array of all primitive values
	 * @param metaData
	 *            Meta data of the described entity
	 * @return All valid ORIs
	 */
	IList<IObjRef> entityToAllObjRefs(Object id, Object version, Object[] primitives, IEntityMetaData metaData);

	/**
	 * Returns all valid (key != null) references for the given entity.
	 * 
	 * @param entity
	 *            Entity to build ORIS for
	 * @return All valid ORIs
	 */
	IList<IObjRef> entityToAllObjRefs(Object entity);

	/**
	 * Returns all valid (key != null) references for the given entity.
	 * 
	 * @param entity
	 *            Entity to build ORIS for
	 * @param metaData
	 *            Meta data of the given entity
	 * @return All valid ORIs
	 */
	IList<IObjRef> entityToAllObjRefs(Object entity, IEntityMetaData metaData);
}