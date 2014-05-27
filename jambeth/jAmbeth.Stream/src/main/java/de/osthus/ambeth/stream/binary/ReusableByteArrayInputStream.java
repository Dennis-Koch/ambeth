package de.osthus.ambeth.stream.binary;

import java.io.ByteArrayInputStream;

public class ReusableByteArrayInputStream extends ByteArrayInputStream
{
	public ReusableByteArrayInputStream(byte[] buf)
	{
		super(buf);
	}

	public ReusableByteArrayInputStream(byte[] buf, int offset, int length)
	{
		super(buf, offset, length);
	}

	public void reset(byte[] buf)
	{
		this.mark = 0;
		this.pos = 0;
		this.buf = buf;
		this.count = buf.length;
	}
}
