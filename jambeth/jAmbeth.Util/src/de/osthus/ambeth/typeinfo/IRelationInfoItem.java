package de.osthus.ambeth.typeinfo;

import de.osthus.ambeth.annotation.CascadeLoadMode;

public interface IRelationInfoItem extends ITypeInfoItem
{
	CascadeLoadMode getCascadeLoadMode();

	boolean isToMany();

	boolean isManyTo();
}
