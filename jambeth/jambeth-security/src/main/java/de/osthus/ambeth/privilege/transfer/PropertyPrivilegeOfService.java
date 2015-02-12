package de.osthus.ambeth.privilege.transfer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import de.osthus.ambeth.privilege.model.impl.PropertyPrivilegeImpl;
import de.osthus.ambeth.util.IPrintable;

@XmlRootElement(name = "PropertyPrivilegeOfService", namespace = "http://schemas.osthus.de/Ambeth")
@XmlAccessorType(XmlAccessType.FIELD)
public final class PropertyPrivilegeOfService implements IPropertyPrivilegeOfService, IPrintable
{
	private static final PropertyPrivilegeOfService[] array = new PropertyPrivilegeOfService[PropertyPrivilegeImpl.arraySizeForIndex()];

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

	private static void put(boolean create, boolean read, boolean update, boolean delete)
	{
		int index = PropertyPrivilegeImpl.calcIndex(create, read, update, delete);
		array[index] = new PropertyPrivilegeOfService(create, read, update, delete);
	}

	public static IPropertyPrivilegeOfService create(boolean create, boolean read, boolean update, boolean delete)
	{
		int index = PropertyPrivilegeImpl.calcIndex(create, read, update, delete);
		return array[index];
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
		return PropertyPrivilegeImpl.calcIndex(create, read, update, delete);
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
