package com.koch.ambeth.concept;

import java.io.Closeable;

public interface IAmbethApplication extends Closeable
{
	IServiceContext getApplicationContext();
}
