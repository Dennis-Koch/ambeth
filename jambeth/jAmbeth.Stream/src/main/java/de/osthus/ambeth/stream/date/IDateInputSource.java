package de.osthus.ambeth.stream.date;

import de.osthus.ambeth.stream.IInputSource;

public interface IDateInputSource extends IInputSource
{
	IDateInputStream deriveDateInputStream();
}
