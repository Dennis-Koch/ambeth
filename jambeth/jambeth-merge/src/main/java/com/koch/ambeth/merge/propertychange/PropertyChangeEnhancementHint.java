package com.koch.ambeth.merge.propertychange;

import com.koch.ambeth.ioc.bytecode.IEnhancementHint;

public enum PropertyChangeEnhancementHint implements IEnhancementHint
{
	PropertyChangeEnhancementHint;

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IEnhancementHint> T unwrap(Class<T> includedContextType)
	{
		if (PropertyChangeEnhancementHint.class.isAssignableFrom(includedContextType))
		{
			return (T) this;
		}
		return null;
	}
}
