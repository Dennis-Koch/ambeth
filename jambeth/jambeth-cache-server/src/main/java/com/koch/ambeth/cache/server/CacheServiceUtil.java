package com.koch.ambeth.cache.server;

/*-
 * #%L
 * jambeth-cache-server
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

import java.util.List;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.metadata.IPreparedObjRefFactory;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.persistence.ServiceUtil;
import com.koch.ambeth.persistence.api.ILinkCursor;
import com.koch.ambeth.persistence.api.ILinkCursorItem;
import com.koch.ambeth.query.persistence.IVersionCursor;
import com.koch.ambeth.query.persistence.IVersionItem;
import com.koch.ambeth.service.cache.IServiceResultHolder;
import com.koch.ambeth.service.cache.transfer.ServiceResult;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IList;

public class CacheServiceUtil extends ServiceUtil {
	@Autowired
	protected IServiceResultHolder oriResultHolder;

	@Override
	public <T> void loadObjectsIntoCollection(List<T> targetEntities, Class<T> entityType,
			IVersionCursor cursor) {
		if (!oriResultHolder.isExpectServiceResult()) {
			super.loadObjectsIntoCollection(targetEntities, entityType, cursor);
			return;
		}

		IList<IObjRef> objRefs;
		if (cursor != null) {
			objRefs = loadObjRefs(entityType, ObjRef.PRIMARY_KEY_INDEX, cursor);
		}
		else {
			objRefs = new ArrayList<>();
		}
		oriResultHolder.setServiceResult(new ServiceResult(objRefs));
	}

	@Override
	public <T> T loadObject(Class<T> entityType, IVersionItem item) {
		if (!oriResultHolder.isExpectServiceResult()) {
			return super.loadObject(entityType, item);
		}
		ArrayList<IObjRef> objRefs = new ArrayList<>();
		if (item != null) {
			objRefs.add(objRefFactory.createObjRef(entityType, ObjRef.PRIMARY_KEY_INDEX, item.getId(),
					item.getVersion()));
		}
		oriResultHolder.setServiceResult(new ServiceResult(objRefs));
		return null;
	}

	@Override
	public <T> void loadObjects(List<T> targetEntities, Class<T> entityType, ILinkCursor cursor) {
		if (!oriResultHolder.isExpectServiceResult()) {
			super.loadObjects(targetEntities, entityType, cursor);
			return;
		}
		ArrayList<IObjRef> objRefs = new ArrayList<>();
		if (cursor != null) {
			try {
				IPreparedObjRefFactory preparedObjRefFactory = objRefFactory
						.prepareObjRefFactory(entityType, cursor.getToIdIndex());
				for (ILinkCursorItem item : cursor) {
					objRefs.add(preparedObjRefFactory.createObjRef(item.getToId(), null));
				}
			}
			finally {
				cursor.dispose();
				cursor = null;
			}
		}
		entityLoader.fillVersion(objRefs);
		oriResultHolder.setServiceResult(new ServiceResult(objRefs));
	}
}
