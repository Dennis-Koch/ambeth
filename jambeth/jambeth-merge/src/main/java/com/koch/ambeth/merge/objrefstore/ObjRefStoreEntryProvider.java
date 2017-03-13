package com.koch.ambeth.merge.objrefstore;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.koch.ambeth.ioc.accessor.IAccessorTypeProvider;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.bytecode.IBytecodeEnhancer;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.util.collections.Tuple2KeyHashMap;

public class ObjRefStoreEntryProvider extends IObjRefStoreEntryProvider
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

	protected final Tuple2KeyHashMap<Class<?>, Integer, IObjRefStoreFactory> constructorDelegateMap = new Tuple2KeyHashMap<Class<?>, Integer, IObjRefStoreFactory>();

	protected final Lock writeLock = new ReentrantLock();

	@Override
	public ObjRefStore createObjRefStore(Class<?> entityType, byte idIndex, Object id)
	{
		IObjRefStoreFactory objRefConstructorDelegate = constructorDelegateMap.get(entityType, Integer.valueOf(idIndex));
		if (objRefConstructorDelegate == null)
		{
			objRefConstructorDelegate = buildDelegate(entityType, idIndex);
		}
		ObjRefStore objRefStore = objRefConstructorDelegate.createObjRef();
		objRefStore.setId(id);
		return objRefStore;
	}

	@Override
	public ObjRefStore createObjRefStore(Class<?> entityType, byte idIndex, Object id, ObjRefStore nextEntry)
	{
		IObjRefStoreFactory objRefConstructorDelegate = constructorDelegateMap.get(entityType, Integer.valueOf(idIndex));
		if (objRefConstructorDelegate == null)
		{
			objRefConstructorDelegate = buildDelegate(entityType, idIndex);
		}
		ObjRefStore objRefStore = objRefConstructorDelegate.createObjRef();
		objRefStore.setId(id);
		objRefStore.setNextEntry(nextEntry);
		return objRefStore;
	}

	protected IObjRefStoreFactory buildDelegate(Class<?> entityType, int idIndex)
	{
		writeLock.lock();
		try
		{
			IObjRefStoreFactory objRefConstructorDelegate = constructorDelegateMap.get(entityType, Integer.valueOf(idIndex));
			if (objRefConstructorDelegate != null)
			{
				return objRefConstructorDelegate;
			}
			Class<?> enhancedType = bytecodeEnhancer.getEnhancedType(ObjRefStore.class, new ObjRefStoreEnhancementHint(entityType, idIndex));
			objRefConstructorDelegate = accessorTypeProvider.getConstructorType(IObjRefStoreFactory.class, enhancedType);
			constructorDelegateMap.put(entityType, Integer.valueOf(idIndex), objRefConstructorDelegate);
			return objRefConstructorDelegate;
		}
		finally
		{
			writeLock.unlock();
		}
	}
}
