package de.osthus.ambeth.cache.rootcachevalue;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.accessor.IAccessorTypeProvider;
import de.osthus.ambeth.bytecode.IBytecodeEnhancer;
import de.osthus.ambeth.bytecode.IBytecodePrinter;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.model.IEntityMetaData;

public class RootCacheValueFactory implements IRootCacheValueFactory
{
	protected static final RootCacheValueFactoryDelegate rcvFactory = new DefaultRootCacheValueFactoryDelegate();

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired(optional = true)
	protected IAccessorTypeProvider accessorTypeProvider;

	@Autowired(optional = true)
	protected IBytecodeEnhancer bytecodeEnhancer;

	@Autowired(optional = true)
	protected IBytecodePrinter bytecodePrinter;

	protected final HashMap<IEntityMetaData, RootCacheValueFactoryDelegate> typeToConstructorMap = new HashMap<IEntityMetaData, RootCacheValueFactoryDelegate>();

	protected final Lock writeLock = new ReentrantLock();

	@Override
	public RootCacheValue createRootCacheValue(IEntityMetaData metaData)
	{
		RootCacheValueFactoryDelegate rootCacheValueFactory = typeToConstructorMap.get(metaData);
		if (rootCacheValueFactory != null)
		{
			return rootCacheValueFactory.createRootCacheValue(metaData);
		}
		if (bytecodeEnhancer == null)
		{
			return rcvFactory.createRootCacheValue(metaData);
		}
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			// concurrent thread might have been faster
			rootCacheValueFactory = typeToConstructorMap.get(metaData);
			if (rootCacheValueFactory == null)
			{
				rootCacheValueFactory = createDelegate(metaData);
			}
		}
		finally
		{
			writeLock.unlock();
		}
		return rootCacheValueFactory.createRootCacheValue(metaData);
	}

	protected RootCacheValueFactoryDelegate createDelegate(IEntityMetaData metaData)
	{
		RootCacheValueFactoryDelegate rootCacheValueFactory;
		Class<?> enhancedType = null;
		try
		{
			enhancedType = bytecodeEnhancer.getEnhancedType(RootCacheValue.class, new RootCacheValueEnhancementHint(metaData.getEntityType()));
			if (enhancedType == RootCacheValue.class)
			{
				// Nothing has been enhanced
				rootCacheValueFactory = rcvFactory;
			}
			else
			{
				rootCacheValueFactory = accessorTypeProvider.getConstructorType(RootCacheValueFactoryDelegate.class, enhancedType);
			}
		}
		catch (Throwable e)
		{
			if (log.isWarnEnabled())
			{
				log.warn(bytecodePrinter.toPrintableBytecode(enhancedType), e);
			}
			// something serious happened during enhancement: continue with a fallback
			rootCacheValueFactory = rcvFactory;
		}
		typeToConstructorMap.put(metaData, rootCacheValueFactory);
		return rootCacheValueFactory;
	}
}
