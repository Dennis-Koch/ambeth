package de.osthus.ambeth.security;

import java.security.Signature;

import de.osthus.ambeth.security.model.ISignAndVerify;
import de.osthus.ambeth.security.model.ISignature;

public interface ISignatureUtil
{
	void generateNewSignature(ISignature newEmptySignature, char[] clearTextPassword);

	void reencryptSignature(ISignature signature, char[] oldClearTextPassword, char[] newClearTextPassword);

	Signature createSignatureHandle(ISignAndVerify signAndVerify, byte[] privateKey);

	Signature createVerifyHandle(ISignAndVerify signAndVerify, byte[] publicKey);
}