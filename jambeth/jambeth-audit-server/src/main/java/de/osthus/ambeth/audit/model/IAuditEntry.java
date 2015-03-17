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

	void setProtocol(int protocol);

	long getTimestamp();

	void setTimestamp(long timestamp);

	IUser getUser();

	void setUser(IUser user);

	String getUserIdentifier();

	void setUserIdentifier(String userIdentifier);

	String getReason();

	void setReason(String reason);

	String getContext();

	void setContext(String context);

	ISignature getSignatureOfUser();

	void setSignatureOfUser(ISignature signatureOfUser);

	char[] getSignature();

	void setSignature(char[] signature);

	String getHashAlgorithm();

	void setHashAlgorithm(String hashAlgorithm);

	List<? extends IAuditedService> getServices();

	List<? extends IAuditedEntity> getEntities();
}