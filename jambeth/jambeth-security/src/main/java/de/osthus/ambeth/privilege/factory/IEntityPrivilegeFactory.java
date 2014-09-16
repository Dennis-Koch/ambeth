package de.osthus.ambeth.privilege.factory;

import de.osthus.ambeth.privilege.model.IPrivilege;
import de.osthus.ambeth.privilege.model.IPropertyPrivilege;

public interface IEntityPrivilegeFactory
{
	IPrivilege createPrivilege(boolean read, boolean create, boolean update, boolean delete, boolean execute, IPropertyPrivilege[] primitivePropertyPrivileges,
			IPropertyPrivilege[] relationPropertyPrivileges);
}
