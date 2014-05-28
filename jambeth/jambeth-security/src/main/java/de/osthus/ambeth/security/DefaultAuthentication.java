package de.osthus.ambeth.security;

import java.io.Serializable;

public class DefaultAuthentication implements IAuthentication, Serializable
{
	private static final long serialVersionUID = -5075984200275756231L;

	protected String userName;

	protected byte[] userPass;

	protected PasswordType passwordType;

	public DefaultAuthentication()
	{
		// Intended blank
	}

	public DefaultAuthentication(String userName, byte[] userPass, PasswordType passwordType)
	{
		this.userName = userName;
		this.userPass = userPass;
		this.passwordType = passwordType;
	}

	@Override
	public String getUserName()
	{
		return userName;
	}

	@Override
	public byte[] getPassword()
	{
		return userPass;
	}

	public void setUserName(String userName)
	{
		this.userName = userName;
	}

	public void setUserPass(byte[] userPass)
	{
		this.userPass = userPass;
	}

	public void setPasswordType(PasswordType passwordType)
	{
		this.passwordType = passwordType;
	}

	@Override
	public PasswordType getType()
	{
		return passwordType;
	}
}
