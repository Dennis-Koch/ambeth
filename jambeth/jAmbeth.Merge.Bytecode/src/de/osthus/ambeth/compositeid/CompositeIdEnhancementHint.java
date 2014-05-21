package de.osthus.ambeth.compositeid;

import de.osthus.ambeth.bytecode.IEnhancementHint;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;
import de.osthus.ambeth.util.IPrintable;

public class CompositeIdEnhancementHint implements IEnhancementHint, IPrintable
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

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	@Override
	public void toString(StringBuilder sb)
	{
		sb.append(getClass().getName()).append(": ");
		for (int a = 0, size = idMembers.length; a < size; a++)
		{
			if (a > 0)
			{
				sb.append(',');
			}
			sb.append(idMembers[a].getName());
		}
	}
}
