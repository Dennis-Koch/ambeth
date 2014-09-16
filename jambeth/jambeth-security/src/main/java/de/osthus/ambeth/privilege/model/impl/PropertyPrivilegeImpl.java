package de.osthus.ambeth.privilege.model.impl;

import de.osthus.ambeth.privilege.model.IPrivilege;
import de.osthus.ambeth.privilege.model.IPropertyPrivilege;
import de.osthus.ambeth.privilege.transfer.IPrivilegeOfService;
import de.osthus.ambeth.privilege.transfer.IPropertyPrivilegeOfService;
import de.osthus.ambeth.util.IImmutableType;
import de.osthus.ambeth.util.IPrintable;

public final class PropertyPrivilegeImpl implements IPropertyPrivilege, IPrintable, IImmutableType
{
	public static final IPropertyPrivilege[] EMPTY_PROPERTY_PRIVILEGES = new IPropertyPrivilege[0];

	private static final PropertyPrivilegeImpl[] array = new PropertyPrivilegeImpl[1 << 4];

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
		put(create, read, update, true);
		put(create, read, update, false);
	}

	public static int toBitValue(boolean value, int startingBit)
	{
		return value ? 1 << startingBit : 0;
	}

	public static int toBitValue(boolean create, boolean read, boolean update, boolean delete, boolean execute)
	{
		return toBitValue(create, 0) + toBitValue(read, 1) + toBitValue(update, 2) + toBitValue(delete, 3) + toBitValue(execute, 4);
	}

	private static void put(boolean create, boolean read, boolean update, boolean delete)
	{
		int index = toBitValue(create, read, update, delete, false);
		array[index] = new PropertyPrivilegeImpl(create, read, update, delete);
	}

	public static IPropertyPrivilege create(boolean create, boolean read, boolean update, boolean delete)
	{
		int index = toBitValue(create, read, update, delete, false);
		return array[index];
	}

	public static IPropertyPrivilege createFrom(IPrivilege privilegeAsTemplate)
	{
		return create(privilegeAsTemplate.isCreateAllowed(), privilegeAsTemplate.isReadAllowed(), privilegeAsTemplate.isUpdateAllowed(),
				privilegeAsTemplate.isDeleteAllowed());
	}

	public static IPropertyPrivilege createFrom(IPrivilegeOfService privilegeOfService)
	{
		return create(privilegeOfService.isCreateAllowed(), privilegeOfService.isReadAllowed(), privilegeOfService.isUpdateAllowed(),
				privilegeOfService.isDeleteAllowed());
	}

	public static IPropertyPrivilege createFrom(IPropertyPrivilegeOfService propertyPrivilegeResult)
	{
		return create(propertyPrivilegeResult.isCreateAllowed(), propertyPrivilegeResult.isReadAllowed(), propertyPrivilegeResult.isUpdateAllowed(),
				propertyPrivilegeResult.isDeleteAllowed());
	}

	private final boolean create;
	private final boolean read;
	private final boolean update;
	private final boolean delete;

	private PropertyPrivilegeImpl(boolean create, boolean read, boolean update, boolean delete)
	{
		this.create = create;
		this.read = read;
		this.update = update;
		this.delete = delete;
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
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (!(obj instanceof PropertyPrivilegeImpl))
		{
			return false;
		}
		PropertyPrivilegeImpl other = (PropertyPrivilegeImpl) obj;
		return create == other.create && read == other.read && update == other.update && delete == other.delete;
	}

	@Override
	public int hashCode()
	{
		return toBitValue(create, read, update, delete, false);
	}

	@Override
	public final String toString()
	{
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	@Override
	public void toString(StringBuilder sb)
	{
		sb.append(AbstractPrivilege.upperOrLower(isCreateAllowed(), 'c'));
		sb.append(AbstractPrivilege.upperOrLower(isReadAllowed(), 'r'));
		sb.append(AbstractPrivilege.upperOrLower(isUpdateAllowed(), 'u'));
		sb.append(AbstractPrivilege.upperOrLower(isDeleteAllowed(), 'd'));
	}
}
