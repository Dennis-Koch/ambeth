package de.osthus.ambeth.stream.float32;

import de.osthus.ambeth.stream.IInputStream;

public interface IFloatInputStream extends IInputStream
{
	boolean hasFloat();

	float readFloat();
}