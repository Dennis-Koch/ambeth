package com.koch.ambeth.ioc.config;

import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.config.IProperties;

public class BeanInstanceConfiguration extends AbstractBeanConfiguration
{
	protected Object bean;

	protected boolean withLifecycle;

	public BeanInstanceConfiguration(Object bean, String beanName, boolean withLifecycle, IProperties props)
	{
		super(beanName, props);
		ParamChecker.assertParamNotNull(bean, "bean");
		this.bean = bean;
		this.withLifecycle = withLifecycle;
		if (withLifecycle && declarationStackTrace != null && bean instanceof IDeclarationStackTraceAware)
		{
			((IDeclarationStackTraceAware) bean).setDeclarationStackTrace(declarationStackTrace);
		}
	}

	@Override
	public Class<?> getBeanType()
	{
		return bean.getClass();
	}

	@Override
	public Object getInstance()
	{
		return bean;
	}

	@Override
	public Object getInstance(Class<?> instanceType)
	{
		return bean;
	}

	@Override
	public boolean isWithLifecycle()
	{
		return withLifecycle;
	}
}
