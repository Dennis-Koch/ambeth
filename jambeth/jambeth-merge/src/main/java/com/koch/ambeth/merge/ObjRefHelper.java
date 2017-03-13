package com.koch.ambeth.merge;

import java.util.Iterator;
import java.util.List;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.cache.AbstractCacheValue;
import com.koch.ambeth.merge.compositeid.ICompositeIdFactory;
import com.koch.ambeth.merge.metadata.IObjRefFactory;
import com.koch.ambeth.merge.model.IDirectObjRef;
import com.koch.ambeth.merge.proxy.IEntityMetaDataHolder;
import com.koch.ambeth.merge.transfer.DirectObjRef;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.service.cache.model.ILoadContainer;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.EmptyList;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;

public class ObjRefHelper implements IObjRefHelper {
	@Autowired
	protected ICompositeIdFactory compositeIdFactory;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IObjRefFactory objRefFactory;

	@Override
	public IList<IObjRef> extractObjRefList(Object objValue, MergeHandle mergeHandle) {
		return extractObjRefList(objValue, mergeHandle, null);
	}

	@Override
	public IList<IObjRef> extractObjRefList(Object objValue, MergeHandle mergeHandle,
			IList<IObjRef> targetOriList) {
		return extractObjRefList(objValue, mergeHandle, null, null);
	}

	@Override
	public IList<IObjRef> extractObjRefList(Object objValue, MergeHandle mergeHandle,
			IList<IObjRef> targetOriList, EntityCallback entityCallback) {
		if (objValue == null) {
			if (targetOriList == null) {
				targetOriList = EmptyList.getInstance();
			}
			return targetOriList;
		}
		if (objValue.getClass().isArray()) {
			Object[] array = (Object[]) objValue;
			if (targetOriList == null) {
				targetOriList = new ArrayList<IObjRef>(array.length);
			}
			for (int a = 0, size = array.length; a < size; a++) {
				extractObjRefList(array[a], mergeHandle, targetOriList, entityCallback);
			}
		}
		else if (objValue instanceof List<?>) {
			List<?> list = (List<?>) objValue;
			if (targetOriList == null) {
				targetOriList = new ArrayList<IObjRef>(list.size());
			}
			for (int a = 0, size = list.size(); a < size; a++) {
				extractObjRefList(list.get(a), mergeHandle, targetOriList, entityCallback);
			}
		}
		else if (objValue instanceof Iterable<?>) {
			Iterator<?> objEnumerator = ((Iterable<?>) objValue).iterator();
			if (targetOriList == null) {
				targetOriList = new ArrayList<IObjRef>();
			}
			while (objEnumerator.hasNext()) {
				extractObjRefList(objEnumerator.next(), mergeHandle, targetOriList, entityCallback);
			}
		}
		else {
			if (targetOriList == null) {
				targetOriList = new ArrayList<IObjRef>(1);
			}
			getCreateORIs(objValue, mergeHandle, targetOriList, entityCallback);
		}
		return targetOriList;
	}

	@Override
	public IList<IObjRef> extractObjRefList(Object objValue, IObjRefProvider oriProvider,
			IList<IObjRef> targetOriList, EntityCallback entityCallback) {
		if (objValue == null) {
			if (targetOriList == null) {
				targetOriList = EmptyList.getInstance();
			}
			return targetOriList;
		}
		if (targetOriList == null) {
			targetOriList = new ArrayList<IObjRef>();
		}
		if (objValue.getClass().isArray()) {
			Object[] array = (Object[]) objValue;
			for (int a = 0, size = array.length; a < size; a++) {
				Object objItem = array[a];
				targetOriList.add(getCreateObjRef(objItem, oriProvider));
				if (entityCallback != null) {
					entityCallback.callback(objItem);
				}
			}
		}
		else if (objValue instanceof List<?>) {
			List<?> list = (List<?>) objValue;
			for (int a = 0, size = list.size(); a < size; a++) {
				Object objItem = list.get(a);
				targetOriList.add(getCreateObjRef(objItem, oriProvider));
				if (entityCallback != null) {
					entityCallback.callback(objItem);
				}
			}
		}
		else if (objValue instanceof Iterable<?>) {
			Iterator<?> objEnumerator = ((Iterable<?>) objValue).iterator();
			while (objEnumerator.hasNext()) {
				Object objItem = objEnumerator.next();
				targetOriList.add(getCreateObjRef(objItem, oriProvider));
				if (entityCallback != null) {
					entityCallback.callback(objItem);
				}
			}
		}
		else {
			targetOriList.add(getCreateObjRef(objValue, oriProvider));
			if (entityCallback != null) {
				entityCallback.callback(objValue);
			}
		}
		return targetOriList;
	}

