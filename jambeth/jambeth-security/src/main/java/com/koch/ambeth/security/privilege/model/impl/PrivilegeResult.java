package com.koch.ambeth.security.privilege.model.impl;

import java.io.Serializable;

import com.koch.ambeth.security.privilege.model.IPrivilege;
import com.koch.ambeth.security.privilege.model.IPrivilegeResult;

public class PrivilegeResult implements IPrivilegeResult, Serializable
{
	private static final long serialVersionUID = -5403054067546734382L;

	private final String sid;

	private final IPrivilege[] privileges;

	public PrivilegeResult(String sid, IPrivilege[] privileges)
	{
		this.sid = sid;
		this.privileges = privileges;
	}

	@Override
	public String getSID()
	{
		return sid;
	}

	@Override
	public IPrivilege[] getPrivileges()
	{
		return privileges;
	}
}
