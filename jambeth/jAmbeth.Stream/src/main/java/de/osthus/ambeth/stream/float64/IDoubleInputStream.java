package de.osthus.ambeth.stream.float64;

import de.osthus.ambeth.stream.IInputStream;

public interface IDoubleInputStream extends IInputStream
{
	boolean hasDouble();

	double readDouble();
}