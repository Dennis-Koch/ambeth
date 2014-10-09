package de.osthus.ambeth.audit;

import de.osthus.ambeth.model.IAbstractEntity;
import de.osthus.ambeth.security.model.IUser;

public interface User extends IAbstractEntity, IUser
{
	String getName();

	void setName(String name);
}
