package de.osthus.ambeth.concept;

import java.io.Closeable;

public interface IAmbethApplication extends Closeable
{
	IServiceContext getApplicationContext();
}
