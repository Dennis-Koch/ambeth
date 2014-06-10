package de.osthus.ambeth.ioc.factory;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IdentityHashMap;
import de.osthus.ambeth.collections.IdentityLinkedMap;
import de.osthus.ambeth.collections.IdentityLinkedSet;
import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.ioc.IDisposableBean;
import de.osthus.ambeth.ioc.ServiceContext;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;

public class BeanContextInit
{
	public Properties properties;

	public ServiceContext beanContext;

	public BeanContextFactory beanContextFactory;

	public IdentityLinkedMap<Object, IBeanConfiguration> objectToBeanConfigurationMap;

	public IdentityHashMap<Object, IBeanConfiguration> objectToHandledBeanConfigurationMap;

	public IdentityLinkedSet<Object> allLifeCycledBeansSet;

	public ArrayList<Object> initializedOrdering;

	public ArrayList<IDisposableBean> toDestroyOnError = new ArrayList<IDisposableBean>();
}
