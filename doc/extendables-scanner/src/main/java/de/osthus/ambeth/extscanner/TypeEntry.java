package de.osthus.ambeth.extscanner;

import de.osthus.classbrowser.java.TypeDescription;

public class TypeEntry implements Comparable<TypeEntry>
{
	public final TypeDescription typeDesc;

	public ModuleEntry moduleEntry;

	public TypeEntry(TypeDescription typeDesc)
	{
		super();
		this.typeDesc = typeDesc;
	}

	@Override
	public int compareTo(TypeEntry o)
	{
		return typeDesc.getFullTypeName().compareTo(o.typeDesc.getFullTypeName());
	}
}
