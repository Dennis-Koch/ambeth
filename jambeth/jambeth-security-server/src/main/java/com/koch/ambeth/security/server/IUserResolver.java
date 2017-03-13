package com.koch.ambeth.security.server;

import com.koch.ambeth.security.model.IUser;

public interface IUserResolver
{
	IUser resolveUserBySID(String sid);
}
