package de.osthus.ambeth.privilege.model.impl;

import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.privilege.model.IPrivilege;
import de.osthus.ambeth.privilege.model.IPropertyPrivilege;
import de.osthus.ambeth.privilege.transfer.IPrivilegeOfService;
import de.osthus.ambeth.privilege.transfer.IPropertyPrivilegeOfService;
import de.osthus.ambeth.util.IImmutableType;
import de.osthus.ambeth.util.IPrintable;

public final class PropertyPrivilegeImpl implements IPropertyPrivilege, IPrintable, IImmutableType
{
	public static final IPropertyPrivilege[] EMPTY_PROPERTY_PRIVILEGES = new IPropertyPrivilege[0];

	private static final HashSet<PropertyPrivilegeImpl> set = new HashSet<PropertyPrivilegeImpl>(0.5f);

	static
	{
		set.add(new PropertyPrivilegeImpl(false, false, false, false));
		set.add(new PropertyPrivilegeImpl(false, false, false, true));
		set.add(new PropertyPrivilegeImpl(false, false, true, false));
		set.add(new PropertyPrivilegeImpl(false, false, true, true));
		set.add(new PropertyPrivilegeImpl(false, true, false, false));
		set.add(new PropertyPrivilegeImpl(false, true, false, true));
		set.add(new PropertyPrivilegeImpl(false, true, true, false));
		set.add(new PropertyPrivilegeImpl(false, true, true, true));
		set.add(new PropertyPrivilegeImpl(true, false, false, false));
		set.add(new PropertyPrivilegeImpl(true, false, false, true));
		set.add(new PropertyPrivilegeImpl(true, false, true, false));
		set.add(new PropertyPrivilegeImpl(true, false, true, true));
		set.add(new PropertyPrivilegeImpl(true, true, false, false));
		set.add(new PropertyPrivilegeImpl(true, true, false, true));
		set.add(new PropertyPrivilegeImpl(true, true, true, false));
		set.add(new PropertyPrivilegeImpl(true, true, true, true));
	}

	public static IPropertyPrivilege create(boolean create, boolean read, boolean update, boolean delete)
	{
		return set.get(new PropertyPrivilegeImpl(create, read, update, delete));
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
		return (create ? 1 : 0) * 7 ^ (read ? 1 : 0) * 17 ^ (update ? 1 : 0) * 11 ^ (delete ? 1 : 0) * 13;
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
