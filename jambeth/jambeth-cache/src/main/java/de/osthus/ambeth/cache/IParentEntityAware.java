package de.osthus.ambeth.cache;

import de.osthus.ambeth.metadata.Member;

public interface IParentEntityAware
{
	void setParentEntity(Object parentEntity, Member member);
}
