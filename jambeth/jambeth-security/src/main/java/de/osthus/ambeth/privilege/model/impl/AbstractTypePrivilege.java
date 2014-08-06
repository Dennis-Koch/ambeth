package de.osthus.ambeth.privilege.model.impl;

import de.osthus.ambeth.privilege.model.ITypePrivilege;
import de.osthus.ambeth.privilege.model.ITypePropertyPrivilege;
import de.osthus.ambeth.util.IImmutableType;
import de.osthus.ambeth.util.IPrintable;

public abstract class AbstractTypePrivilege implements ITypePrivilege, IPrintable, IImmutableType
{
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
