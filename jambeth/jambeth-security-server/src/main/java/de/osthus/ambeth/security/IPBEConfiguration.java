package de.osthus.ambeth.security;

public interface IPBEConfiguration
{
	String getEncryptionAlgorithm();

	void setEncryptionAlgorithm(String encryptionAlgorithm);

	String getEncryptionKeySpec();

	void setEncryptionKeySpec(String encryptionKeySpec);

	char[] getEncryptionKeyIV();

	void setEncryptionKeyIV(char[] encryptionKeyIV);

	String getPaddedKeyAlgorithm();

	void setPaddedKeyAlgorithm(String paddedKeyAlgorithm);

	int getPaddedKeySize();

	void setPaddedKeySize(int paddedKeySize);

	int getPaddedKeyIterations();

	void setPaddedKeyIterations(int paddedKeyIterations);

	int getPaddedKeySaltSize();

	void setPaddedKeySaltSize(int paddedKeySaltSize);

	char[] getPaddedKeySalt();

	void setPaddedKeySalt(char[] paddedKeySalt);
}
