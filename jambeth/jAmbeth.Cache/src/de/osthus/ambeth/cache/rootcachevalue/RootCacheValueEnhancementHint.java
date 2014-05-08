package de.osthus.ambeth.cache.rootcachevalue;

import net.sf.cglib.asm.Type;
import de.osthus.ambeth.bytecode.IEnhancementHint;
import de.osthus.ambeth.bytecode.ITargetNameEnhancementHint;

public class RootCacheValueEnhancementHint implements IEnhancementHint, ITargetNameEnhancementHint
{
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
	public String getTargetName(Class<?> typeToEnhance)
	{
		return Type.getInternalName(entityType) + "$" + RootCacheValue.class.getSimpleName();
	}
}
