package de.osthus.ambeth.stream.chars;

import java.io.Closeable;

public interface ICharacterInputStream extends Closeable
{
	int readChar();
}