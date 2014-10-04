package de.osthus.ambeth.security.model;

import java.util.Collection;

public interface IUser
{
	public static final String AuditedIdentifier = "AuditedIdentifier";

	public static final String Password = "Password";

	public static final String PasswordHistory = "PasswordHistory";

	public static final String Signature = "Signature";

	String getAuditedIdentifier();

	IPassword getPassword();

	void setPassword(IPassword password);

	Collection<? extends IPassword> getPasswordHistory();

	ISignature getSignature();

	void setSignature(ISignature signature);
}
