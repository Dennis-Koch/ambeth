package com.koch.ambeth.cache.rootcachevalue;

import com.koch.ambeth.ioc.accessor.AccessorClassLoader;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastConstructor;

public class DefaultRootCacheValueFactoryDelegate extends RootCacheValueFactoryDelegate {

	protected final FastConstructor constructor;

	public DefaultRootCacheValueFactoryDelegate() {
		AccessorClassLoader classLoader = AccessorClassLoader.get(DefaultRootCacheValue.class);
		FastClass fastClass = FastClass.create(classLoader, DefaultRootCacheValue.class);
		try {
			constructor = fastClass
					.getConstructor(DefaultRootCacheValue.class.getConstructor(IEntityMetaData.class));
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public RootCacheValue createRootCacheValue(IEntityMetaData metaData) {
		try {
			return (RootCacheValue) constructor.newInstance(new Object[] {metaData});
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
