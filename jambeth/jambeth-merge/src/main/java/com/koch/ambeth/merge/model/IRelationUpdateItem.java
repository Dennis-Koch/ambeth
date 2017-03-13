package com.koch.ambeth.merge.model;

import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.annotation.XmlType;

@XmlType(name = "IRUI")
public interface IRelationUpdateItem extends IUpdateItem
{
	IObjRef[] getAddedORIs();

	IObjRef[] getRemovedORIs();
}
