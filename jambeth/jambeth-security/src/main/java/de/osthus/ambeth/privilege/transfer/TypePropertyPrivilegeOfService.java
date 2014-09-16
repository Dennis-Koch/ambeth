package de.osthus.ambeth.privilege.transfer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import de.osthus.ambeth.util.IPrintable;

@XmlRootElement(name = "TypePropertyPrivilegeOfService", namespace = "http://schemas.osthus.de/Ambeth")
@XmlAccessorType(XmlAccessType.FIELD)
public final class TypePropertyPrivilegeOfService implements ITypePropertyPrivilegeOfService, IPrintable
{
	private static final TypePropertyPrivilegeOfService[] array = new TypePropertyPrivilegeOfService[1 << 8];

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
		array[index] = new TypePropertyPrivilegeOfService(create, read, update, delete);
	}

	public static ITypePropertyPrivilegeOfService create(Boolean create, Boolean read, Boolean update, Boolean delete)
	{
		int index = toBitValue(create, 0) + toBitValue(read, 2) + toBitValue(update, 4) + toBitValue(delete, 6);
		return array[index];
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
		int index = toBitValue(create, 0) + toBitValue(read, 2) + toBitValue(update, 4) + toBitValue(delete, 6);
		int otherIndex = toBitValue(other.create, 0) + toBitValue(other.read, 2) + toBitValue(other.update, 4) + toBitValue(other.delete, 6);
		return index == otherIndex;
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
		sb.append(isReadAllowed() != null ? isReadAllowed() ? "+R" : "-R" : "nR");
		sb.append(isCreateAllowed() != null ? isCreateAllowed() ? "+C" : "-C" : "nC");
		sb.append(isUpdateAllowed() != null ? isUpdateAllowed() ? "+U" : "-U" : "nU");
		sb.append(isDeleteAllowed() != null ? isDeleteAllowed() ? "+D" : "-D" : "nD");
	}
}
