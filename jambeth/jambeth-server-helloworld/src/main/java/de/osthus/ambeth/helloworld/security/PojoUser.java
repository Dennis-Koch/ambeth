package de.osthus.ambeth.helloworld.security;

import java.util.Collection;

import de.osthus.ambeth.security.model.IPassword;
import de.osthus.ambeth.security.model.IUser;

public class PojoUser implements IUser
{
	private IPassword password;

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
	public Collection<? extends IPassword> getPasswordHistory()
	{
		return null;
	}
}
