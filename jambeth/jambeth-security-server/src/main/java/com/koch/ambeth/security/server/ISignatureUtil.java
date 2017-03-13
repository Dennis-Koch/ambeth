package com.koch.ambeth.security.server;

import java.security.Signature;

import com.koch.ambeth.security.model.ISignAndVerify;
import com.koch.ambeth.security.model.ISignature;

public interface ISignatureUtil
{
	void generateNewSignature(ISignature newEmptySignature, char[] clearTextPassword);

	void reencryptSignature(ISignature signature, char[] oldClearTextPassword, char[] newClearTextPassword);

	Signature createSignatureHandle(ISignAndVerify signAndVerify, byte[] privateKey);

	Signature createVerifyHandle(ISignAndVerify signAndVerify, byte[] publicKey);
}