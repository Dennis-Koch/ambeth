package de.osthus.ambeth.security;

import java.security.GeneralSecurityException;

import org.junit.Test;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class PasswordTest // extends AbstractIocTest
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Test
	public void testEffort() throws GeneralSecurityException
	{
		char[] password = "12345678".toCharArray();
		long startMillis = System.currentTimeMillis();
		byte[] saltBytes = PasswordSalts.nextSalt();
		Passwords.hashPassword(password, saltBytes);
		System.out.println("time " + (System.currentTimeMillis() - startMillis) + "ms");
	}
}
