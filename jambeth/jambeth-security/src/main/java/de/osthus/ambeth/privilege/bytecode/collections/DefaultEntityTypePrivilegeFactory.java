package de.osthus.ambeth.privilege.bytecode.collections;

import de.osthus.ambeth.privilege.model.ITypePropertyPrivilege;
import de.osthus.ambeth.privilege.model.impl.AbstractTypePrivilege;
import de.osthus.ambeth.privilege.model.impl.DefaultTypePrivilegeImpl;

public class DefaultEntityTypePrivilegeFactory implements IEntityTypePrivilegeFactory
{
	@Override
	public AbstractTypePrivilege createPrivilege(Boolean create, Boolean read, Boolean update, Boolean delete, Boolean execute,
			ITypePropertyPrivilege[] primitivePropertyPrivileges, ITypePropertyPrivilege[] relationPropertyPrivileges)
	{
		return new DefaultTypePrivilegeImpl(create, read, update, delete, execute, primitivePropertyPrivileges, relationPropertyPrivileges);
	}
}
