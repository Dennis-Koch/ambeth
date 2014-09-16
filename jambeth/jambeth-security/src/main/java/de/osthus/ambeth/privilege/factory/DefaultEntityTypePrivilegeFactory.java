package de.osthus.ambeth.privilege.factory;

import de.osthus.ambeth.privilege.model.ITypePrivilege;
import de.osthus.ambeth.privilege.model.ITypePropertyPrivilege;
import de.osthus.ambeth.privilege.model.impl.DefaultTypePrivilegeImpl;

public class DefaultEntityTypePrivilegeFactory implements IEntityTypePrivilegeFactory
{
	@Override
	public ITypePrivilege createPrivilege(Boolean create, Boolean read, Boolean update, Boolean delete, Boolean execute,
			ITypePropertyPrivilege[] primitivePropertyPrivileges, ITypePropertyPrivilege[] relationPropertyPrivileges)
	{
		return new DefaultTypePrivilegeImpl(create, read, update, delete, execute, primitivePropertyPrivileges, relationPropertyPrivileges);
	}
}
