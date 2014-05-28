package de.osthus.ambeth.cache;

import de.osthus.ambeth.ioc.IServiceContext;

public interface IEntityWithValueHolderTemplate
{
	void afterPropertiesSet__() throws Throwable;

	void set__BeanContext(IServiceContext beanContext);

	void set__ParentEntity(Object parentEntity);
}
