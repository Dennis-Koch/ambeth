package com.koch.ambeth.stream.strings;

import com.koch.ambeth.stream.IInputStream;

public interface IStringInputStream extends IInputStream
{
	boolean hasString();

	String readString();
}