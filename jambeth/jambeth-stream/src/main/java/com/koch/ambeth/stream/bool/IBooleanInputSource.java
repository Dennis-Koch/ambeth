package com.koch.ambeth.stream.bool;

import com.koch.ambeth.stream.IInputSource;

public interface IBooleanInputSource extends IInputSource
{
	IBooleanInputStream deriveBooleanInputStream();
}
