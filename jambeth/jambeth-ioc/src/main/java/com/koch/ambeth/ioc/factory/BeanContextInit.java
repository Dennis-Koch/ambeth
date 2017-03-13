package com.koch.ambeth.ioc.factory;

import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.ServiceContext;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IdentityHashMap;
import com.koch.ambeth.util.collections.IdentityLinkedMap;
import com.koch.ambeth.util.collections.IdentityLinkedSet;

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
