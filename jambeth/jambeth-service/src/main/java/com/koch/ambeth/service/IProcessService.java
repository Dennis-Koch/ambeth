package com.koch.ambeth.service;

import com.koch.ambeth.service.model.IServiceDescription;

public interface IProcessService
{
	Object invokeService(IServiceDescription serviceDescription);
}
