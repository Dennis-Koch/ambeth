package com.koch.ambeth.audit;

import com.koch.ambeth.audit.server.AuditReasonRequired;
import com.koch.ambeth.model.IAbstractEntity;
import com.koch.ambeth.security.audit.model.Audited;
import com.koch.ambeth.security.model.IUser;

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
