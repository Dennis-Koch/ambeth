package de.osthus.ambeth.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class ByteBufferOutputStream extends OutputStream
{
	protected final ByteBuffer[] byteBuffers;

	protected ByteBuffer currentByteBuffer;

	protected int currentIndex = -1;

	public ByteBufferOutputStream(ByteBuffer... byteBuffers)
	{
		this.byteBuffers = byteBuffers;
		incCurrentIndex();
	}

	public void writeSpaceFill()
	{
		while (true)
		{
			while (currentByteBuffer.hasRemaining())
			{
				currentByteBuffer.put((byte) ' ');
			}
			if (byteBuffers.length > currentIndex + 1)
			{
				incCurrentIndex();
			}
			else
			{
				break;
			}
		}
	}

	protected void incCurrentIndex()
	{
		this.currentIndex++;
		currentByteBuffer = byteBuffers[currentIndex];
	}

	@Override
	public void write(int b) throws IOException
	{
		while (!currentByteBuffer.hasRemaining())
		{
			incCurrentIndex();
		}
		currentByteBuffer.put((byte) b);
	}
}
