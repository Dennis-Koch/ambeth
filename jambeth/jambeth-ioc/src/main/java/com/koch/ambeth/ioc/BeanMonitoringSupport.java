package com.koch.ambeth.ioc;

import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.typeinfo.IPropertyInfoProvider;

public class BeanMonitoringSupport extends AbstractBeanMonitoringSupport {
	private IServiceContext beanContext;

	private IPropertyInfoProvider propertyInfoProvider;

	private IConversionHelper conversionHelper;

	public BeanMonitoringSupport(Object bean, IServiceContext beanContext) {
		super(bean);
		this.beanContext = beanContext;
	}

	@Override
	protected IPropertyInfoProvider getPropertyInfoProvider() {
		if (propertyInfoProvider == null) {
			propertyInfoProvider = beanContext.getService(IPropertyInfoProvider.class);
		}
		return propertyInfoProvider;
	}

	@Override
	protected IConversionHelper getConversionHelper() {
		if (conversionHelper == null) {
			conversionHelper = beanContext.getService(IConversionHelper.class);
		}
		return conversionHelper;
	}
}
