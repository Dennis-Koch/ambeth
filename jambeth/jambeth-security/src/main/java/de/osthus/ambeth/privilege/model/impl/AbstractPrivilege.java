package de.osthus.ambeth.privilege.model.impl;

import de.osthus.ambeth.privilege.model.IPrivilege;
import de.osthus.ambeth.privilege.model.IPropertyPrivilege;
import de.osthus.ambeth.util.IImmutableType;
import de.osthus.ambeth.util.IPrintable;

public abstract class AbstractPrivilege implements IPrivilege, IPrintable, IImmutableType
{
	public static int arraySizeForIndex()
	{
		return 1 << 8;
	}

	public static int calcIndex(boolean create, boolean read, boolean update, boolean delete, boolean execute)
	{
		return toBitValue(create, 0) + toBitValue(read, 1) + toBitValue(update, 2) + toBitValue(delete, 3) + toBitValue(execute, 4);
	}

	public static int toBitValue(boolean value, int startingBit)
	{
		return value ? 1 << startingBit : 0;
	}

	public static char upperOrLower(boolean flag, char oneChar)
	{
		if (flag)
		{
			return Character.toUpperCase(oneChar);
		}
		return Character.toLowerCase(oneChar);
	}

	public static char upperOrLower(Boolean flag, char oneChar)
	{
		if (flag == null)
		{
			return '_';
		}
		if (flag.booleanValue())
		{
			return Character.toUpperCase(oneChar);
		}
		return Character.toLowerCase(oneChar);
	}

	public AbstractPrivilege(boolean create, boolean read, boolean update, boolean delete, boolean execute, IPropertyPrivilege[] primitivePropertyPrivileges,
			IPropertyPrivilege[] relationPropertyPrivileges)
	{
		// intended blank
	}

	@Override
	public abstract IPropertyPrivilege getPrimitivePropertyPrivilege(int primitiveIndex);

	@Override
	public abstract IPropertyPrivilege getRelationPropertyPrivilege(int relationIndex);

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
		sb.append(upperOrLower(isCreateAllowed(), 'c'));
		sb.append(upperOrLower(isReadAllowed(), 'r'));
		sb.append(upperOrLower(isUpdateAllowed(), 'u'));
		sb.append(upperOrLower(isDeleteAllowed(), 'd'));
		sb.append(upperOrLower(isExecuteAllowed(), 'e'));
	}
}
