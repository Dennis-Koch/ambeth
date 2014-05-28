package de.osthus.ambeth.merge.model;

import de.osthus.ambeth.annotation.XmlType;

@XmlType
public interface IChangeContainer
{
	IObjRef getReference();

	void setReference(IObjRef reference);
}
