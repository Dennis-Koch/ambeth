package de.osthus.ambeth.stream.chars;

import de.osthus.ambeth.stream.IInputSource;

public interface ICharacterInputSource extends IInputSource
{
	ICharacterInputStream deriveCharacterInputStream();
}
