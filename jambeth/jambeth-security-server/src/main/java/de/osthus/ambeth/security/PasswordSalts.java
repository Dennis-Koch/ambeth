package de.osthus.ambeth.security;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;

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
