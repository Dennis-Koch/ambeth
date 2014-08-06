package de.osthus.ambeth.privilege.model.impl;

import de.osthus.ambeth.privilege.model.ITypePrivilege;
import de.osthus.ambeth.privilege.model.ITypePropertyPrivilege;
import de.osthus.ambeth.privilege.transfer.ITypePrivilegeOfService;
import de.osthus.ambeth.privilege.transfer.ITypePropertyPrivilegeOfService;
import de.osthus.ambeth.util.IImmutableType;
import de.osthus.ambeth.util.IPrintable;

public final class TypePropertyPrivilegeImpl implements ITypePropertyPrivilege, IPrintable, IImmutableType
{
	public static final ITypePropertyPrivilege[] EMPTY_PROPERTY_PRIVILEGES = new ITypePropertyPrivilege[0];

	private static final TypePropertyPrivilegeImpl[] array = new TypePropertyPrivilegeImpl[1 << 8];

	static
	{
		put1();
	}

	private static void put1()
	{
		put2(null);
		put2(Boolean.FALSE);
		put2(Boolean.TRUE);
	}

	private static void put2(Boolean create)
	{
		put3(create, null);
		put3(create, Boolean.FALSE);
		put3(create, Boolean.TRUE);
	}

	private static void put3(Boolean create, Boolean read)
	{
		put4(create, read, null);
		put4(create, read, Boolean.FALSE);
		put4(create, read, Boolean.TRUE);
	}

	private static void put4(Boolean create, Boolean read, Boolean update)
	{
		put(create, read, update, null);
		put(create, read, update, Boolean.FALSE);
		put(create, read, update, Boolean.TRUE);
	}

	protected static int toBitValue(Boolean value, int startingBit)
	{
		if (value == null)
		{
			return 0;
		}
		return value.booleanValue() ? 1 << startingBit : 1 << (startingBit + 1);
	}

	private static void put(Boolean create, Boolean read, Boolean update, Boolean delete)
	{
		int index = toBitValue(create, 0) + toBitValue(read, 2) + toBitValue(update, 4) + toBitValue(delete, 6);
		array[index] = new TypePropertyPrivilegeImpl(create, read, update, delete);
	}

	public static ITypePropertyPrivilege create(Boolean create, Boolean read, Boolean update, Boolean delete)
	{
		int index = toBitValue(create, 0) + toBitValue(read, 2) + toBitValue(update, 4) + toBitValue(delete, 6);
		return array[index];
	}

	public static ITypePropertyPrivilege createFrom(ITypePrivilege privilegeAsTemplate)
	{
		return create(privilegeAsTemplate.isCreateAllowed(), privilegeAsTemplate.isReadAllowed(), privilegeAsTemplate.isUpdateAllowed(),
				privilegeAsTemplate.isDeleteAllowed());
	}

	public static ITypePropertyPrivilege createFrom(ITypePrivilegeOfService privilegeOfService)
	{
		return create(privilegeOfService.isCreateAllowed(), privilegeOfService.isReadAllowed(), privilegeOfService.isUpdateAllowed(),
				privilegeOfService.isDeleteAllowed());
	}

	public static ITypePropertyPrivilege createFrom(ITypePropertyPrivilegeOfService propertyPrivilegeResult)
	{
		return create(propertyPrivilegeResult.isCreateAllowed(), propertyPrivilegeResult.isReadAllowed(), propertyPrivilegeResult.isUpdateAllowed(),
				propertyPrivilegeResult.isDeleteAllowed());
	}

	private final Boolean create;
	private final Boolean read;
	private final Boolean update;
	private final Boolean delete;

	private TypePropertyPrivilegeImpl(Boolean create, Boolean read, Boolean update, Boolean delete)
	{
		this.create = create;
		this.read = read;
		this.update = update;
		this.delete = delete;
	}

	@Override
	public Boolean isCreateAllowed()
	{
		return create;
	}

	@Override
	public Boolean isReadAllowed()
	{
		return read;
	}

	@Override
	public Boolean isUpdateAllowed()
	{
		return update;
	}

	@Override
	public Boolean isDeleteAllowed()
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
		if (!(obj instanceof TypePropertyPrivilegeImpl))
		{
			return false;
		}
		TypePropertyPrivilegeImpl other = (TypePropertyPrivilegeImpl) obj;
		return create == other.create && read == other.read && update == other.update && delete == other.delete;
	}

	@Override
	public int hashCode()
	{
		return toBitValue(create, 0) + toBitValue(read, 2) + toBitValue(update, 4) + toBitValue(delete, 6);
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
