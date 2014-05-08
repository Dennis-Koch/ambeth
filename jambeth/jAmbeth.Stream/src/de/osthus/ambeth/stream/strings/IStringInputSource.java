package de.osthus.ambeth.stream.strings;

import de.osthus.ambeth.stream.IInputSource;

public interface IStringInputSource extends IInputSource
{
	IStringInputStream deriveStringInputStream();
}
