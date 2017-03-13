package com.koch.ambeth.merge.model;

import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.annotation.XmlType;

@XmlType
public interface IChangeContainer
{
	IObjRef getReference();

	void setReference(IObjRef reference);
}
