package de.osthus.ambeth.privilege.model.impl;

import de.osthus.ambeth.privilege.model.ITypePrivilege;
import de.osthus.ambeth.privilege.model.ITypePropertyPrivilege;
import de.osthus.ambeth.util.IPrintable;

public final class SkipAllTypePrivilege implements ITypePrivilege, IPrintable
{
	public static final ITypePrivilege INSTANCE = new SkipAllTypePrivilege();

	private static final ITypePropertyPrivilege skipAllPropertyPrivilege = TypePropertyPrivilegeImpl.create(null, null, null, null);

	private SkipAllTypePrivilege()
	{
		// intended blank
	}

	@Override
	public ITypePropertyPrivilege getDefaultPropertyPrivilegeIfValid()
	{
		return skipAllPropertyPrivilege;
	}

	@Override
	public ITypePropertyPrivilege getPrimitivePropertyPrivilege(int primitiveIndex)
	{
		return skipAllPropertyPrivilege;
	}

	@Override
	public ITypePropertyPrivilege getRelationPropertyPrivilege(int relationIndex)
	{
		return skipAllPropertyPrivilege;
	}

	@Override
	public Boolean isCreateAllowed()
	{
		return null;
	}

	@Override
	public Boolean isReadAllowed()
	{
		return null;
	}

	@Override
	public Boolean isUpdateAllowed()
	{
		return null;
	}

	@Override
	public Boolean isDeleteAllowed()
	{
		return null;
	}

	@Override
	public Boolean isExecuteAllowed()
	{
		return null;
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
		sb.append(AbstractPrivilege.upperOrLower(isExecuteAllowed(), 'e'));
	}
}
