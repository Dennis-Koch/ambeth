package de.osthus.ambeth.change;

import de.osthus.ambeth.collections.ILinkedMap;

public interface ICreateCommand extends IChangeCommand
{
	ILinkedMap<String, Object> getItems();
}
