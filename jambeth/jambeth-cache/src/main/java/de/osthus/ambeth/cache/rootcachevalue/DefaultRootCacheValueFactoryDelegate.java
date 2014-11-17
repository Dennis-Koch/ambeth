package de.osthus.ambeth.cache.rootcachevalue;

import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastConstructor;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.merge.model.IEntityMetaData;

public class DefaultRootCacheValueFactoryDelegate extends RootCacheValueFactoryDelegate
{
	protected final FastConstructor constructor;

	public DefaultRootCacheValueFactoryDelegate()
	{
		FastClass fastClass = FastClass.create(Thread.currentThread().getContextClassLoader(), DefaultRootCacheValue.class);
		try
		{
			constructor = fastClass.getConstructor(DefaultRootCacheValue.class.getConstructor(IEntityMetaData.class));
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public RootCacheValue createRootCacheValue(IEntityMetaData metaData)
	{
		try
		{
			return (RootCacheValue) constructor.newInstance(new Object[] { metaData });
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
