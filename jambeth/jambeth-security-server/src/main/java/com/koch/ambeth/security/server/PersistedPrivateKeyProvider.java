package com.koch.ambeth.security.server;

import java.security.Signature;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.IEntityFactory;
import com.koch.ambeth.security.model.ISignature;
import com.koch.ambeth.security.model.IUser;
import com.koch.ambeth.util.codec.Base64;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class PersistedPrivateKeyProvider implements IPrivateKeyProvider
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IEntityFactory entityFactory;

	@Autowired
	protected IPBEncryptor pbEncryptor;

	@Autowired
	protected ISignatureUtil signatureUtil;

	@Override
	public Signature getSigningHandle(IUser user, char[] clearTextPassword)
	{
		if (clearTextPassword == null)
		{
			return null;
		}
		return getSigningHandle(user.getSignature(), clearTextPassword);
	}

	@Override
	public Signature getSigningHandle(ISignature signature, char[] clearTextPassword)
	{
		if (clearTextPassword == null)
		{
			return null;
		}
		if (signature == null)
		{
			return null;
		}
		try
		{
			byte[] decryptedPrivateKey = pbEncryptor.decrypt(signature.getPBEConfiguration(), clearTextPassword, Base64.decode(signature.getPrivateKey()));
			return signatureUtil.createSignatureHandle(signature.getSignAndVerify(), decryptedPrivateKey);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public Signature getVerifyingHandle(IUser user)
	{
		ISignature signature = user.getSignature();
		try
		{
			return signatureUtil.createVerifyHandle(signature.getSignAndVerify(), Base64.decode(signature.getPublicKey()));
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
