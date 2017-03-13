package com.koch.ambeth.server.helloworld.security;

import java.util.Collection;

import com.koch.ambeth.security.model.IPassword;
import com.koch.ambeth.security.model.ISignature;
import com.koch.ambeth.security.model.IUser;

public class PojoUser implements IUser
{
	private IPassword password;

	private ISignature signature;

	private final String name;

	public PojoUser(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	@Override
	public IPassword getPassword()
	{
		return password;
	}

	@Override
	public void setPassword(IPassword password)
	{
		this.password = password;
	}

	@Override
	public ISignature getSignature()
	{
		return signature;
	}

	@Override
	public void setSignature(ISignature signature)
	{
		this.signature = signature;
	}

	@Override
	public Collection<? extends IPassword> getPasswordHistory()
	{
		return null;
	}
}
