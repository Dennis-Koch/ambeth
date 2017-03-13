package com.koch.ambeth.util.objectcollector;

public interface ICollectableControllerExtendable
{
	void registerCollectableController(ICollectableController collectableController, Class<?> handledType);

	void unregisterCollectableController(ICollectableController collectableController, Class<?> handledType);
}