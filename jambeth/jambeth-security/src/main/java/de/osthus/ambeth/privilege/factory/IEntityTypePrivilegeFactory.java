package de.osthus.ambeth.privilege.factory;

import de.osthus.ambeth.privilege.model.ITypePrivilege;
import de.osthus.ambeth.privilege.model.ITypePropertyPrivilege;

public interface IEntityTypePrivilegeFactory
{
	ITypePrivilege createPrivilege(Boolean read, Boolean create, Boolean update, Boolean delete, Boolean execute,
			ITypePropertyPrivilege[] primitivePropertyPrivileges, ITypePropertyPrivilege[] relationPropertyPrivileges);
}
