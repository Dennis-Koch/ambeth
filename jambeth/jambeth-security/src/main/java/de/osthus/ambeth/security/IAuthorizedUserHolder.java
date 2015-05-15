package de.osthus.ambeth.security;

public interface IAuthorizedUserHolder
{
	String getAuthorizedUserSID();

	void setAuthorizedUserSID(String sid);
}
