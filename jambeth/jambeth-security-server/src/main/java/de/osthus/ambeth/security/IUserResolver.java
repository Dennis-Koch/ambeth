package de.osthus.ambeth.security;

import de.osthus.ambeth.security.model.IUser;

public interface IUserResolver
{
	IUser resolveUserBySID(String sid);
}
