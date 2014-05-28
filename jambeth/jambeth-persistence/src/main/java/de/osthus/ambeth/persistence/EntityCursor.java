package de.osthus.ambeth.persistence;

import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class EntityCursor<T> extends BasicEnumerator<T> implements IEntityCursor<T>
{
	@SuppressWarnings("unused")
	@LogInstance(EntityCursor.class)
	private ILogger log;

	protected IVersionCursor cursor;

	protected Class<T> entityType;

	protected ICache cache;

	protected IServiceUtil serviceUtil;

	protected T current;

	public EntityCursor(IVersionCursor cursor, Class<T> entityType, ICache cache)
	{
		this.cursor = cursor;
		this.entityType = entityType;
		this.cache = cache;
	}

	public EntityCursor(IVersionCursor cursor, Class<T> entityType, IServiceUtil serviceUtil)
	{
		this.cursor = cursor;
		this.entityType = entityType;
		this.serviceUtil = serviceUtil;
	}

	@Override
	public T getCurrent()
	{
		if (this.current == null)
		{
			IVersionItem item = this.cursor.getCurrent();
			if (item == null)
			{
				return null;
			}
			else
			{
				if (this.serviceUtil != null)
				{
					this.current = this.serviceUtil.loadObject(this.entityType, item);
				}
				else
				{
					this.current = this.cache.getObject(this.entityType, item);
				}
			}
		}
		return this.current;
	}

	@Override
	public boolean moveNext()
	{
		this.current = null;
		return this.cursor.moveNext();
	}

	@Override
	public void dispose()
	{
		this.cursor.dispose();
		this.cursor = null;
		this.cache = null;
		this.current = null;
	}
}
