package de.osthus.ambeth.security.model;

import java.util.Calendar;

public interface IPassword
{
	public static final String Algorithm = "Algorithm";

	public static final String ChangeAfter = "ChangeAfter";

	public static final String IterationCount = "IterationCount";

	public static final String KeySize = "KeySize";

	public static final String Salt = "Salt";

	public static final String Value = "Value";

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

	String getSaltAlgorithm();

	void setSaltAlgorithm(String saltAlgorithm);

	Integer getSaltLength();

	void setSaltLength(Integer saltLength);
}
