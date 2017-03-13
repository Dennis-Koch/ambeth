package com.koch.ambeth.persistence.api;


/**
 * Interface to allow registering a hook which gets called after a IDatabase instance is disposed
 * 
 * @author dennis.koch
 *
 */
public interface IDatabaseDisposeHookExtendable
{
	void registerDisposeHook(IDatabaseDisposeHook disposeHook);

	void unregisterDisposeHook(IDatabaseDisposeHook disposeHook);
}
