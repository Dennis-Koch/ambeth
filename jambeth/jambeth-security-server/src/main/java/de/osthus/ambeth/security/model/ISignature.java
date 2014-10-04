package de.osthus.ambeth.security.model;

public interface ISignature
{
	public static final String Algorithm = "Algorithm";

	public static final String ChangeAfter = "ChangeAfter";

	public static final String KeySize = "KeySize";

	public static final String User = "User";

	public static final String Value = "Value";

	IUser getUser();

	char[] getPrivateKey();

	void setPrivateKey(char[] privateKey);

	char[] getPublicKey();

	void setPublicKey(char[] privateKey);

	String getAlgorithm();

	void setAlgorithm(String algorithm);

	String getKeyFactoryAlgorithm();

	void setKeyFactoryAlgorithm(String keyFactoryAlgorithm);

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

	char[] getPaddedKeySalt();

	void setPaddedKeySalt(char[] paddedKeySalt);
}
