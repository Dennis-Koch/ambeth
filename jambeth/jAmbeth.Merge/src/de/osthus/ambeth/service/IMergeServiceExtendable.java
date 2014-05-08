package de.osthus.ambeth.service;

public interface IMergeServiceExtendable
{
	void registerMergeService(IMergeService mergeService, Class<?> handledType);

	void unregisterMergeService(IMergeService mergeService, Class<?> handledType);
}
