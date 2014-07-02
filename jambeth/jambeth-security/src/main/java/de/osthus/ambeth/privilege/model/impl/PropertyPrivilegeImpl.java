package de.osthus.ambeth.privilege.model.impl;

import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.privilege.model.IPropertyPrivilege;
import de.osthus.ambeth.privilege.transfer.IPropertyPrivilegeOfService;
import de.osthus.ambeth.util.IPrintable;

public final class PropertyPrivilegeImpl implements IPropertyPrivilege, IPrintable
{
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

	public static PropertyPrivilegeImpl create(boolean create, boolean read, boolean update, boolean delete)
	{
		return set.get(new PropertyPrivilegeImpl(create, read, update, delete));
	}

	public static PropertyPrivilegeImpl createFrom(IPropertyPrivilegeOfService propertyPrivilegeResult)
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
		sb.append(isReadAllowed() ? "+R" : "-R");
		sb.append(isCreateAllowed() ? "+C" : "-C");
		sb.append(isUpdateAllowed() ? "+U" : "-U");
		sb.append(isDeleteAllowed() ? "+D" : "-D");
	}
}
