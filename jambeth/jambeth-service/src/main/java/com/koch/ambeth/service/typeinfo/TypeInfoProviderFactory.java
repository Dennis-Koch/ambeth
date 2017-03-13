package com.koch.ambeth.service.typeinfo;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.typeinfo.ITypeInfoProvider;
import com.koch.ambeth.util.typeinfo.ITypeInfoProviderFactory;

public class TypeInfoProviderFactory implements ITypeInfoProviderFactory, IInitializingBean
{
	protected IServiceContext serviceContext;

	protected Class<? extends TypeInfoProvider> typeInfoProviderType;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(serviceContext, "ServiceContext");
		ParamChecker.assertNotNull(typeInfoProviderType, "TypeInfoProviderType");
	}

	public void setServiceContext(IServiceContext serviceContext)
	{
		this.serviceContext = serviceContext;
	}

	public void setTypeInfoProviderType(Class<? extends TypeInfoProvider> typeInfoProviderType)
	{
		this.typeInfoProviderType = typeInfoProviderType;
	}

	@Override
	public ITypeInfoProvider createTypeInfoProvider()
	{
		return serviceContext.registerBean(typeInfoProviderType).propertyValue("Synchronized", Boolean.FALSE).finish();
	}
}
