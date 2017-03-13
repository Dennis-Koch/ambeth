package com.koch.ambeth.security;

import com.koch.ambeth.security.model.IUser;

public interface ICurrentUserProvider
{
	IUser getCurrentUser();

	boolean currentUserHasActionPermission(String permission);
}
