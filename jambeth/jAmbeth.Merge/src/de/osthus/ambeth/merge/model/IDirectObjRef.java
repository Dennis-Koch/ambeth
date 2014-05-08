package de.osthus.ambeth.merge.model;

import de.osthus.ambeth.annotation.XmlType;

@XmlType
public interface IDirectObjRef extends IObjRef
{
	Object getDirect();

	void setDirect(Object direct);

	int getCreateContainerIndex();

	void setCreateContainerIndex(int createContainerIndex);
}
