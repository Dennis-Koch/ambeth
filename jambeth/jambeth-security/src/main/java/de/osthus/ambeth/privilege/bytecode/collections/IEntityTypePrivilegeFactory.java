package de.osthus.ambeth.privilege.bytecode.collections;

import de.osthus.ambeth.privilege.model.ITypePropertyPrivilege;
import de.osthus.ambeth.privilege.model.impl.AbstractTypePrivilege;

public interface IEntityTypePrivilegeFactory
{
	AbstractTypePrivilege createPrivilege(Boolean read, Boolean create, Boolean update, Boolean delete, Boolean execute,
			ITypePropertyPrivilege[] primitivePropertyPrivileges, ITypePropertyPrivilege[] relationPropertyPrivileges);
}
