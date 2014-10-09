package de.osthus.ambeth.objrefstore;

import de.osthus.ambeth.bytecode.IEnhancementHint;
import de.osthus.ambeth.bytecode.ITargetNameEnhancementHint;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Type;

public class ObjRefStoreEnhancementHint implements IEnhancementHint, ITargetNameEnhancementHint
{
	protected final Class<?> entityType;

	protected final int idIndex;

	public ObjRefStoreEnhancementHint(Class<?> entityType, int idIndex)
	{
		this.entityType = entityType;
		this.idIndex = idIndex;
	}

	public Class<?> getEntityType()
	{
		return entityType;
	}

	public int getIdIndex()
	{
		return idIndex;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (!getClass().equals(obj.getClass()))
		{
			return false;
		}
		ObjRefStoreEnhancementHint other = (ObjRefStoreEnhancementHint) obj;
		return getEntityType().equals(other.getEntityType()) && idIndex == other.getIdIndex();
	}

	@Override
	public int hashCode()
	{
		return getClass().hashCode() ^ getEntityType().hashCode() ^ getIdIndex();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IEnhancementHint> T unwrap(Class<T> includedHintType)
	{
		if (ObjRefStoreEnhancementHint.class.equals(includedHintType))
		{
			return (T) this;
		}
		return null;
	}

	@Override
	public String getTargetName(Class<?> typeToEnhance)
	{
		return Type.getInternalName(entityType) + "$" + ObjRefStore.class.getSimpleName() + "$" + (idIndex == ObjRef.PRIMARY_KEY_INDEX ? "PK" : "AK" + idIndex);
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + ": " + entityType.getSimpleName() + "-" + (idIndex == ObjRef.PRIMARY_KEY_INDEX ? "PK" : "AK" + idIndex);
	}
}
