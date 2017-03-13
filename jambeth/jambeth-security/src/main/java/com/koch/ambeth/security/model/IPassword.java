package com.koch.ambeth.security.model;

import java.util.Calendar;

public interface IPassword
{
	public static final String Algorithm = "Algorithm";

	public static final String ChangeAfter = "ChangeAfter";

	public static final String IterationCount = "IterationCount";

	public static final String HistoryUser = "HistoryUser";

	public static final String KeySize = "KeySize";

	public static final String Salt = "Salt";

	public static final String User = "User";

	public static final String Value = "Value";

	IUser getUser();

	IUser getHistoryUser();

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

	Integer getSaltLength();

	void setSaltLength(Integer saltLength);

	IPBEConfiguration getSaltPBEConfiguration();
}
