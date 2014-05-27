package de.osthus.ambeth.stream.bool;

import de.osthus.ambeth.stream.IInputStream;

public interface IBooleanInputStream extends IInputStream
{
	boolean hasBoolean();

	boolean readBoolean();
}