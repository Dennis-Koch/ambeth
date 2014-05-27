package de.osthus.ambeth.change;

import de.osthus.ambeth.merge.model.IChangeContainer;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.persistence.ITable;
import de.osthus.ambeth.util.IDisposable;

public interface IChangeCommand extends IDisposable
{
	void configureFromContainer(IChangeContainer changeContainer, ITable table);

	IObjRef getReference();

	void setReference(IObjRef reference);

	IChangeCommand addCommand(IChangeCommand other);
}
