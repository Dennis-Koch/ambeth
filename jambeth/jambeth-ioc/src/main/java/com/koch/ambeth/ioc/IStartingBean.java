package com.koch.ambeth.ioc;

/**
 * Interface for beans that need to initialize or check things after all beans have been injected with their dependencies and have been initialized.
 */
public interface IStartingBean
{
	/**
	 * Implement initializations and checks in this method to be run after all beans have been prepared and initialized by the IoC container.
	 * 
	 * @throws Throwable
	 */
	void afterStarted() throws Throwable;
}
