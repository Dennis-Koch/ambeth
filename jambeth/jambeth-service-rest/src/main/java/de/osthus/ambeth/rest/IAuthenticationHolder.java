package de.osthus.ambeth.rest;

public interface IAuthenticationHolder
{
	String[] getAuthentication();

	void setAuthentication(String userName, String password);
}