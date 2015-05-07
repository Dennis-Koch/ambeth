package de.osthus.ambeth.cache;

import java.util.List;

import de.osthus.ambeth.cache.transfer.ServiceResult;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.metadata.IPreparedObjRefFactory;
import de.osthus.ambeth.persistence.ILinkCursor;
import de.osthus.ambeth.persistence.ILinkCursorItem;
import de.osthus.ambeth.persistence.IVersionCursor;
import de.osthus.ambeth.persistence.IVersionItem;
import de.osthus.ambeth.persistence.ServiceUtil;

public class CacheServiceUtil extends ServiceUtil
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IServiceResultHolder oriResultHolder;

	@Override
	public <T> void loadObjectsIntoCollection(List<T> targetEntities, Class<T> entityType, IVersionCursor cursor)
	{
		if (!oriResultHolder.isExpectServiceResult())
		{
			super.loadObjectsIntoCollection(targetEntities, entityType, cursor);
			return;
		}

		IList<IObjRef> objRefs;
		if (cursor != null)
		{
			objRefs = loadObjRefs(entityType, ObjRef.PRIMARY_KEY_INDEX, cursor);
		}
		else
		{
			objRefs = new ArrayList<IObjRef>();
		}
		oriResultHolder.setServiceResult(new ServiceResult(objRefs));
	}

	@Override
	public <T> T loadObject(Class<T> entityType, IVersionItem item)
	{
		if (!oriResultHolder.isExpectServiceResult())
		{
			return super.loadObject(entityType, item);
		}
		ArrayList<IObjRef> objRefs = new ArrayList<IObjRef>();
		if (item != null)
		{
			objRefs.add(objRefFactory.createObjRef(entityType, ObjRef.PRIMARY_KEY_INDEX, item.getId(), item.getVersion()));
		}
		oriResultHolder.setServiceResult(new ServiceResult(objRefs));
		return null;
	}

	@Override
	public <T> void loadObjects(List<T> targetEntities, Class<T> entityType, ILinkCursor cursor)
	{
		if (!oriResultHolder.isExpectServiceResult())
		{
			super.loadObjects(targetEntities, entityType, cursor);
			return;
		}
		ArrayList<IObjRef> objRefs = new ArrayList<IObjRef>();
		if (cursor != null)
		{
			try
			{
				IPreparedObjRefFactory preparedObjRefFactory = objRefFactory.prepareObjRefFactory(entityType, cursor.getToIdIndex());
				while (cursor.moveNext())
				{
					ILinkCursorItem item = cursor.getCurrent();
					objRefs.add(preparedObjRefFactory.createObjRef(item.getToId(), null));
				}
			}
			finally
			{
				cursor.dispose();
				cursor = null;
			}
		}
		entityLoader.fillVersion(objRefs);
		oriResultHolder.setServiceResult(new ServiceResult(objRefs));
	}
}
