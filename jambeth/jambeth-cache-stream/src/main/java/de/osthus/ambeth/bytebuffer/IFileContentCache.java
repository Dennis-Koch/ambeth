package de.osthus.ambeth.bytebuffer;

import java.nio.ByteBuffer;

public interface IFileContentCache
{
	IByteBuffer getByteBuffer(FileKey fileKey);

	ByteBuffer[] getContent(FileKey fileKey, long position, long length);

	ByteBuffer getContent(FileKey fileKey, long position);

	void releaseByteBuffer(ByteBuffer byteBuffer);
}