package de.osthus.ambeth.bytecode;

public enum EntityEnhancementHint implements IEnhancementHint
{
	HOOK;

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
