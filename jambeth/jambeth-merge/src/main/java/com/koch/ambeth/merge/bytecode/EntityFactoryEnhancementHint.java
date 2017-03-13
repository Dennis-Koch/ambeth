package com.koch.ambeth.merge.bytecode;

import com.koch.ambeth.ioc.bytecode.IEnhancementHint;

public enum EntityFactoryEnhancementHint implements IEnhancementHint
{
	EntityFactoryEnhancementHint;

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IEnhancementHint> T unwrap(Class<T> includedContextType)
	{
		if (EntityFactoryEnhancementHint.class.isAssignableFrom(includedContextType))
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
