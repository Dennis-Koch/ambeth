package com.koch.ambeth.core.start;

import java.io.Closeable;

import com.koch.ambeth.ioc.IServiceContext;

public interface IAmbethApplication extends Closeable
{
	/**
	 * Accessor for the application level jAmbeth context.
	 * 
	 * @return Application context
	 */
	IServiceContext getApplicationContext();
}
