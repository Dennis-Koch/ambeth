package de.osthus.ambeth.webservice;

import java.io.Serializable;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

/**
 * This class is a trick to stop tomcat from restoring the session (because this class is not {@link Serializable}9
 */
public class AttributeAuthorizationChangeRegistered
{
	private Boolean registered;

	public AttributeAuthorizationChangeRegistered(Boolean true1)
	{
		setRegistered(true1);
	}

	public Boolean getRegistered()
	{
		return registered;
	}

	public void setRegistered(Boolean registered)
	{
		this.registered = registered;
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;
}
