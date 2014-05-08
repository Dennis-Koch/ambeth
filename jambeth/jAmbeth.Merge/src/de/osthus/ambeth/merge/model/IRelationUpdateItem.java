package de.osthus.ambeth.merge.model;

import de.osthus.ambeth.annotation.XmlType;

@XmlType(name = "IRUI")
public interface IRelationUpdateItem extends IUpdateItem
{
	IObjRef[] getAddedORIs();

	IObjRef[] getRemovedORIs();
}
