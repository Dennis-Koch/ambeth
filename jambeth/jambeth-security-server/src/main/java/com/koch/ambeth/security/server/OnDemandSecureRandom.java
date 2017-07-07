package com.koch.ambeth.security.server;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class OnDemandSecureRandom implements ISecureRandom {
	protected SecureRandom secureRandom;

	@Override
	public SecureRandom getSecureRandomHandle() {
		if (secureRandom != null) {
			return secureRandom;
		}
		synchronized (this) {
			if (secureRandom != null) {
				return secureRandom;
			}
			try {
				secureRandom = SecureRandom.getInstance("SHA1PRNG");
				return secureRandom;
			}
			catch (NoSuchAlgorithmException e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}
	}

	public byte[] acquireRandomBytes(int length) {
		byte[] salt = new byte[length];
		getSecureRandomHandle().nextBytes(salt);
		return salt;
	}
}
