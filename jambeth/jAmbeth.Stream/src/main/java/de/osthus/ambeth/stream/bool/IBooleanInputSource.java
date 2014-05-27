package de.osthus.ambeth.stream.bool;

import de.osthus.ambeth.stream.IInputSource;

public interface IBooleanInputSource extends IInputSource
{
	IBooleanInputStream deriveBooleanInputStream();
}
