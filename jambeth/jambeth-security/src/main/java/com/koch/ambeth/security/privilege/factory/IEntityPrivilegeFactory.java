package com.koch.ambeth.security.privilege.factory;

import com.koch.ambeth.security.privilege.model.IPrivilege;
import com.koch.ambeth.security.privilege.model.IPropertyPrivilege;

public interface IEntityPrivilegeFactory
{
	IPrivilege createPrivilege(boolean read, boolean create, boolean update, boolean delete, boolean execute, IPropertyPrivilege[] primitivePropertyPrivileges,
			IPropertyPrivilege[] relationPropertyPrivileges);
}
