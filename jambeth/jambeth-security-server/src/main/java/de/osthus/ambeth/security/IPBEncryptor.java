package de.osthus.ambeth.security;

import de.osthus.ambeth.security.model.IPBEConfiguration;

public interface IPBEncryptor
{
	byte[] doPaddingForPassword(IPBEConfiguration pbeConfiguration, char[] clearTextPassword);

	byte[] decrypt(IPBEConfiguration pbeConfiguration, char[] clearTextPassword, byte[] dataToDecrypt);

	byte[] encrypt(IPBEConfiguration pbeConfiguration, boolean forceUseSalt, char[] clearTextPassword, byte[] dataToEncrypt);

	boolean isReencryptionRecommended(IPBEConfiguration pbeConfiguration);
}