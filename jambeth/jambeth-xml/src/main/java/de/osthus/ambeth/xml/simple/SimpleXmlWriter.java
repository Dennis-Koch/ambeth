package de.osthus.ambeth.xml.simple;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import sun.nio.cs.StreamEncoder;
import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.xml.DefaultXmlWriter;
import de.osthus.ambeth.xml.ICyclicXmlController;
import de.osthus.ambeth.xml.ICyclicXmlWriter;
import de.osthus.ambeth.xml.IWriter;
import de.osthus.ambeth.xml.appendable.ByteBufferAppendable;

public class SimpleXmlWriter implements ICyclicXmlWriter
{
	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected ICyclicXmlController xmlController;

	@Override
	public String write(Object obj)
	{
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();

		StringBuilder sb = tlObjectCollector.create(StringBuilder.class);
		try
		{
			DefaultXmlWriter writer = new DefaultXmlWriter(new AppendableStringBuilder(sb), xmlController);

			writePrefix(writer);
			writer.writeObject(obj);
			postProcess(writer);
			writePostfix(writer);
			return sb.toString();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			tlObjectCollector.dispose(sb);
		}
	}

	@Override
	public void writeToStream(OutputStream os, Object obj)
	{
		try
		{
			StreamEncoder se = StreamEncoder.forOutputStreamWriter(os, this, Properties.CHARSET_UTF_8);
			DefaultXmlWriter writer = new DefaultXmlWriter(new AppendableStreamEncoder(se), xmlController);

			writePrefix(writer);
			writer.writeObject(obj);
			postProcess(writer);
			writePostfix(writer);
			se.flushBuffer();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public void writeToChannel(final WritableByteChannel byteChannel, Object obj)
	{
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();

		final ByteBuffer byteBuffer = tlObjectCollector.create(ByteBuffer.class);
		try
		{
			DefaultXmlWriter writer = new DefaultXmlWriter(new ByteBufferAppendable(byteChannel, byteBuffer), xmlController);

			writePrefix(writer);
			writer.writeObject(obj);
			postProcess(writer);
			writePostfix(writer);

			// Flush the remaining bytes in the buffer
			byteBuffer.flip();
			try
			{
				while (byteBuffer.hasRemaining())
				{
					byteChannel.write(byteBuffer);
				}
			}
			catch (IOException e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			tlObjectCollector.dispose(ByteBuffer.class, byteBuffer);
		}
	}

	protected void writePrefix(IWriter writer)
	{
		// Intended blank
	}

	protected void writePostfix(IWriter writer)
	{
		// Intended blank
	}

	protected void postProcess(DefaultXmlWriter writer)
	{
		// Intended blank
	}
}
