package com.koch.ambeth.stream.float32;

import com.koch.ambeth.stream.IInputStream;

public interface IFloatInputStream extends IInputStream
{
	boolean hasFloat();

	float readFloat();
}