	protected void getCreateORIs(Object obj, IObjRefProvider oriProvider, List<IObjRef> targetList) {
		IObjRef ori = getCreateObjRef(obj, oriProvider);
		targetList.add(ori);
	}

	@Override
	public IObjRef getCreateObjRef(Object obj, IObjRefProvider oriProvider) {
		if (obj == null) {
			return null;
		}
		if (obj instanceof IObjRef) {
			return (IObjRef) obj;
		}
		IEntityMetaData metaData = ((IEntityMetaDataHolder) obj).get__EntityMetaData();
		return oriProvider.getORI(obj, metaData);
	}

	@Override
	public IObjRef getCreateObjRef(Object obj, MergeHandle mergeHandle) {
		if (obj == null) {
			return null;
		}
		IObjRef ori = null;
		IMap<Object, IObjRef> objToOriDict = mergeHandle != null ? mergeHandle.objToOriDict : null;
		if (objToOriDict != null) {
			ori = objToOriDict.get(obj);
		}
		if (ori != null) {
			return ori;
		}
		if (obj instanceof IObjRef) {
			return (IObjRef) obj;
		}
		if (!(obj instanceof IEntityMetaDataHolder)) {
			return null;
		}
		IEntityMetaData metaData = ((IEntityMetaDataHolder) obj).get__EntityMetaData();

		Object keyValue;
		if (obj instanceof AbstractCacheValue) {
			keyValue = ((AbstractCacheValue) obj).getId();
		}
		else {
			keyValue = metaData.getIdMember().getValue(obj, false);
		}
		if (keyValue == null || mergeHandle != null && mergeHandle.isHandleExistingIdAsNewId()) {
			IDirectObjRef dirOri = new DirectObjRef(metaData.getEntityType(), obj);
			if (keyValue != null) {
				dirOri.setId(keyValue);
			}
			ori = dirOri;
		}
		else {
			Object version;
			if (obj instanceof AbstractCacheValue) {
				version = ((AbstractCacheValue) obj).getVersion();
			}
			else {
				Member versionMember = metaData.getVersionMember();
				version = versionMember != null ? versionMember.getValue(obj, true) : null;
			}
			ori = objRefFactory.createObjRef(metaData.getEntityType(), ObjRef.PRIMARY_KEY_INDEX, keyValue,
					version);
		}
		if (objToOriDict != null) {
			objToOriDict.put(obj, ori);

			IMap<IObjRef, Object> oriToObjDict = mergeHandle != null ? mergeHandle.oriToObjDict : null;
			if (oriToObjDict != null) {
				oriToObjDict.putIfNotExists(ori, obj);
			}
		}
		return ori;
	}

	protected void getCreateORIs(Object obj, MergeHandle mergeHandle, List<IObjRef> targetList,
			EntityCallback entityCallback) {
		IObjRef ori = getCreateObjRef(obj, mergeHandle);
		targetList.add(ori);
		if (entityCallback != null) {
			entityCallback.callback(obj);
		}

	}

	@Override
	public IObjRef entityToObjRef(Object entity) {
		return entityToObjRef(entity, ObjRef.PRIMARY_KEY_INDEX,
				((IEntityMetaDataHolder) entity).get__EntityMetaData());
	}

	@Override
	public IObjRef entityToObjRef(Object entity, boolean forceOri) {
		return entityToObjRef(entity, ObjRef.PRIMARY_KEY_INDEX,
				((IEntityMetaDataHolder) entity).get__EntityMetaData(), forceOri);
	}

	@Override
	public IObjRef entityToObjRef(Object entity, int idIndex) {
		return entityToObjRef(entity, idIndex, ((IEntityMetaDataHolder) entity).get__EntityMetaData());
	}

	@Override
	public IObjRef entityToObjRef(Object entity, IEntityMetaData metaData) {
		return entityToObjRef(entity, ObjRef.PRIMARY_KEY_INDEX, metaData);
	}

