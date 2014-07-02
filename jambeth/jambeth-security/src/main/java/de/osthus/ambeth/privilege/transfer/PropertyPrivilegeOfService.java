package de.osthus.ambeth.privilege.transfer;

import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.util.IPrintable;

public final class PropertyPrivilegeOfService implements IPropertyPrivilegeOfService, IPrintable
{
	private static final HashSet<PropertyPrivilegeOfService> set = new HashSet<PropertyPrivilegeOfService>(0.5f);

	static
	{
		set.add(new PropertyPrivilegeOfService(false, false, false, false));
		set.add(new PropertyPrivilegeOfService(false, false, false, true));
		set.add(new PropertyPrivilegeOfService(false, false, true, false));
		set.add(new PropertyPrivilegeOfService(false, false, true, true));
		set.add(new PropertyPrivilegeOfService(false, true, false, false));
		set.add(new PropertyPrivilegeOfService(false, true, false, true));
		set.add(new PropertyPrivilegeOfService(false, true, true, false));
		set.add(new PropertyPrivilegeOfService(false, true, true, true));
		set.add(new PropertyPrivilegeOfService(true, false, false, false));
		set.add(new PropertyPrivilegeOfService(true, false, false, true));
		set.add(new PropertyPrivilegeOfService(true, false, true, false));
		set.add(new PropertyPrivilegeOfService(true, false, true, true));
		set.add(new PropertyPrivilegeOfService(true, true, false, false));
		set.add(new PropertyPrivilegeOfService(true, true, false, true));
		set.add(new PropertyPrivilegeOfService(true, true, true, false));
		set.add(new PropertyPrivilegeOfService(true, true, true, true));
	}

	public static PropertyPrivilegeOfService create(boolean create, boolean read, boolean update, boolean delete)
	{
		return set.get(new PropertyPrivilegeOfService(create, read, update, delete));
	}

	private final boolean create;
	private final boolean read;
	private final boolean update;
	private final boolean delete;

	private PropertyPrivilegeOfService(boolean create, boolean read, boolean update, boolean delete)
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
		if (!(obj instanceof PropertyPrivilegeOfService))
		{
			return false;
		}
		PropertyPrivilegeOfService other = (PropertyPrivilegeOfService) obj;
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
