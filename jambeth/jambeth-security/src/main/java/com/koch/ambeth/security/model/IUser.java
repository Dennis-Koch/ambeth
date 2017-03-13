package com.koch.ambeth.security.model;

import java.util.Collection;

public interface IUser
{
	public static final String Password = "Password";

	public static final String PasswordHistory = "PasswordHistory";

	public static final String Signature = "Signature";

	IPassword getPassword();

	void setPassword(IPassword password);

	Collection<? extends IPassword> getPasswordHistory();

	ISignature getSignature();

	void setSignature(ISignature signature);
}
