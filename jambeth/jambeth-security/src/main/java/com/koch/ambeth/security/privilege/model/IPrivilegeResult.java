package com.koch.ambeth.security.privilege.model;

public interface IPrivilegeResult
{
	String getSID();

	IPrivilege[] getPrivileges();
}
