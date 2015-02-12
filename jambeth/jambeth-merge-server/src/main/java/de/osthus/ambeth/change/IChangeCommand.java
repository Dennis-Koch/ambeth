package de.osthus.ambeth.change;

import de.osthus.ambeth.merge.model.IChangeContainer;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.persistence.ITable;

public interface IChangeCommand
{
	void configureFromContainer(IChangeContainer changeContainer, ITable table);

	IObjRef getReference();

	IChangeCommand addCommand(IChangeCommand other);
}
