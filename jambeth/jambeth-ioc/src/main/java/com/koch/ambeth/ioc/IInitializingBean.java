package com.koch.ambeth.ioc;

/**
 * Interface for beans that need to initialize or check things after all injections have been made.
 */
public interface IInitializingBean
{
	/**
	 * Implement initializations and checks in this method to be run after the bean has been prepared by the IoC container.
	 * 
	 * @throws Throwable
	 */
	void afterPropertiesSet() throws Throwable;
}
