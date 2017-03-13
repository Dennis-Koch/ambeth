package com.koch.ambeth.security.server;

import com.koch.ambeth.security.model.ISignature;
import com.koch.ambeth.security.model.IUser;

public interface IPrivateKeyProvider
{
	java.security.Signature getSigningHandle(ISignature signature, char[] clearTextPassword);

	java.security.Signature getSigningHandle(IUser user, char[] clearTextPassword);

	java.security.Signature getVerifyingHandle(IUser user);
}