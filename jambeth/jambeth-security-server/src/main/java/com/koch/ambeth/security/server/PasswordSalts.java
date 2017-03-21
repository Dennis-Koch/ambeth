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

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class PasswordSalts
{
	public static final int SALT_LENGTH = 16;

	public static byte[] nextSalt()
	{
		return nextSalt(SALT_LENGTH);
	}

	public static byte[] nextSalt(int saltLength)
	{
		byte[] salt = new byte[saltLength];
		SecureRandom sr;
		try
		{
			sr = SecureRandom.getInstance("SHA1PRNG");
		}
		catch (NoSuchAlgorithmException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		sr.nextBytes(salt);
		return salt;
	}
}
