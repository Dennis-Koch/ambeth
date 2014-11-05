package de.osthus.ambeth.metadata;

import de.osthus.ambeth.annotation.CascadeLoadMode;

public abstract class RelationMember extends Member
{
	@Override
	public Object getNullEquivalentValue()
	{
		return null;
	}

	public abstract CascadeLoadMode getCascadeLoadMode();

	public abstract boolean isManyTo();
}
