package de.osthus.ambeth.appendable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;

public class ByteBufferAppendable implements IAppendable
{
	private final byte[] byteArray1 = new byte[1];

	private final byte[] byteArray2 = new byte[2];

	private final byte[] byteArray3 = new byte[3];

	private final byte[] byteArray4 = new byte[4];

	protected final ByteBuffer buffer;

	protected final WritableByteChannel targetChannel;

	public ByteBufferAppendable(WritableByteChannel targetChannel, ByteBuffer buffer)
	{
		this.buffer = buffer;
		this.targetChannel = targetChannel;
	}

	@Override
	public IAppendable append(CharSequence value)
	{
		for (int writePos = 0, size = value.length(); writePos < size; writePos++)
		{
			append(value.charAt(writePos));
		}
		return this;
	}

	@Override
	public IAppendable append(char value)
	{
		byte[] byteValue = encodeUTF8(value);
		append(byteValue);
		return this;
	}

	protected void append(byte[] value)
	{
		ByteBuffer byteBuffer = buffer;

		if (byteBuffer.remaining() < value.length)
		{
			// buffer is to full. Now we write it to the channel
			byteBuffer.flip();
			try
			{
				while (byteBuffer.hasRemaining())
				{
					targetChannel.write(byteBuffer);
				}
			}
			catch (IOException e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
			// clear the buffer to activate the "has remaining" state again
			byteBuffer.clear();
		}

		byteBuffer.put(value);
	}

	protected byte[] encodeUTF8(char value)
	{
		if (value < 128)
		{
			byte[] output = byteArray1;
			output[0] = (byte) value;
			return output;
		}
		if (value < 0x800)
		{
			byte[] output = byteArray2;
			output[0] = (byte) (((value & 0x7c0) >> 6) | 0xc0);
			output[1] = (byte) ((value & 0x3f) | 0x80);
			return output;
		}
		if (value < 0xe000)
		{
			byte[] output = byteArray3;
			output[0] = (byte) (((value & 0xf000) >> 12) | 0xe0);
			output[1] = (byte) (((value & 0xfc) >> 6) | 0x80);
			output[2] = (byte) ((value & 0x3f) | 0x80);
			return output;
		}

		byte[] output = byteArray4;
		output[0] = (byte) (((value & 0x1c0000) >> 18) | 0xf0);
		output[1] = (byte) (((value & 0x3f0) >> 12) | 0x80);
		output[2] = (byte) (((value & 0xfc) >> 6) | 0x80);
		output[3] = (byte) ((value & 0x3f) | 0x80);
		return output;
	}
}
