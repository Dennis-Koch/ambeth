package de.osthus.ambeth.metadata;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.accessor.IAccessorTypeProvider;
import de.osthus.ambeth.bytecode.IBytecodeEnhancer;
import de.osthus.ambeth.cache.AbstractCacheValue;
import de.osthus.ambeth.collections.Tuple2KeyHashMap;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;

public class ObjRefFactory extends IObjRefFactory
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IAccessorTypeProvider accessorTypeProvider;

	@Autowired
	protected IBytecodeEnhancer bytecodeEnhancer;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	protected final Tuple2KeyHashMap<Class<?>, Integer, IPreparedObjRefFactory> constructorDelegateMap = new Tuple2KeyHashMap<Class<?>, Integer, IPreparedObjRefFactory>();

	protected final Lock writeLock = new ReentrantLock();

	protected IPreparedObjRefFactory buildDelegate(Class<?> realType, int idIndex)
	{
		writeLock.lock();
		try
		{
			IPreparedObjRefFactory objRefConstructorDelegate = constructorDelegateMap.get(realType, Integer.valueOf(idIndex));
			if (objRefConstructorDelegate != null)
			{
				return objRefConstructorDelegate;
			}
			Class<?> enhancedType = bytecodeEnhancer.getEnhancedType(Object.class, new ObjRefEnhancementHint(realType, idIndex));
			objRefConstructorDelegate = accessorTypeProvider.getConstructorType(IPreparedObjRefFactory.class, enhancedType);
			constructorDelegateMap.put(realType, Integer.valueOf(idIndex), objRefConstructorDelegate);
			return objRefConstructorDelegate;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public IObjRef dup(IObjRef objRef)
	{
		IPreparedObjRefFactory objRefConstructorDelegate = constructorDelegateMap.get(objRef.getRealType(), Integer.valueOf(objRef.getIdNameIndex()));
		if (objRefConstructorDelegate == null)
		{
			objRefConstructorDelegate = buildDelegate(objRef.getRealType(), objRef.getIdNameIndex());
		}
		return objRefConstructorDelegate.createObjRef(objRef.getId(), objRef.getVersion());
	}

	@Override
	public IObjRef createObjRef(AbstractCacheValue cacheValue)
	{
		IPreparedObjRefFactory objRefConstructorDelegate = constructorDelegateMap.get(cacheValue.getEntityType(), Integer.valueOf(ObjRef.PRIMARY_KEY_INDEX));
		if (objRefConstructorDelegate == null)
		{
			objRefConstructorDelegate = buildDelegate(cacheValue.getEntityType(), ObjRef.PRIMARY_KEY_INDEX);
		}
		return objRefConstructorDelegate.createObjRef(cacheValue.getId(), cacheValue.getVersion());
	}

	@Override
	public IObjRef createObjRef(AbstractCacheValue cacheValue, int idIndex)
	{
		IPreparedObjRefFactory objRefConstructorDelegate = constructorDelegateMap.get(cacheValue.getEntityType(), Integer.valueOf(idIndex));
		if (objRefConstructorDelegate == null)
		{
			objRefConstructorDelegate = buildDelegate(cacheValue.getEntityType(), idIndex);
		}
		return objRefConstructorDelegate.createObjRef(cacheValue.getId(), cacheValue.getVersion());
	}

	@Override
	public IObjRef createObjRef(Class<?> entityType, int idIndex, Object id, Object version)
	{
		IPreparedObjRefFactory objRefConstructorDelegate = constructorDelegateMap.get(entityType, Integer.valueOf(idIndex));
		if (objRefConstructorDelegate == null)
		{
			objRefConstructorDelegate = buildDelegate(entityType, idIndex);
		}
		return objRefConstructorDelegate.createObjRef(id, version);
	}

	@Override
	public IPreparedObjRefFactory prepareObjRefFactory(Class<?> entityType, int idIndex)
	{
		IPreparedObjRefFactory objRefConstructorDelegate = constructorDelegateMap.get(entityType, Integer.valueOf(idIndex));
		if (objRefConstructorDelegate == null)
		{
			objRefConstructorDelegate = buildDelegate(entityType, idIndex);
		}
		return objRefConstructorDelegate;
	}
}
