package de.osthus.ambeth.xml.pending;

import de.osthus.ambeth.xml.IReader;

public interface IObjectCommand
{
	IObjectFuture getObjectFuture();

	void execute(IReader reader);
}
