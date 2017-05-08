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

import java.security.GeneralSecurityException;
import java.util.Arrays;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class Passwords {
	public static final String ALGORITHM = "PBKDF2WithHmacSHA1";
	public static final int ITERATION_COUNT = 8192;
	public static final int KEY_SIZE = 160;

	public static byte[] hashPassword(char[] password, byte[] salt) throws GeneralSecurityException {
		return hashPassword(password, salt, ITERATION_COUNT, KEY_SIZE);
	}

	public static byte[] hashPassword(char[] password, byte[] salt, int iterationCount, int keySize)
			throws GeneralSecurityException {
		return hashPassword(password, salt, ALGORITHM, iterationCount, keySize);
	}

	public static byte[] hashPassword(char[] password, byte[] salt, String algorithm,
			int iterationCount, int keySize) throws GeneralSecurityException {
		PBEKeySpec spec = new PBEKeySpec(password, salt, iterationCount, keySize);
		SecretKeyFactory factory = SecretKeyFactory.getInstance(algorithm);
		return factory.generateSecret(spec).getEncoded();
	}

	public static boolean matches(char[] password, byte[] passwordHash, byte[] salt)
			throws GeneralSecurityException {
		return matches(password, passwordHash, salt, ITERATION_COUNT, KEY_SIZE);
	}

	public static boolean matches(char[] password, byte[] passwordHash, byte[] salt,
			int iterationCount, int keySize) throws GeneralSecurityException {
		return Arrays.equals(passwordHash, hashPassword(password, salt, iterationCount, keySize));
	}
}
