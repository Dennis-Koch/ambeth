package com.koch.ambeth.persistence.jdbc.lob;

import java.io.IOException;

import com.koch.ambeth.stream.IInputStream;
import com.koch.ambeth.stream.binary.IBinaryInputStream;

public class EmptyBlobInputStream implements IBinaryInputStream, IInputStream
{
	@Override
	public int readByte()
	{
		return -1;
	}

	@Override
	public void close() throws IOException
	{
		// Intended blank
	}
}
