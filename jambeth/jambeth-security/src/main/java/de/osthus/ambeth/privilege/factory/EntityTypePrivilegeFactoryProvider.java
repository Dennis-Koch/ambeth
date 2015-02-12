package de.osthus.ambeth.privilege.factory;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.accessor.IAccessorTypeProvider;
import de.osthus.ambeth.bytecode.IBytecodeEnhancer;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.privilege.model.impl.AbstractTypePrivilege;

public class EntityTypePrivilegeFactoryProvider implements IEntityTypePrivilegeFactoryProvider
{
	protected static final IEntityTypePrivilegeFactory ci = new DefaultEntityTypePrivilegeFactory();

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired(optional = true)
	protected IBytecodeEnhancer bytecodeEnhancer;

	@Autowired
	protected IAccessorTypeProvider accessorTypeProvider;

	protected final HashMap<Class<?>, IEntityTypePrivilegeFactory[]> typeToConstructorMap = new HashMap<Class<?>, IEntityTypePrivilegeFactory[]>();

	protected final Lock writeLock = new ReentrantLock();

	@Override
	public IEntityTypePrivilegeFactory getEntityTypePrivilegeFactory(Class<?> entityType, Boolean create, Boolean read, Boolean update, Boolean delete,
			Boolean execute)
	{
		if (bytecodeEnhancer == null)
		{
			return ci;
		}
		int index = AbstractTypePrivilege.calcIndex(create, read, update, delete, execute);
		IEntityTypePrivilegeFactory[] factories = typeToConstructorMap.get(entityType);
		IEntityTypePrivilegeFactory factory = factories != null ? factories[index] : null;
		if (factory != null)
		{
			return factory;
		}
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			// concurrent thread might have been faster
			factories = typeToConstructorMap.get(entityType);
			factory = factories != null ? factories[index] : null;
			if (factory != null)
			{
				return factory;
			}
			try
			{
				Class<?> enhancedType = bytecodeEnhancer.getEnhancedType(AbstractTypePrivilege.class, new EntityTypePrivilegeEnhancementHint(entityType,
						create, read, update, delete, execute));

				if (enhancedType == AbstractTypePrivilege.class)
				{
					// Nothing has been enhanced
					factory = ci;
				}
				else
				{
					factory = accessorTypeProvider.getConstructorType(IEntityTypePrivilegeFactory.class, enhancedType);
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
			if (factories == null)
			{
				factories = new IEntityTypePrivilegeFactory[AbstractTypePrivilege.arraySizeForIndex()];
				typeToConstructorMap.put(entityType, factories);
			}
			factories[index] = factory;
			return factory;
		}
		finally
		{
			writeLock.unlock();
		}
	}
}
