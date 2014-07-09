package de.osthus.ambeth.privilege.transfer;

import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.util.IPrintable;

public final class TypePropertyPrivilegeOfService implements ITypePropertyPrivilegeOfService, IPrintable
{
	private static final HashSet<TypePropertyPrivilegeOfService> set = new HashSet<TypePropertyPrivilegeOfService>(0.5f);

	static
	{
		set.add(new TypePropertyPrivilegeOfService(false, false, false, false));
		set.add(new TypePropertyPrivilegeOfService(false, false, false, true));
		set.add(new TypePropertyPrivilegeOfService(false, false, true, false));
		set.add(new TypePropertyPrivilegeOfService(false, false, true, true));
		set.add(new TypePropertyPrivilegeOfService(false, true, false, false));
		set.add(new TypePropertyPrivilegeOfService(false, true, false, true));
		set.add(new TypePropertyPrivilegeOfService(false, true, true, false));
		set.add(new TypePropertyPrivilegeOfService(false, true, true, true));
		set.add(new TypePropertyPrivilegeOfService(true, false, false, false));
		set.add(new TypePropertyPrivilegeOfService(true, false, false, true));
		set.add(new TypePropertyPrivilegeOfService(true, false, true, false));
		set.add(new TypePropertyPrivilegeOfService(true, false, true, true));
		set.add(new TypePropertyPrivilegeOfService(true, true, false, false));
		set.add(new TypePropertyPrivilegeOfService(true, true, false, true));
		set.add(new TypePropertyPrivilegeOfService(true, true, true, false));
		set.add(new TypePropertyPrivilegeOfService(true, true, true, true));
	}

	public static TypePropertyPrivilegeOfService create(Boolean create, Boolean read, Boolean update, Boolean delete)
	{
		return set.get(new TypePropertyPrivilegeOfService(create, read, update, delete));
	}

	private final Boolean create;
	private final Boolean read;
	private final Boolean update;
	private final Boolean delete;

	private TypePropertyPrivilegeOfService(Boolean create, Boolean read, Boolean update, Boolean delete)
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
		if (!(obj instanceof TypePropertyPrivilegeOfService))
		{
			return false;
		}
		TypePropertyPrivilegeOfService other = (TypePropertyPrivilegeOfService) obj;
		return create == other.create && read == other.read && update == other.update && delete == other.delete;
	}

	@Override
	public int hashCode()
	{
		return (create != null ? create.hashCode() : 1) * 7 ^ (read != null ? read.hashCode() : 1) * 17 ^ (update != null ? update.hashCode() : 1) * 11
				^ (delete != null ? delete.hashCode() : 1) * 13;
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
