package de.osthus.ambeth.stream.binary;

import de.osthus.ambeth.stream.IInputSource;

public interface IBinaryInputSource extends IInputSource
{
	IBinaryInputStream deriveBinaryInputStream();
}
