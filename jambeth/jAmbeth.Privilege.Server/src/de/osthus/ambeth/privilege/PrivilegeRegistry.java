//package de.osthus.ambeth.privilege;
//
//import de.osthus.ambeth.ioc.IInitializingBean;
//import de.osthus.ambeth.ioc.extendable.ClassExtendableContainer;
//import de.osthus.ambeth.ioc.extendable.IMapExtendableContainer;
//import de.osthus.ambeth.log.ILogger;
//import de.osthus.ambeth.log.LogInstance;
//import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
//import de.osthus.ambeth.util.ParamChecker;
//
//public class PrivilegeRegistry implements IInitializingBean, IPrivilegeProviderExtendable, IPrivilegeRegistry
//{
//	@SuppressWarnings("unused")
//	@LogInstance
//	private ILogger log;
//
//	protected IThreadLocalObjectCollector objectCollector;
//
//	protected IMapExtendableContainer<Class<?>, IPrivilegeProvider> privilegeProviders;
//
//	@Override
//	public void afterPropertiesSet() throws Throwable
//	{
//		ParamChecker.assertNotNull(objectCollector, "ObjectCollector");
//		privilegeProviders = new ClassExtendableContainer<IPrivilegeProvider>("privilegeProvider", "entityType", objectCollector);
//	}
//
//	public void setObjectCollector(IThreadLocalObjectCollector objectCollector)
//	{
//		this.objectCollector = objectCollector;
//	}
//
//	@Override
//	public IPrivilegeProvider getExtension(Class<?> entityType)
//	{
//		return privilegeProviders.getExtension(entityType);
//	}
//
//	@Override
//	public void registerPrivilegeProvider(IPrivilegeProvider privilegeProvider, Class<?> entityType)
//	{
//		privilegeProviders.register(privilegeProvider, entityType);
//	}
//
//	@Override
//	public void unregisterPrivilegeProvider(IPrivilegeProvider privilegeProvider, Class<?> entityType)
//	{
//		privilegeProviders.unregister(privilegeProvider, entityType);
//	}
// }
