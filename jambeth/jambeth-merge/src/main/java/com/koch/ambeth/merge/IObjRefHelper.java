package com.koch.ambeth.merge;

/*-
 * #%L
 * jambeth-merge
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.collections.IList;

public interface IObjRefHelper {
	IList<IObjRef> extractObjRefList(Object objValue, MergeHandle mergeHandle);

	IList<IObjRef> extractObjRefList(Object objValue, MergeHandle mergeHandle,
			IList<IObjRef> targetOriList);

	IList<IObjRef> extractObjRefList(Object objValue, MergeHandle mergeHandle,
			IList<IObjRef> targetOriList, EntityCallback entityCallback);

	IList<IObjRef> extractObjRefList(Object objValue, IObjRefProvider oriProvider,
			IList<IObjRef> targetOriList, EntityCallback entityCallback);

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
	 * @param id Primary ID
	 * @param version Entity version
	 * @param primitives Array of all primitive values
	 * @param metaData Meta data of the described entity
	 * @return All valid ORIs
	 */
	IList<IObjRef> entityToAllObjRefs(Object id, Object version, Object[] primitives,
			IEntityMetaData metaData);

	/**
	 * Returns all valid (key != null) references for the given entity.
	 *
	 * @param entity Entity to build ORIS for
	 * @return All valid ORIs
	 */
	IList<IObjRef> entityToAllObjRefs(Object entity);

	/**
	 * Returns all valid (key != null) references for the given entity.
	 *
	 * @param entity Entity to build ORIS for
	 * @param metaData Meta data of the given entity
	 * @return All valid ORIs
	 */
	IList<IObjRef> entityToAllObjRefs(Object entity, IEntityMetaData metaData);
}
