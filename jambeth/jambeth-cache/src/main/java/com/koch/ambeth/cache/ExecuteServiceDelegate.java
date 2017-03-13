package com.koch.ambeth.cache;

import com.koch.ambeth.service.cache.model.IServiceResult;
import com.koch.ambeth.service.model.IServiceDescription;

public interface ExecuteServiceDelegate
{
	IServiceResult invoke(IServiceDescription serviceDescription);
}
