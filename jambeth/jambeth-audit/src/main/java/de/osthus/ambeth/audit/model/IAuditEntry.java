package de.osthus.ambeth.audit.model;

import java.util.List;

import de.osthus.ambeth.security.model.IUser;

public interface IAuditEntry
{
	public static final String Timestamp = "Timestamp";

	public static final String User = "User";

	public static final String Services = "Services";

	public static final String Entities = "Entities";

	long getTimestamp();

	void setTimestamp(long timestamp);

	IUser getUser();

	void setUser(IUser user);

	char[] getSignature();

	void setSignature(char[] signature);

	String getHashAlgorithm();

	void setHashAlgorithm(String hashAlgorithm);

	List<? extends IAuditedService> getServices();

	List<? extends IAuditedEntity> getEntities();
}