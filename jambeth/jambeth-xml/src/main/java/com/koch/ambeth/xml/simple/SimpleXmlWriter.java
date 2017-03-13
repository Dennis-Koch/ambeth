package com.koch.ambeth.xml.simple;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.util.appendable.AppendableStringBuilder;
import com.koch.ambeth.util.appendable.ByteBufferAppendable;
import com.koch.ambeth.util.appendable.WriterAppendable;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;
import com.koch.ambeth.xml.DefaultXmlWriter;
import com.koch.ambeth.xml.ICyclicXmlController;
import com.koch.ambeth.xml.ICyclicXmlWriter;
import com.koch.ambeth.xml.IWriter;

public class SimpleXmlWriter implements ICyclicXmlWriter {
	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected ICyclicXmlController xmlController;

	@Override
	public String write(Object obj) {
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();

		StringBuilder sb = tlObjectCollector.create(StringBuilder.class);
		try {
			DefaultXmlWriter writer =
					new DefaultXmlWriter(new AppendableStringBuilder(sb), xmlController);

			writePrefix(writer);
			writer.writeObject(obj);
			postProcess(writer);
			writePostfix(writer);
			return sb.toString();
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		finally {
			tlObjectCollector.dispose(sb);
		}
	}

	@Override
	public void writeToStream(OutputStream os, Object obj) {
		try {
			OutputStreamWriter osw = new OutputStreamWriter(os, Properties.CHARSET_UTF_8);
			DefaultXmlWriter writer = new DefaultXmlWriter(new WriterAppendable(osw), xmlController);

			writePrefix(writer);
			writer.writeObject(obj);
			postProcess(writer);
			writePostfix(writer);
			osw.flush();
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public void writeToChannel(final WritableByteChannel byteChannel, Object obj) {
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();

		final ByteBuffer byteBuffer = tlObjectCollector.create(ByteBuffer.class);
		try {
			DefaultXmlWriter writer =
					new DefaultXmlWriter(new ByteBufferAppendable(byteChannel, byteBuffer), xmlController);

			writePrefix(writer);
			writer.writeObject(obj);
			postProcess(writer);
			writePostfix(writer);

			// Flush the remaining bytes in the buffer
			byteBuffer.flip();
			try {
				while (byteBuffer.hasRemaining()) {
					byteChannel.write(byteBuffer);
				}
			}
			catch (IOException e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		finally {
			tlObjectCollector.dispose(ByteBuffer.class, byteBuffer);
		}
	}

	protected void writePrefix(IWriter writer) {
		// Intended blank
	}

	protected void writePostfix(IWriter writer) {
		// Intended blank
	}

	protected void postProcess(DefaultXmlWriter writer) {
		// Intended blank
	}
}
