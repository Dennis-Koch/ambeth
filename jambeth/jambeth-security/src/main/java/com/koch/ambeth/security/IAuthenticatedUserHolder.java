package com.koch.ambeth.security;

public interface IAuthenticatedUserHolder
{
	String getAuthenticatedSID();

	void setAuthenticatedSID(String authenticatedSID);
}
