package de.osthus.ambeth.persistence;

import java.util.List;

import de.osthus.ambeth.cache.CacheDirective;
import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.ParamChecker;

public class ServiceUtil implements IServiceUtil, IInitializingBean
{
	protected IEntityLoader entityLoader;

	protected ICache cache;

	protected IConversionHelper conversionHelper;

	protected IDatabase database;

	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Override
	public void afterPropertiesSet()
	{
		ParamChecker.assertNotNull(cache, "cache");
		ParamChecker.assertNotNull(conversionHelper, "conversionHelper");
		ParamChecker.assertNotNull(database, "database");
		ParamChecker.assertNotNull(entityLoader, "entityLoader");
		ParamChecker.assertNotNull(entityMetaDataProvider, "entityMetaDataProvider");
	}

	public void setConversionHelper(IConversionHelper conversionHelper)
	{
		this.conversionHelper = conversionHelper;
	}

	public void setDatabase(IDatabase database)
	{
		this.database = database;
	}

	public void setEntityLoader(IEntityLoader entityLoader)
	{
		this.entityLoader = entityLoader;
	}

	public void setEntityMetaDataProvider(IEntityMetaDataProvider entityMetaDataProvider)
	{
		this.entityMetaDataProvider = entityMetaDataProvider;
	}

	public void setCache(ICache cache)
	{
		this.cache = cache;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> void loadObjectsIntoCollection(List<T> targetEntities, Class<T> entityType, IVersionCursor cursor)
	{
		if (cursor == null)
		{
			return;
		}
		IConversionHelper conversionHelper = this.conversionHelper;
		ITable table = database.getTableByType(entityType);
		Class<?> idType = table.getIdField().getFieldType();
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
		Class<?> idTypeOfObject = metaData.getIdMember().getRealType();
		Class<?> versionTypeOfObject = metaData.getVersionMember() != null ? metaData.getVersionMember().getRealType() : null;
		ArrayList<IObjRef> objRefs = new ArrayList<IObjRef>();
		try
		{
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
			IList<Object> objects = this.cache.getObjects(objRefs, CacheDirective.none());
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
			return (T) this.cache.getObject(new ObjRef(entityType, ObjRef.PRIMARY_KEY_INDEX, id, version), CacheDirective.none());
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
			IList<Object> objects = this.cache.getObjects(objRefs, CacheDirective.none());
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
}
