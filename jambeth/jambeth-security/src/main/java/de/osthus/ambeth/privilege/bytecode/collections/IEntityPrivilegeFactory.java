package de.osthus.ambeth.privilege.bytecode.collections;

import de.osthus.ambeth.privilege.model.IPropertyPrivilege;
import de.osthus.ambeth.privilege.model.impl.AbstractPrivilege;

public interface IEntityPrivilegeFactory
{
	AbstractPrivilege createPrivilege(boolean read, boolean create, boolean update, boolean delete, boolean execute,
			IPropertyPrivilege[] primitivePropertyPrivileges, IPropertyPrivilege[] relationPropertyPrivileges);
}
