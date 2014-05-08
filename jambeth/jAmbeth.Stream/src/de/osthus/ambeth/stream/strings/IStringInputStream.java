package de.osthus.ambeth.stream.strings;

import de.osthus.ambeth.stream.IInputStream;

public interface IStringInputStream extends IInputStream
{
	boolean hasString();

	String readString();
}