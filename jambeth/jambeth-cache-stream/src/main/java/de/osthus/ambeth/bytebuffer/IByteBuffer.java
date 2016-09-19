package de.osthus.ambeth.bytebuffer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

public interface IByteBuffer
{
	byte byteAt(long offset);

	byte[] getBytes(long offset, int len);

	long length();

	void writeTo(OutputStream dst, long offset, int length) throws IOException;

	void writeTo(ByteBuffer dst, long offset, int length);

	void writeTo(WritableByteChannel dst, long offset, int length) throws IOException;
}
