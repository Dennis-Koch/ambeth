package de.osthus.ambeth.privilege.model.impl;

import de.osthus.ambeth.privilege.model.IPrivilege;
import de.osthus.ambeth.privilege.model.IPropertyPrivilege;
import de.osthus.ambeth.util.IImmutableType;
import de.osthus.ambeth.util.IPrintable;

public abstract class AbstractPrivilege implements IPrivilege, IPrintable, IImmutableType
{
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
