package de.osthus.ambeth.cache.rootcachevalue;

import java.io.Serializable;

import de.osthus.ambeth.bytecode.IEnhancementHint;
import de.osthus.ambeth.bytecode.ITargetNameEnhancementHint;
import de.osthus.ambeth.repackaged.org.objectweb.asm.Type;

public class RootCacheValueEnhancementHint implements IEnhancementHint, ITargetNameEnhancementHint, Serializable
{
	private static final long serialVersionUID = 5722369699026975653L;

	protected final Class<?> entityType;

	public RootCacheValueEnhancementHint(Class<?> entityType)
	{
		this.entityType = entityType;
	}

	public Class<?> getEntityType()
	{
		return entityType;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (!(obj instanceof RootCacheValueEnhancementHint))
		{
			return false;
		}
		RootCacheValueEnhancementHint other = (RootCacheValueEnhancementHint) obj;
		return getEntityType().equals(other.getEntityType());
	}

	@Override
	public int hashCode()
	{
		return RootCacheValueEnhancementHint.class.hashCode() ^ getEntityType().hashCode();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IEnhancementHint> T unwrap(Class<T> includedHintType)
	{
		if (RootCacheValueEnhancementHint.class.isAssignableFrom(includedHintType))
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
		return Type.getInternalName(entityType) + "$" + RootCacheValue.class.getSimpleName();
	}
}
