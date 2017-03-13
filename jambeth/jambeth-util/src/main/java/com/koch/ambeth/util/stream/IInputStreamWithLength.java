package com.koch.ambeth.util.stream;

import java.io.InputStream;

public interface IInputStreamWithLength
{
	InputStream getInputStream();

	int getOverallLength();
}
