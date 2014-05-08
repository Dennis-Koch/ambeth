package de.osthus.ambeth.typeinfo;

import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.util.ParamChecker;

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
		return serviceContext.registerAnonymousBean(typeInfoProviderType).propertyValue("Synchronized", Boolean.FALSE).finish();
	}
}
