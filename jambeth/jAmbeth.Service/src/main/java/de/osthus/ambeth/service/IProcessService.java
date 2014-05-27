package de.osthus.ambeth.service;

import de.osthus.ambeth.model.IServiceDescription;

public interface IProcessService
{
	Object invokeService(IServiceDescription serviceDescription);
}
