package de.osthus.ambeth.cache.rootcachevalue;

import java.lang.reflect.Constructor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastConstructor;
import de.osthus.ambeth.bytecode.IBytecodeEnhancer;
import de.osthus.ambeth.bytecode.IBytecodePrinter;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class RootCacheValueTypeProvider implements IRootCacheValueTypeProvider
{
	protected static final FastConstructor ci;

	static
	{
		try
		{
			FastClass fastClass = FastClass.create(Thread.currentThread().getContextClassLoader(), DefaultRootCacheValue.class);
			ci = fastClass.getConstructor(DefaultRootCacheValue.class.getConstructor(Class.class));
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired(optional = true)
	protected IBytecodeEnhancer bytecodeEnhancer;

	@Autowired(optional = true)
	protected IBytecodePrinter bytecodePrinter;

	protected final HashMap<Class<?>, FastConstructor> typeToConstructorMap = new HashMap<Class<?>, FastConstructor>();

	protected final Lock writeLock = new ReentrantLock();

	@Override
	public <V> FastConstructor getRootCacheValueType(Class<V> entityType)
	{
		if (bytecodeEnhancer == null)
		{
			return ci;
		}
		FastConstructor constructor = typeToConstructorMap.get(entityType);
		if (constructor != null)
		{
			return constructor;
		}
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			// concurrent thread might have been faster
			constructor = typeToConstructorMap.get(entityType);
			if (constructor != null)
			{
				return constructor;
			}
			Class<?> enhancedType = null;
			try
			{
				enhancedType = bytecodeEnhancer.getEnhancedType(RootCacheValue.class, new RootCacheValueEnhancementHint(entityType));
				if (enhancedType == RootCacheValue.class)
				{
					// Nothing has been enhanced
					constructor = ci;
				}
				else
				{
					Constructor<?> ci = enhancedType.getConstructor(Class.class);
					FastClass fastClass = FastClass.create(enhancedType.getClassLoader(), enhancedType);
					constructor = fastClass.getConstructor(ci);
				}
			}
			catch (Throwable e)
			{
				if (log.isWarnEnabled())
				{
					log.warn(bytecodePrinter.toPrintableBytecode(enhancedType), e);
				}
				// something serious happened during enhancement: continue with a fallback
				constructor = ci;
			}
			typeToConstructorMap.put(entityType, constructor);
			return constructor;
		}
		finally
		{
			writeLock.unlock();
		}
	}
}
