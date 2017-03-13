package com.koch.ambeth.merge.server.change;

import com.koch.ambeth.persistence.api.IFieldMetaData;
import com.koch.ambeth.util.collections.ILinkedMap;

public interface IUpdateCommand extends IChangeCommand
{
	ILinkedMap<IFieldMetaData, Object> getItems();
}
