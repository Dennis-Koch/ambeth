package de.osthus.ambeth.security;

import java.nio.charset.Charset;
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
	private static final Charset utf8 = Charset.forName("UTF-8");

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IEntityFactory entityFactory;

	@Autowired
	protected IPBEncryptor pbEncryptor;

	@Autowired
	protected ISecurityContextHolder securityContextHolder;

	@Autowired
	protected ISignatureUtil signatureUtil;

	@Override
	public Signature getSigningHandle(IUser user)
	{
		ISecurityContext context = securityContextHolder.getContext();
		IAuthorization authorization = context != null ? context.getAuthorization() : null;
		if (authorization == null)
		{
			return null;
		}
		IAuthentication authentication = context.getAuthentication();
		char[] clearTextPassword = authentication.getPassword();
		// the signatureUtil expects the "clearTextPassword" base64-encoded
		// clearTextPassword = Base64.encodeBytes(new String(clearTextPassword).getBytes(utf8)).toCharArray();
		ISignature signature = user.getSignature();
		if (signature == null)
		{
			signature = entityFactory.createEntity(ISignature.class);
			signatureUtil.generateNewSignature(signature, clearTextPassword);
			user.setSignature(signature);
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