	@Override
	public IObjRef entityToObjRef(Object entity, int idIndex, IEntityMetaData metaData) {
		return entityToObjRef(entity, idIndex, metaData, false);
	}

	@Override
	public IObjRef entityToObjRef(Object entity, int idIndex, IEntityMetaData metaData,
			boolean forceOri) {
		Object id;
		Object version;
		Member versionMember = metaData.getVersionMember();
		if (entity instanceof AbstractCacheValue) {
			AbstractCacheValue cacheValue = (AbstractCacheValue) entity;
			if (idIndex == ObjRef.PRIMARY_KEY_INDEX) {
				id = cacheValue.getId();
			}
			else {
				id = compositeIdFactory.createIdFromPrimitives(metaData, idIndex, cacheValue);
			}
			version = cacheValue.getVersion();
		}
		else if (entity instanceof ILoadContainer) {
			ILoadContainer lc = (ILoadContainer) entity;
			if (idIndex == ObjRef.PRIMARY_KEY_INDEX) {
				id = lc.getReference().getId();
			}
			else {
				id = compositeIdFactory.createIdFromPrimitives(metaData, idIndex, lc.getPrimitives());
			}
			version = lc.getReference().getVersion();
		}
		else {
			id = metaData.getIdMemberByIdIndex(idIndex).getValue(entity, false);
			version = versionMember != null ? versionMember.getValue(entity, false) : null;
		}
		IObjRef ori;

		if (id != null || forceOri) {
			ori = objRefFactory.createObjRef(metaData.getEntityType(), idIndex, id, version);
		}
		else {
			ori = new DirectObjRef(metaData.getEntityType(), entity);
		}

		return ori;
	}

	@Override
	public IList<IObjRef> entityToAllObjRefs(Object id, Object version, Object[] primitives,
			IEntityMetaData metaData) {
		int alternateIdCount = metaData.getAlternateIdCount();

		ArrayList<IObjRef> allOris = new ArrayList<IObjRef>();

		Class<?> entityType = metaData.getEntityType();
		// Convert id and version to the correct metadata type
		if (id != null) {
			allOris.add(objRefFactory.createObjRef(entityType, ObjRef.PRIMARY_KEY_INDEX, id, version));
		}
		if (alternateIdCount > 0) {
			Member[] alternateIdMembers = metaData.getAlternateIdMembers();

			Member[] primitiveMembers = metaData.getPrimitiveMembers();
			for (int a = primitiveMembers.length; a-- > 0;) {
				Member primitiveMember = primitiveMembers[a];
				for (int b = alternateIdMembers.length; b-- > 0;) {
					Member alternateIdMember = alternateIdMembers[b];
					if (alternateIdMember == primitiveMember) {
						Object alternateId = primitives[a];
						if (alternateId == null) {
							// The current member is an alternate id. But alternate ids are not mandatorily
							// not-null
							// If they are not specified, they are simply ignored
							continue;
						}
						allOris.add(objRefFactory.createObjRef(entityType, b, alternateId, version));
						break;
					}
				}
			}
		}
		return allOris;
	}

	@Override
	public IList<IObjRef> entityToAllObjRefs(Object entity) {
		if (entity instanceof IEntityMetaDataHolder) {
			return entityToAllObjRefs(entity, ((IEntityMetaDataHolder) entity).get__EntityMetaData());
		}
		ILoadContainer lc = (ILoadContainer) entity;
		return entityToAllObjRefs(entity,
				entityMetaDataProvider.getMetaData(lc.getReference().getRealType()));
	}

	@Override
	public IList<IObjRef> entityToAllObjRefs(Object entity, IEntityMetaData metaData) {
		int alternateIdCount = metaData.getAlternateIdCount();
		ArrayList<IObjRef> allOris = new ArrayList<IObjRef>();

		IObjRef ref = entityToObjRef(entity, ObjRef.PRIMARY_KEY_INDEX, metaData);
		if (ref.getId() != null) {
			allOris.add(ref);
		}
		// Sorted list may later reduce search cost
		for (int i = 0; i < alternateIdCount; i++) {
			ref = entityToObjRef(entity, (byte) i, metaData);
			if (ref.getId() != null) {
				allOris.add(ref);
			}
		}

		return allOris;
	}
}
