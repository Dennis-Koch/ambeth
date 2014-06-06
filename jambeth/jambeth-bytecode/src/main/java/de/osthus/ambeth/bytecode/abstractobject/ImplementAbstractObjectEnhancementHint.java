package de.osthus.ambeth.bytecode.abstractobject;

import de.osthus.ambeth.bytecode.IEnhancementHint;

/**
 * The context for the {@link ImplementAbstractObjectFactory}
 */
public enum ImplementAbstractObjectEnhancementHint implements IEnhancementHint
{
	ImplementAbstractObjectEnhancementHint;

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IEnhancementHint> T unwrap(Class<T> includedContextType)
	{
		if (ImplementAbstractObjectEnhancementHint.class.isAssignableFrom(includedContextType))
		{
			return (T) this;
		}
		return null;
	}
}
