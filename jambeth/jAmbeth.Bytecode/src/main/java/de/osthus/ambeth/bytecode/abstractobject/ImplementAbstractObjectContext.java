package de.osthus.ambeth.bytecode.abstractobject;

import de.osthus.ambeth.bytecode.IEnhancementHint;

/**
 * The context for the {@link ImplementAbstractObjectFactory}
 */
public enum ImplementAbstractObjectContext implements IEnhancementHint
{
	INSTANCE;

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IEnhancementHint> T unwrap(Class<T> includedContextType)
	{
		if (ImplementAbstractObjectContext.class.isAssignableFrom(includedContextType))
		{
			return (T) this;
		}
		return null;
	}
}
