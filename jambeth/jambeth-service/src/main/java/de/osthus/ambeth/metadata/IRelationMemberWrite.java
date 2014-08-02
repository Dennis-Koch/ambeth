package de.osthus.ambeth.metadata;

import de.osthus.ambeth.annotation.CascadeLoadMode;

public interface IRelationMemberWrite
{
	void setCascadeLoadMode(CascadeLoadMode cascadeLoadMode);
}
