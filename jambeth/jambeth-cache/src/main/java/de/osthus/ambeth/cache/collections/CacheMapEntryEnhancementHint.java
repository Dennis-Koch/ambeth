package de.osthus.ambeth.cache.collections;

import java.io.Serializable;

import de.osthus.ambeth.bytecode.IEnhancementHint;
import de.osthus.ambeth.bytecode.ITargetNameEnhancementHint;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Type;

public class CacheMapEntryEnhancementHint implements IEnhancementHint, ITargetNameEnhancementHint, Serializable
{
	private static final long serialVersionUID = -7179620109557840890L;

	protected final Class<?> entityType;

	protected final byte idIndex;

	public CacheMapEntryEnhancementHint(Class<?> entityType, byte idIndex)
	{
		this.entityType = entityType;
		this.idIndex = idIndex;
	}

	public Class<?> getEntityType()
	{
		return entityType;
	}

	public byte getIdIndex()
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
		if (!(obj instanceof CacheMapEntryEnhancementHint))
		{
			return false;
		}
		CacheMapEntryEnhancementHint other = (CacheMapEntryEnhancementHint) obj;
		return getEntityType().equals(other.getEntityType()) && getIdIndex() == other.getIdIndex();
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
		if (CacheMapEntryEnhancementHint.class.isAssignableFrom(includedHintType))
		{
			return (T) this;
		}
		return null;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + ": " + getTargetName(null);
	}

	@Override
	public String getTargetName(Class<?> typeToEnhance)
	{
		return Type.getInternalName(entityType) + "$" + CacheMapEntry.class.getSimpleName() + "$" + (idIndex == ObjRef.PRIMARY_KEY_INDEX ? "PK" : idIndex);
	}
}
