package de.osthus.ambeth.cache;

import de.osthus.ambeth.cache.model.IServiceResult;
import de.osthus.ambeth.model.IServiceDescription;

public interface IServiceResultCache
{
	IServiceResult getORIsOfService(IServiceDescription serviceDescription, ExecuteServiceDelegate executeServiceDelegate);

	void invalidateAll();
}
