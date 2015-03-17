package de.osthus.ambeth.privilege.model.impl;

import java.io.ObjectStreamException;

import de.osthus.ambeth.privilege.model.IPrivilege;
import de.osthus.ambeth.privilege.model.IPropertyPrivilege;
import de.osthus.ambeth.privilege.transfer.IPrivilegeOfService;

public final class SimplePrivilegeImpl extends AbstractPrivilege
{
	private static final long serialVersionUID = -4733612935037155207L;

	private static final SimplePrivilegeImpl[] array = new SimplePrivilegeImpl[arraySizeForIndex()];

	static
	{
		put1();
	}

	private static void put1()
	{
		put2(true);
		put2(false);
	}

	private static void put2(boolean create)
	{
		put3(create, true);
		put3(create, false);
	}

	private static void put3(boolean create, boolean read)
	{
		put4(create, read, true);
		put4(create, read, false);
	}

	private static void put4(boolean create, boolean read, boolean update)
	{
		put5(create, read, update, true);
		put5(create, read, update, false);
	}

	private static void put5(boolean create, boolean read, boolean update, boolean delete)
	{
		put(create, read, update, delete, true);
		put(create, read, update, delete, false);
	}

	public static int arraySizeForIndex()
	{
		return 1 << 5;
	}

	private static void put(boolean create, boolean read, boolean update, boolean delete, boolean execute)
	{
		IPropertyPrivilege propertyPrivilege = PropertyPrivilegeImpl.create(create, read, update, delete);
		int index = calcIndex(create, read, update, delete, execute);
		array[index] = new SimplePrivilegeImpl(create, read, update, delete, execute, propertyPrivilege);
	}

	public static IPrivilege createFrom(IPrivilegeOfService privilegeOfService)
	{
		return create(privilegeOfService.isCreateAllowed(), privilegeOfService.isReadAllowed(), privilegeOfService.isUpdateAllowed(),
				privilegeOfService.isDeleteAllowed(), privilegeOfService.isExecuteAllowed());
	}

	public static IPrivilege create(boolean create, boolean read, boolean update, boolean delete, boolean execute)
	{
		int index = calcIndex(create, read, update, delete, execute);
		return array[index];
	}

	private final boolean create, read, update, delete, execute;

	private final IPropertyPrivilege defaultPropertyPrivileges;

	private SimplePrivilegeImpl(boolean create, boolean read, boolean update, boolean delete, boolean execute, IPropertyPrivilege defaultPropertyPrivileges)
	{
		super(create, read, update, delete, execute, null, null);
		this.create = create;
		this.read = read;
		this.update = update;
		this.delete = delete;
		this.execute = execute;
		this.defaultPropertyPrivileges = defaultPropertyPrivileges;
	}

	@Override
	public IPropertyPrivilege getPrimitivePropertyPrivilege(int primitiveIndex)
	{
		return defaultPropertyPrivileges;
	}

	@Override
	public IPropertyPrivilege getRelationPropertyPrivilege(int relationIndex)
	{
		return defaultPropertyPrivileges;
	}

	@Override
	public IPropertyPrivilege getDefaultPropertyPrivilegeIfValid()
	{
		return defaultPropertyPrivileges;
	}

	@Override
	public boolean isCreateAllowed()
	{
		return create;
	}

	@Override
	public boolean isReadAllowed()
	{
		return read;
	}

	@Override
	public boolean isUpdateAllowed()
	{
		return update;
	}

	@Override
	public boolean isDeleteAllowed()
	{
		return delete;
	}

	@Override
	public boolean isExecuteAllowed()
	{
		return execute;
	}

	private Object readResolve() throws ObjectStreamException
	{
		return create(create, read, update, delete, execute);
	}
}
