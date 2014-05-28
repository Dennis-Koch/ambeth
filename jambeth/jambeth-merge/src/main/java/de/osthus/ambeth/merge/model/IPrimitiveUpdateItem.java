package de.osthus.ambeth.merge.model;

import de.osthus.ambeth.annotation.XmlType;

@XmlType(name = "IPUI")
public interface IPrimitiveUpdateItem extends IUpdateItem
{
	Object getNewValue();
}
