package de.osthus.ambeth.privilege.bytecode.collections;

import de.osthus.ambeth.privilege.model.IPropertyPrivilege;
import de.osthus.ambeth.privilege.model.impl.AbstractPrivilege;
import de.osthus.ambeth.privilege.model.impl.DefaultPrivilegeImpl;

public class DefaultEntityPrivilegeFactory implements IEntityPrivilegeFactory
{
	@Override
	public AbstractPrivilege createPrivilege(boolean create, boolean read, boolean update, boolean delete, boolean execute,
			IPropertyPrivilege[] primitivePropertyPrivileges, IPropertyPrivilege[] relationPropertyPrivileges)
	{
		return new DefaultPrivilegeImpl(create, read, update, delete, execute, primitivePropertyPrivileges, relationPropertyPrivileges);
	}
}
