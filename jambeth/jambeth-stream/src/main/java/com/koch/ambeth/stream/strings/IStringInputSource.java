package com.koch.ambeth.stream.strings;

import com.koch.ambeth.stream.IInputSource;

public interface IStringInputSource extends IInputSource
{
	IStringInputStream deriveStringInputStream();
}
