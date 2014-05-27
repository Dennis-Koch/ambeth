package de.osthus.ambeth.security;

import de.osthus.ambeth.model.ISecurityScope;

public interface IUserHandle
{
	String getSID();

	boolean isValid();

	ISecurityScope[] getSecurityScopes();

	IUseCase[] getUseCases();
}
