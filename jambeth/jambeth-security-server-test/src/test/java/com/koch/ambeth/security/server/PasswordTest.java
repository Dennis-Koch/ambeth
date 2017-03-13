package com.koch.ambeth.security.server;

import java.security.GeneralSecurityException;

import org.junit.Test;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.security.server.PasswordSalts;
import com.koch.ambeth.security.server.Passwords;

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
