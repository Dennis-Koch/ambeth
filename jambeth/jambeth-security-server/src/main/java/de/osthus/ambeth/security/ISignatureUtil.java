package de.osthus.ambeth.security;

import java.security.Signature;

import de.osthus.ambeth.security.model.ISignature;
import de.osthus.ambeth.security.model.IUser;

public interface ISignatureUtil
{
	void updateSignature(ISignature newEmptySignature, char[] clearTextPassword, IUser user);

	Signature createSignatureHandle(ISignature signature, char[] clearTextPassword);

	Signature createVerifyHandle(ISignature signature);
}