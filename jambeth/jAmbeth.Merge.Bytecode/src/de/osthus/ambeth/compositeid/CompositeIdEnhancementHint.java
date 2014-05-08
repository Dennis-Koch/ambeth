package de.osthus.ambeth.compositeid;

import de.osthus.ambeth.bytecode.IEnhancementHint;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;

public class CompositeIdEnhancementHint implements IEnhancementHint
{
	private final ITypeInfoItem[] idMembers;

	public CompositeIdEnhancementHint(ITypeInfoItem[] idMembers)
	{
		this.idMembers = idMembers;
	}

	public ITypeInfoItem[] getIdMembers()
	{
		return idMembers;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IEnhancementHint> T unwrap(Class<T> includedContextType)
	{
		if (CompositeIdEnhancementHint.class.isAssignableFrom(includedContextType))
		{
			return (T) this;
		}
		return null;
	}
}
