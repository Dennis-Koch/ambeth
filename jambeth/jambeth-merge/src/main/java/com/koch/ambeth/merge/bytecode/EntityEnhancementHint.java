package com.koch.ambeth.merge.bytecode;

import com.koch.ambeth.ioc.bytecode.IEnhancementHint;

public enum EntityEnhancementHint implements IEnhancementHint
{
	Instance;

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IEnhancementHint> T unwrap(Class<T> includedContextType)
	{
		if (EntityEnhancementHint.class.isAssignableFrom(includedContextType))
		{
			return (T) this;
		}
		return null;
	}

	@Override
	public String toString()
	{
		return getClass().getName();
	}
}
