package com.koch.ambeth.cache.collections;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.koch.ambeth.ioc.accessor.IAccessorTypeProvider;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.bytecode.IBytecodeEnhancer;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.collections.Tuple2KeyHashMap;

public class CacheMapEntryTypeProvider implements ICacheMapEntryTypeProvider
{
	protected static final ICacheMapEntryFactory ci = new DefaultCacheMapEntryFactory();

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired(optional = true)
	protected IBytecodeEnhancer bytecodeEnhancer;

	@Autowired
	protected IAccessorTypeProvider accessorTypeProvider;

	protected final Tuple2KeyHashMap<Class<?>, Byte, ICacheMapEntryFactory> typeToConstructorMap = new Tuple2KeyHashMap<Class<?>, Byte, ICacheMapEntryFactory>();

	protected final Lock writeLock = new ReentrantLock();

	@Override
	public ICacheMapEntryFactory getCacheMapEntryType(Class<?> entityType, byte idIndex)
	{
		if (bytecodeEnhancer == null)
		{
			return ci;
		}
		ICacheMapEntryFactory factory = typeToConstructorMap.get(entityType, Byte.valueOf(idIndex));
		if (factory != null)
		{
			return factory;
		}
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			// concurrent thread might have been faster
			factory = typeToConstructorMap.get(entityType, Byte.valueOf(idIndex));
			if (factory != null)
			{
				return factory;
			}
			try
			{
				Class<?> enhancedType = bytecodeEnhancer.getEnhancedType(CacheMapEntry.class, new CacheMapEntryEnhancementHint(entityType, idIndex));
				if (enhancedType == CacheMapEntry.class)
				{
					// Nothing has been enhanced
					factory = ci;
				}
				else
				{
					factory = accessorTypeProvider.getConstructorType(ICacheMapEntryFactory.class, enhancedType);
				}
			}
			catch (Throwable e)
			{
				if (log.isWarnEnabled())
				{
					log.warn(e);
				}
				// something serious happened during enhancement: continue with a fallback
				factory = ci;
			}
			typeToConstructorMap.put(entityType, Byte.valueOf(idIndex), factory);
			return factory;
		}
		finally
		{
			writeLock.unlock();
		}
	}
}
