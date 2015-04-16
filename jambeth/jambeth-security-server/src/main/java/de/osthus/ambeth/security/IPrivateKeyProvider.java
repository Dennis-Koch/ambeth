package de.osthus.ambeth.security;

import de.osthus.ambeth.security.model.ISignature;
import de.osthus.ambeth.security.model.IUser;

public interface IPrivateKeyProvider
{
	java.security.Signature getSigningHandle(ISignature signature, char[] clearTextPassword);

	java.security.Signature getSigningHandle(IUser user, char[] clearTextPassword);

	java.security.Signature getVerifyingHandle(IUser user);
}