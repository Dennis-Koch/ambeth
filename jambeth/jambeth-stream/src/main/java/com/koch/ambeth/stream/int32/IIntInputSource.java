package com.koch.ambeth.stream.int32;

import com.koch.ambeth.stream.IInputSource;

public interface IIntInputSource extends IInputSource
{
	IIntInputStream deriveIntInputStream();
}
