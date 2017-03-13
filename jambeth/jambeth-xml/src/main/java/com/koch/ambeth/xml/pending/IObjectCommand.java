package com.koch.ambeth.xml.pending;

import com.koch.ambeth.xml.IReader;

public interface IObjectCommand
{
	IObjectFuture getObjectFuture();

	void execute(IReader reader);
}
