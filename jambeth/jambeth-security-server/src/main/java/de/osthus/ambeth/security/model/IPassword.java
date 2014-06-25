package de.osthus.ambeth.security.model;

import java.util.Calendar;

public interface IPassword
{
	char[] getValue();

	void setValue(char[] value);

	Calendar getChangeAfter();

	void setChangeAfter(Calendar changeAfter);

	String getAlgorithm();

	void setAlgorithm(String algorithm);

	int getIterationCount();

	void setIterationCount(int iterationCount);

	int getKeySize();

	void setKeySize(int keySize);

	char[] getSalt();

	void setSalt(char[] salt);

	boolean isSaltEncrypted();

	void setSaltEncrypted(boolean saltEncrypted);
}
