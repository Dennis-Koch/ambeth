package de.osthus.ambeth.privilege.model.impl;

import java.io.Serializable;

import de.osthus.ambeth.privilege.model.ITypePrivilege;
import de.osthus.ambeth.privilege.model.ITypePrivilegeResult;

public class TypePrivilegeResult implements ITypePrivilegeResult, Serializable
{
	private static final long serialVersionUID = -5403054067546734382L;

	private final String sid;

	private final ITypePrivilege[] typePrivileges;

	public TypePrivilegeResult(String sid, ITypePrivilege[] typePrivileges)
	{
		this.sid = sid;
		this.typePrivileges = typePrivileges;
	}

	@Override
	public String getSID()
	{
		return sid;
	}

	@Override
	public ITypePrivilege[] getTypePrivileges()
	{
		return typePrivileges;
	}
}
