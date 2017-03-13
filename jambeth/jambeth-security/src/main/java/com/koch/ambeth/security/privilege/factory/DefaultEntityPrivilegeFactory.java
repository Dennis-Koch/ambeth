package com.koch.ambeth.security.privilege.factory;

import com.koch.ambeth.security.privilege.model.IPrivilege;
import com.koch.ambeth.security.privilege.model.IPropertyPrivilege;
import com.koch.ambeth.security.privilege.model.impl.DefaultPrivilegeImpl;

public class DefaultEntityPrivilegeFactory implements IEntityPrivilegeFactory
{
	@Override
	public IPrivilege createPrivilege(boolean create, boolean read, boolean update, boolean delete, boolean execute,
			IPropertyPrivilege[] primitivePropertyPrivileges, IPropertyPrivilege[] relationPropertyPrivileges)
	{
		return new DefaultPrivilegeImpl(create, read, update, delete, execute, primitivePropertyPrivileges, relationPropertyPrivileges);
	}
}
