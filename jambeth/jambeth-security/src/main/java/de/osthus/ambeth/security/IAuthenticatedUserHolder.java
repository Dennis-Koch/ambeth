package de.osthus.ambeth.security;

public interface IAuthenticatedUserHolder
{
	String getAuthenticatedSID();

	void setAuthenticatedSID(String authenticatedSID);
}
