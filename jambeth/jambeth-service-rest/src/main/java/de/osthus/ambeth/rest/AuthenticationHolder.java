package de.osthus.ambeth.rest;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.config.ServiceConfigurationConstants;

public class AuthenticationHolder implements IAuthenticationHolder
{
	@Property(name = ServiceConfigurationConstants.UserName, mandatory = false, defaultValue = "dummyUser")
	private String userName;

	@Property(name = ServiceConfigurationConstants.Password, mandatory = false, defaultValue = "dummyPass")
	private String password;

	protected final Lock writeLock = new ReentrantLock();

	@Override
	public String[] getAuthentication()
	{
		writeLock.lock();
		try
		{
			String[] authentication = { userName, password };
			return authentication;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public void setAuthentication(String userName, String password)
	{
		writeLock.lock();
		try
		{
			setUserName(userName);
			setPassword(password);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	public void setPassword(String password)
	{
		this.password = password;
	}

	public String getPassword()
	{
		return password;
	}

	public void setUserName(String userName)
	{
		this.userName = userName;
	}

	public String getUserName()
	{
		return userName;
	}

}