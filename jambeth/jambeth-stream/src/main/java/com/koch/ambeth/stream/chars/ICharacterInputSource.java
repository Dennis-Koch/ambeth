package com.koch.ambeth.stream.chars;

import com.koch.ambeth.stream.IInputSource;

public interface ICharacterInputSource extends IInputSource
{
	ICharacterInputStream deriveCharacterInputStream();
}
