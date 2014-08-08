package de.osthus.ambeth.privilege.factory;

import de.osthus.ambeth.privilege.model.IPrivilege;
import de.osthus.ambeth.privilege.model.IPropertyPrivilege;
import de.osthus.ambeth.privilege.model.impl.DefaultPrivilegeImpl;

public class DefaultEntityPrivilegeFactory implements IEntityPrivilegeFactory
{
	@Override
	public IPrivilege createPrivilege(boolean create, boolean read, boolean update, boolean delete, boolean execute,
			IPropertyPrivilege[] primitivePropertyPrivileges, IPropertyPrivilege[] relationPropertyPrivileges)
	{
		return new DefaultPrivilegeImpl(create, read, update, delete, execute, primitivePropertyPrivileges, relationPropertyPrivileges);
	}
}
