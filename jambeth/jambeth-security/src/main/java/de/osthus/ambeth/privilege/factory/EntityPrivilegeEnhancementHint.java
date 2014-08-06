package de.osthus.ambeth.privilege.factory;

import de.osthus.ambeth.bytecode.IEnhancementHint;
import de.osthus.ambeth.bytecode.ITargetNameEnhancementHint;
import de.osthus.ambeth.privilege.model.impl.AbstractPrivilege;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Type;

public class EntityPrivilegeEnhancementHint implements IEnhancementHint, ITargetNameEnhancementHint
{
	protected final Class<?> entityType;

	protected final boolean create, read, update, delete, execute;

	public EntityPrivilegeEnhancementHint(Class<?> entityType, boolean create, boolean read, boolean update, boolean delete, boolean execute)
	{
		this.entityType = entityType;
		this.create = create;
		this.read = read;
		this.update = update;
		this.delete = delete;
		this.execute = execute;
	}

	public Class<?> getEntityType()
	{
		return entityType;
	}

	public boolean isCreate()
	{
		return create;
	}

	public boolean isRead()
	{
		return read;
	}

	public boolean isUpdate()
	{
		return update;
	}

	public boolean isDelete()
	{
		return delete;
	}

	public boolean isExecute()
	{
		return execute;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (!(obj instanceof EntityPrivilegeEnhancementHint))
		{
			return false;
		}
		EntityPrivilegeEnhancementHint other = (EntityPrivilegeEnhancementHint) obj;
		return getEntityType().equals(other.getEntityType()) && isCreate() == other.isCreate() && isRead() == other.isRead() && isUpdate() == other.isUpdate()
				&& isDelete() == other.isDelete() && isExecute() == other.isExecute();
	}

	@Override
	public int hashCode()
	{
		return getClass().hashCode() ^ getEntityType().hashCode() ^ (isCreate() ? 3 : 5) ^ (isRead() ? 7 : 11) ^ (isUpdate() ? 13 : 17)
				^ (isDelete() ? 19 : 23) ^ (isExecute() ? 27 : 31);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IEnhancementHint> T unwrap(Class<T> includedHintType)
	{
		if (EntityPrivilegeEnhancementHint.class.isAssignableFrom(includedHintType))
		{
			return (T) this;
		}
		return null;
	}

	@Override
	public String getTargetName(Class<?> typeToEnhance)
	{
		return Type.getInternalName(entityType) + "$" + AbstractPrivilege.class.getSimpleName() + "_" + AbstractPrivilege.upperOrLower(create, 'c')
				+ AbstractPrivilege.upperOrLower(read, 'r') + AbstractPrivilege.upperOrLower(update, 'u') + AbstractPrivilege.upperOrLower(delete, 'd')
				+ AbstractPrivilege.upperOrLower(execute, 'e');
	}
}
