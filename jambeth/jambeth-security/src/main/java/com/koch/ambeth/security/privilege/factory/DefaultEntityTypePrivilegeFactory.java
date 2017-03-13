package com.koch.ambeth.security.privilege.factory;

import com.koch.ambeth.security.privilege.model.ITypePrivilege;
import com.koch.ambeth.security.privilege.model.ITypePropertyPrivilege;
import com.koch.ambeth.security.privilege.model.impl.DefaultTypePrivilegeImpl;

public class DefaultEntityTypePrivilegeFactory implements IEntityTypePrivilegeFactory
{
	@Override
	public ITypePrivilege createPrivilege(Boolean create, Boolean read, Boolean update, Boolean delete, Boolean execute,
			ITypePropertyPrivilege[] primitivePropertyPrivileges, ITypePropertyPrivilege[] relationPropertyPrivileges)
	{
		return new DefaultTypePrivilegeImpl(create, read, update, delete, execute, primitivePropertyPrivileges, relationPropertyPrivileges);
	}
}
