package de.osthus.ambeth.audit;

import de.osthus.ambeth.audit.model.Audited;
import de.osthus.ambeth.model.IAbstractEntity;
import de.osthus.ambeth.security.model.IUser;

@Audited
@AuditReasonRequired
public interface User extends IAbstractEntity, IUser
{
	public static final String Name = "Name";

	public static final String SID = "SID";

	String getSID();

	void setSID(String sid);

	String getName();

	void setName(String name);

	boolean isActive();

	void setActive(boolean active);
}
