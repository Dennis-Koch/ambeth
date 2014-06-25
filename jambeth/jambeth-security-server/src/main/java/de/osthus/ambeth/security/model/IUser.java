package de.osthus.ambeth.security.model;

import java.util.Collection;

public interface IUser
{
	IPassword getPassword();

	void setPassword(IPassword password);

	Collection<? extends IPassword> getPasswordHistory();
}
