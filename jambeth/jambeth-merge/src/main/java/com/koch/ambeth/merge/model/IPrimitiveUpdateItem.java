package com.koch.ambeth.merge.model;

import com.koch.ambeth.util.annotation.XmlType;

@XmlType(name = "IPUI")
public interface IPrimitiveUpdateItem extends IUpdateItem
{
	Object getNewValue();
}
