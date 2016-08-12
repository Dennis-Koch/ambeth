package de.osthus.ambeth.bytebuffer;

import java.nio.ByteBuffer;

public interface IByteBuffer
{
	byte byteAt(long index);

	byte[] getBytes(int offset, int len);

	long length();

	void writeTo(java.io.OutputStream ost, long offset, int length) throws java.io.IOException;

	void writeTo(ByteBuffer dst, long offset, int length);
}
