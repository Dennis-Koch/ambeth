package com.koch.ambeth.ioc.osgi;

import java.util.Set;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.koch.ambeth.ioc.IExternalServiceContext;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.BeanContextInit;
import com.koch.ambeth.ioc.factory.BeanContextInitializer;
import com.koch.ambeth.util.typeinfo.IPropertyInfo;

public class OsgiExternalServiceContext implements IExternalServiceContext {

	private final Bundle bundle;

	public OsgiExternalServiceContext(Bundle bundle) {
		this.bundle = bundle;
	}

	@Override
	public <T> T getServiceByType(Class<T> serviceType) {
		BundleContext bundleContext = bundle.getBundleContext();
		ServiceReference<T> serviceReference = bundleContext.getServiceReference(serviceType);
		if (serviceReference == null) {
			return null;
		}
		return bundleContext.getService(serviceReference);
	}

	@Override
	public boolean initializeAutowiring(BeanContextInit beanContextInit,
			IBeanConfiguration beanConfiguration, Object bean, Class<?> beanType,
			IPropertyInfo[] propertyInfos, Set<String> alreadySpecifiedPropertyNamesSet,
			Set<String> ignoredPropertyNamesSet, BeanContextInitializer beanContextInitializer,
			boolean highPriorityBean, IPropertyInfo prop) {
		return false;
	}
}
