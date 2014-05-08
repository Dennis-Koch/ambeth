package de.osthus.ambeth.change;

import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.util.IDisposable;

public interface IRowCommand extends IDisposable
{
	IObjRef getReference();

	void setReference(IObjRef reference);

	void addCommand(IChangeCommand command);

	IChangeCommand getCommand();
}