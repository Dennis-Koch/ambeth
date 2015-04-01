package de.osthus.ambeth.change;

import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.persistence.IFieldMetaData;

public interface ICreateCommand extends IChangeCommand
{
	ILinkedMap<IFieldMetaData, Object> getItems();
}
