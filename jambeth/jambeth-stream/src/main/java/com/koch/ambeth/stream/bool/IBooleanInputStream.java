package com.koch.ambeth.stream.bool;

import com.koch.ambeth.stream.IInputStream;

public interface IBooleanInputStream extends IInputStream
{
	boolean hasBoolean();

	boolean readBoolean();
}