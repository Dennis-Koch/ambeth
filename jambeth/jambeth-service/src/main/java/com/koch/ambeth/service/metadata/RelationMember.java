package com.koch.ambeth.service.metadata;

import com.koch.ambeth.util.annotation.CascadeLoadMode;

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
