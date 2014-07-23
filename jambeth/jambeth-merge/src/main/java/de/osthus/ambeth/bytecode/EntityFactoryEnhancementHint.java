package de.osthus.ambeth.bytecode;

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
