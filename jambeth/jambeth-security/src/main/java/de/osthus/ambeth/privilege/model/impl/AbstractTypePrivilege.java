package de.osthus.ambeth.privilege.model.impl;

import de.osthus.ambeth.privilege.model.ITypePrivilege;
import de.osthus.ambeth.privilege.model.ITypePropertyPrivilege;
import de.osthus.ambeth.util.IImmutableType;
import de.osthus.ambeth.util.IPrintable;

public abstract class AbstractTypePrivilege implements ITypePrivilege, IPrintable, IImmutableType
{
	public static int arraySizeForIndex()
	{
		return 81 * 3;
	}

	public static int calcIndex(Boolean create, Boolean read, Boolean update, Boolean delete, Boolean execute)
	{
		return toBitValue(create, 1, 1 * 2) + toBitValue(read, 3, 3 * 2) + toBitValue(update, 9, 9 * 2) + toBitValue(delete, 27, 27 * 2)
				+ toBitValue(execute, 81, 81 * 2);
	}

	public static int toBitValue(Boolean value, int valueIfTrue, int valueIfFalse)
	{
		if (value == null)
		{
			return 0;
		}
		return value.booleanValue() ? valueIfTrue : valueIfFalse;
	}

	public AbstractTypePrivilege(Boolean create, Boolean read, Boolean update, Boolean delete, Boolean execute,
			ITypePropertyPrivilege[] primitivePropertyPrivileges, ITypePropertyPrivilege[] relationPropertyPrivileges)
	{
		// intended blank
	}

	@Override
	public abstract ITypePropertyPrivilege getPrimitivePropertyPrivilege(int primitiveIndex);

	@Override
	public abstract ITypePropertyPrivilege getRelationPropertyPrivilege(int relationIndex);

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
		sb.append(AbstractPrivilege.upperOrLower(isExecuteAllowed(), 'e'));
	}
}
