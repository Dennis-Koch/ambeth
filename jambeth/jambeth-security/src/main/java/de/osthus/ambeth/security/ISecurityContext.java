package de.osthus.ambeth.security;

public interface ISecurityContext
{
	IAuthentication getAuthentication();

	void setAuthentication(IAuthentication authentication);
}
