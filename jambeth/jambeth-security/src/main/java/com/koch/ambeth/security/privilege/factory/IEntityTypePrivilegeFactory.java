package com.koch.ambeth.security.privilege.factory;

import com.koch.ambeth.security.privilege.model.ITypePrivilege;
import com.koch.ambeth.security.privilege.model.ITypePropertyPrivilege;

public interface IEntityTypePrivilegeFactory
{
	ITypePrivilege createPrivilege(Boolean read, Boolean create, Boolean update, Boolean delete, Boolean execute,
			ITypePropertyPrivilege[] primitivePropertyPrivileges, ITypePropertyPrivilege[] relationPropertyPrivileges);
}
