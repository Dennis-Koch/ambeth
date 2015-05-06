package de.osthus.ambeth.audit.model;

import java.util.List;

import de.osthus.ambeth.security.model.ISignature;
import de.osthus.ambeth.security.model.IUser;

public interface IAuditEntry
{
	public static final String Protocol = "Protocol";

	public static final String Timestamp = "Timestamp";

	public static final String User = "User";

	public static final String UserIdentifier = "UserIdentifier";

	public static final String Services = "Services";

	public static final String SignatureOfUser = "SignatureOfUser";

	public static final String Entities = "Entities";

	public static final String Reason = "Reason";

	public static final String Context = "Context";

	public static final String HashAlgorithm = "HashAlgorithm";

	public static final String Signature = "Signature";

	int getProtocol();

	long getTimestamp();

	IUser getUser();

	String getUserIdentifier();

	String getReason();

	String getContext();

	ISignature getSignatureOfUser();

	char[] getSignature();

	String getHashAlgorithm();

	List<? extends IAuditedService> getServices();

	List<? extends IAuditedEntity> getEntities();
}