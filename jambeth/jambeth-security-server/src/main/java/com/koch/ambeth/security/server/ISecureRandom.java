package com.koch.ambeth.security.server;

import java.security.SecureRandom;

public interface ISecureRandom {
	byte[] acquireRandomBytes(int length);

	SecureRandom getSecureRandomHandle();
}
