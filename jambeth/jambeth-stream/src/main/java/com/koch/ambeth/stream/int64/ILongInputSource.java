package com.koch.ambeth.stream.int64;

import com.koch.ambeth.stream.IInputSource;

public interface ILongInputSource extends IInputSource
{
	ILongInputStream deriveLongInputStream();
}
