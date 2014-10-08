package de.osthus.ambeth.security;

import java.security.Signature;

import de.osthus.ambeth.codec.Base64;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityFactory;
import de.osthus.ambeth.security.model.ISignature;
import de.osthus.ambeth.security.model.IUser;

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
		ISignature signature = user.getSignature();
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
