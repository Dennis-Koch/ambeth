package com.koch.ambeth.stream.float32;

import com.koch.ambeth.stream.IInputSource;

public interface IFloatInputSource extends IInputSource
{
	IFloatInputStream deriveFloatInputStream();
}
