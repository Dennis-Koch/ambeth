package com.koch.ambeth.persistence;

/*-
 * #%L
 * jambeth-persistence
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
import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.metadata.IObjRefFactory;
import com.koch.ambeth.merge.metadata.IPreparedObjRefFactory;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.persistence.api.IDatabaseMetaData;
import com.koch.ambeth.persistence.api.ILinkCursor;
import com.koch.ambeth.persistence.api.ILinkCursorItem;
import com.koch.ambeth.persistence.api.ITableMetaData;
import com.koch.ambeth.query.persistence.IVersionCursor;
import com.koch.ambeth.query.persistence.IVersionItem;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IList;

public class ServiceUtil implements IServiceUtil
{
	@Autowired
	protected IEntityLoader entityLoader;

	@Autowired
	protected ICache cache;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected IDatabaseMetaData databaseMetaData;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IObjRefFactory objRefFactory;

	@SuppressWarnings("unchecked")
	@Override
	public <T> void loadObjectsIntoCollection(List<T> targetEntities, Class<T> entityType, IVersionCursor cursor)
	{
		if (cursor == null)
		{
			return;
		}
		try
		{
			IConversionHelper conversionHelper = this.conversionHelper;
			ITableMetaData table = databaseMetaData.getTableByType(entityType);
			Class<?> idType = table.getIdField().getFieldType();
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
			Class<?> idTypeOfObject = metaData.getIdMember().getRealType();
			Class<?> versionTypeOfObject = metaData.getVersionMember() != null ? metaData.getVersionMember().getRealType() : null;
			ArrayList<IObjRef> objRefs = new ArrayList<IObjRef>();
			while (cursor.moveNext())
			{
				IVersionItem item = cursor.getCurrent();
				Object id = conversionHelper.convertValueToType(idType, item.getId());
				// INTENTIONALLY converting the id in 2 steps: first to the fieldType, then to the idTypeOfObject
				// this is because of the fact that the idTypeOfObject may be an Object.class which does not convert the id correctly by itself
				id = conversionHelper.convertValueToType(idTypeOfObject, id);
				Object version = conversionHelper.convertValueToType(versionTypeOfObject, item.getVersion());
				objRefs.add(new ObjRef(entityType, ObjRef.PRIMARY_KEY_INDEX, id, version));
			}
			IList<Object> objects = cache.getObjects(objRefs, CacheDirective.none());
			for (int a = 0, size = objects.size(); a < size; a++)
			{
				targetEntities.add((T) objects.get(a));
			}
		}
		finally
		{
			cursor.dispose();
			cursor = null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T loadObject(Class<T> entityType, IVersionItem item)
	{
		if (item == null)
		{
			return null;
		}
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
		Class<?> idTypeOfObject = metaData.getIdMember().getRealType();
		Class<?> versionTypeOfObject = metaData.getVersionMember() != null ? metaData.getVersionMember().getRealType() : null;

		Object id = conversionHelper.convertValueToType(idTypeOfObject, item.getId());
		Object version = conversionHelper.convertValueToType(versionTypeOfObject, item.getVersion());
		return (T) cache.getObject(new ObjRef(entityType, ObjRef.PRIMARY_KEY_INDEX, id, version), CacheDirective.none());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> void loadObjects(java.util.List<T> targetEntities, java.lang.Class<T> entityType, ILinkCursor cursor)
	{
		if (cursor == null)
		{
			return;
		}
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
		Class<?> idTypeOfObject = metaData.getIdMember().getRealType();
		ArrayList<IObjRef> objRefs = new ArrayList<IObjRef>();
		try
		{
			while (cursor.moveNext())
			{
				ILinkCursorItem item = cursor.getCurrent();
				Object toId = conversionHelper.convertValueToType(idTypeOfObject, item.getToId());
				objRefs.add(new ObjRef(entityType, ObjRef.PRIMARY_KEY_INDEX, toId, null));
			}
			IList<Object> objects = cache.getObjects(objRefs, CacheDirective.none());
			for (int a = 0, size = objects.size(); a < size; a++)
			{
				targetEntities.add((T) objects.get(a));
			}
		}
		finally
		{
			cursor.dispose();
			cursor = null;
		}
	}

	@Override
	public IList<IObjRef> loadObjRefs(Class<?> entityType, int idIndex, IVersionCursor cursor)
	{
		try
		{
			ArrayList<IObjRef> objRefs = new ArrayList<IObjRef>();
			IPreparedObjRefFactory preparedObjRefFactory = objRefFactory.prepareObjRefFactory(entityType, idIndex);
			while (cursor.moveNext())
			{
				IVersionItem item = cursor.getCurrent();
				objRefs.add(preparedObjRefFactory.createObjRef(item.getId(idIndex), item.getVersion()));
			}
			return objRefs;
		}
		finally
		{
			cursor.dispose();
		}
	}
}
