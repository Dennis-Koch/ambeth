package de.osthus.ambeth.util;

import de.osthus.ambeth.cache.model.ILoadContainer;
import de.osthus.ambeth.ioc.IDisposableBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.threadlocal.Forkable;
import de.osthus.ambeth.ioc.threadlocal.IForkProcessor;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.metadata.IObjRefFactory;
import de.osthus.ambeth.threading.SensitiveThreadLocal;

public class AlreadyLoadedCache implements IAlreadyLoadedCache, IDisposableBean, IThreadLocalCleanupBean
{
	public static class ForkProcessor implements IForkProcessor
	{
		@Override
		public Object resolveOriginalValue(Object bean, String fieldName, ThreadLocal<?> fieldValueTL)
		{
			return ((AlreadyLoadedCache) bean).get();
		}

		@Override
		public Object createForkedValue(Object value)
		{
			return ((AlreadyLoadedCacheIntern) value).createChild();
		}

		@Override
		public void returnForkedValue(Object value, Object forkedValue)
		{
			((AlreadyLoadedCacheIntern) value).applyContentFrom((AlreadyLoadedCacheIntern) forkedValue);
		}
	}

	@LogInstance
	private ILogger log;

	@Autowired
	protected IObjRefFactory objRefFactory;

	@Forkable(processor = ForkProcessor.class)
	protected final ThreadLocal<AlreadyLoadedCacheIntern> alreadyLoadedCacheTL = new SensitiveThreadLocal<AlreadyLoadedCacheIntern>();

	protected IAlreadyLoadedCache get()
	{
		AlreadyLoadedCacheIntern alreadyLoadedCache = alreadyLoadedCacheTL.get();
		if (alreadyLoadedCache == null)
		{
			alreadyLoadedCache = new AlreadyLoadedCacheIntern(log, objRefFactory);
			alreadyLoadedCacheTL.set(alreadyLoadedCache);
		}
		return alreadyLoadedCache;
	}

	@Override
	public void cleanupThreadLocal()
	{
		alreadyLoadedCacheTL.remove();
	}

	@Override
	public IAlreadyLoadedCache getCurrent()
	{
		return get();
	}

	@Override
	public void destroy() throws Throwable
	{
		clear();
	}

	@Override
	public void clear()
	{
		getCurrent().clear();
	}

	@Override
	public int size()
	{
		return getCurrent().size();

	}

	@Override
	public IAlreadyLoadedCache snapshot()
	{
		return getCurrent().snapshot();
	}

	@Override
	public void copyTo(IAlreadyLoadedCache targetAlCache)
	{
		getCurrent().copyTo(targetAlCache);
	}

	@Override
	public ILoadContainer getObject(byte idNameIndex, Object id, Class<?> type)
	{
		return getCurrent().getObject(idNameIndex, id, type);
	}

	@Override
	public ILoadContainer getObject(IdTypeTuple idTypeTuple)
	{
		return getCurrent().getObject(idTypeTuple);
	}

	@Override
	public IObjRef getRef(byte idNameIndex, Object id, Class<?> type)
	{
		return getCurrent().getRef(idNameIndex, id, type);
	}

	@Override
	public IObjRef getRef(IdTypeTuple idTypeTuple)
	{
		return getCurrent().getRef(idTypeTuple);
	}

	@Override
	public void add(byte idNameIndex, Object id, Class<?> type, IObjRef objRef)
	{
		getCurrent().add(idNameIndex, id, type, objRef);
	}

	@Override
	public void add(IdTypeTuple idTypeTuple, IObjRef objRef)
	{
		getCurrent().add(idTypeTuple, objRef);
	}

	@Override
	public void add(byte idNameIndex, Object persistentId, Class<?> type, IObjRef objRef, ILoadContainer loadContainer)
	{
		getCurrent().add(idNameIndex, persistentId, type, objRef, loadContainer);
	}

	@Override
	public void add(IdTypeTuple idTypeTuple, IObjRef objRef, ILoadContainer loadContainer)
	{
		getCurrent().add(idTypeTuple, objRef, loadContainer);
	}

	@Override
	public void replace(byte idNameIndex, Object persistentId, Class<?> type, ILoadContainer loadContainer)
	{
		getCurrent().replace(idNameIndex, persistentId, type, loadContainer);
	}

	@Override
	public void replace(IdTypeTuple idTypeTuple, ILoadContainer loadContainer)
	{
		getCurrent().replace(idTypeTuple, loadContainer);
	}
}
