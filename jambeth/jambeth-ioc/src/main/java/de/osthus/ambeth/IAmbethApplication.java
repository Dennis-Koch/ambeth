package de.osthus.ambeth;

import java.io.Closeable;

import de.osthus.ambeth.ioc.IServiceContext;

public interface IAmbethApplication extends Closeable
{
	/**
	 * Accessor for the application level jAmbeth context.
	 * 
	 * @return Application context
	 */
	IServiceContext getApplicationContext();
}
