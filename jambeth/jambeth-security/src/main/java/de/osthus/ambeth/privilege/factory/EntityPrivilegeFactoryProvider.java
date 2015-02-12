package de.osthus.ambeth.privilege.factory;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.accessor.IAccessorTypeProvider;
import de.osthus.ambeth.bytecode.IBytecodeEnhancer;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.privilege.model.impl.AbstractPrivilege;

public class EntityPrivilegeFactoryProvider implements IEntityPrivilegeFactoryProvider
{
	protected static final IEntityPrivilegeFactory ci = new DefaultEntityPrivilegeFactory();

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired(optional = true)
	protected IBytecodeEnhancer bytecodeEnhancer;

	@Autowired
	protected IAccessorTypeProvider accessorTypeProvider;

	protected final HashMap<Class<?>, IEntityPrivilegeFactory[]> typeToConstructorMap = new HashMap<Class<?>, IEntityPrivilegeFactory[]>();

	protected final Lock writeLock = new ReentrantLock();

	@Override
	public IEntityPrivilegeFactory getEntityPrivilegeFactory(Class<?> entityType, boolean create, boolean read, boolean update, boolean delete, boolean execute)
	{
		if (bytecodeEnhancer == null)
		{
			return ci;
		}
		int index = AbstractPrivilege.calcIndex(create, read, update, delete, execute);
		IEntityPrivilegeFactory[] factories = typeToConstructorMap.get(entityType);
		IEntityPrivilegeFactory factory = factories != null ? factories[index] : null;
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
				Class<?> enhancedType = bytecodeEnhancer.getEnhancedType(AbstractPrivilege.class, new EntityPrivilegeEnhancementHint(entityType, create, read,
						update, delete, execute));

				if (enhancedType == AbstractPrivilege.class)
				{
					// Nothing has been enhanced
					factory = ci;
				}
				else
				{
					factory = accessorTypeProvider.getConstructorType(IEntityPrivilegeFactory.class, enhancedType);
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
				factories = new IEntityPrivilegeFactory[AbstractPrivilege.arraySizeForIndex()];
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
