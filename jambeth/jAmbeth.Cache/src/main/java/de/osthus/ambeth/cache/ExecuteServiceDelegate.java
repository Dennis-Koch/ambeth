package de.osthus.ambeth.cache;

import de.osthus.ambeth.cache.model.IServiceResult;
import de.osthus.ambeth.model.IServiceDescription;

public interface ExecuteServiceDelegate
{
	IServiceResult invoke(IServiceDescription serviceDescription);
}
