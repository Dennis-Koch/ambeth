package com.koch.ambeth.security.server;

/*-
 * #%L
 * jambeth-security-server
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

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
