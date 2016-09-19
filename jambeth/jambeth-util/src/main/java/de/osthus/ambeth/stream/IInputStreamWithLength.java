package de.osthus.ambeth.stream;

import java.io.InputStream;

public interface IInputStreamWithLength
{
	InputStream getInputStream();

	int getOverallLength();
}
