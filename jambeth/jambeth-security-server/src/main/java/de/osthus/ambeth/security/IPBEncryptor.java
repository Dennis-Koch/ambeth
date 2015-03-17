package de.osthus.ambeth.security;

public interface IPBEncryptor
{
	byte[] doPaddingForPassword(IPBEConfiguration pbeConfiguration, char[] clearTextPassword);

	byte[] decrypt(IPBEConfiguration pbeConfiguration, char[] clearTextPassword, byte[] dataToDecrypt);

	byte[] encrypt(IPBEConfiguration pbeConfiguration, char[] clearTextPassword, byte[] dataToEncrypt);

	boolean isReencryptionRecommended(IPBEConfiguration pbeConfiguration);
}