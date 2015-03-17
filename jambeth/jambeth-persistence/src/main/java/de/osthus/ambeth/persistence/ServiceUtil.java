package de.osthus.ambeth.persistence;

import java.util.List;

import de.osthus.ambeth.cache.CacheDirective;
import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.metadata.IObjRefFactory;
import de.osthus.ambeth.metadata.IPreparedObjRefFactory;
import de.osthus.ambeth.util.IConversionHelper;

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
		try
		{
			Object id = conversionHelper.convertValueToType(idTypeOfObject, item.getId());
			Object version = conversionHelper.convertValueToType(versionTypeOfObject, item.getVersion());
			return (T) cache.getObject(new ObjRef(entityType, ObjRef.PRIMARY_KEY_INDEX, id, version), CacheDirective.none());
		}
		finally
		{
			item.dispose();
		}
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
