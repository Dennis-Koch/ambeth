package com.koch.ambeth.stream.date;

import com.koch.ambeth.stream.IInputSource;

public interface IDateInputSource extends IInputSource
{
	IDateInputStream deriveDateInputStream();
}
