package com.koch.ambeth.stream.binary;

import com.koch.ambeth.stream.IInputSource;

public interface IBinaryInputSource extends IInputSource
{
	IBinaryInputStream deriveBinaryInputStream();
}
