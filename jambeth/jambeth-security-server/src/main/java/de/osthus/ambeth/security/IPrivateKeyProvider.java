package de.osthus.ambeth.security;

import de.osthus.ambeth.security.model.IUser;

public interface IPrivateKeyProvider
{
	java.security.Signature getSigningHandle(IUser user, char[] clearTextPassword);

	java.security.Signature getVerifyingHandle(IUser user);
}
