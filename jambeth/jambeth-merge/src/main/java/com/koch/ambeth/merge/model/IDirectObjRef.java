package com.koch.ambeth.merge.model;

import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.annotation.XmlType;

@XmlType
public interface IDirectObjRef extends IObjRef
{
	Object getDirect();

	void setDirect(Object direct);

	int getCreateContainerIndex();

	void setCreateContainerIndex(int createContainerIndex);
}
