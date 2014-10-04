package de.osthus.ambeth.helloworld.security;

import java.util.Collection;

import de.osthus.ambeth.security.model.IPassword;
import de.osthus.ambeth.security.model.ISignature;
import de.osthus.ambeth.security.model.IUser;

public class PojoUser implements IUser
{
	private IPassword password;

	private ISignature signature;

	private final String name;

	public PojoUser(String name)
	{
		this.name = name;

	}

	@Override
	public String getAuditedIdentifier()
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
