package de.osthus.ambeth.bytebuffer;

public interface IByteBuffer
{
	byte byteAt(long index);

	byte[] getBytes(int offset, int len);

	long length();

	void writeToOutputStream(java.io.OutputStream ost, long offset, long length) throws java.io.IOException;
}
