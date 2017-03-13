package com.koch.ambeth.xml.simple;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;

import org.xmlpull.mxp1.MXParser;
import org.xmlpull.v1.XmlPullParser;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;
import com.koch.ambeth.util.typeinfo.ITypeInfoProvider;
import com.koch.ambeth.xml.DefaultXmlReader;
import com.koch.ambeth.xml.ICyclicXmlController;
import com.koch.ambeth.xml.ICyclicXmlReader;
import com.koch.ambeth.xml.IReader;
import com.koch.ambeth.xml.XmlTypeNotFoundException;
import com.koch.ambeth.xml.pending.ICommandBuilder;
import com.koch.ambeth.xml.pending.ICommandTypeRegistry;
import com.koch.ambeth.xml.pending.IObjectCommand;
import com.koch.ambeth.xml.pending.IObjectFuture;
import com.koch.ambeth.xml.pending.IObjectFutureHandlerRegistry;
import com.koch.ambeth.xml.postprocess.IPostProcessReader;

public class SimpleXmlReader implements ICyclicXmlReader {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected ICommandBuilder commandBuilder;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected IObjectFutureHandlerRegistry objectFutureHandlerRegistry;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected ITypeInfoProvider typeInfoProvider;

	@Autowired
	protected ICyclicXmlController xmlController;

	@Override
	public Object read(String cyclicXmlContent) {
		Reader reader = new StringReader(cyclicXmlContent);
		return readFromReader(reader);
	}

	@Override
	public Object readFromStream(InputStream is) {
		return readFromStream(is, "UTF8");
	}

	@Override
	public Object readFromStream(InputStream is, final String encoding) {
		if (is.markSupported()) {
			return readFromStreamLogAfterException(is, encoding);
		}
		return readFromStreamLogBeforeException(is, encoding);
	}

	@Override
	public Object readFromChannel(final ReadableByteChannel byteChannel) {
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		final ByteBuffer byteBuffer = tlObjectCollector.create(ByteBuffer.class);
		try {
			int read = byteChannel.read(byteBuffer);
			if (read == -1) {
				return null;
			}
			byteBuffer.flip();
			// Read something into the buffer this is to ensure that the buffer in all cases is in "has
			// remaining" state
			Reader reader = new Reader() {

				@Override
				public int read(char[] cbuf, int off, int len) throws IOException {
					int currLen = len;
					while (currLen > 0) {
						while (byteBuffer.hasRemaining() && currLen > 0) {
							cbuf[off++] = (char) byteBuffer.get();
							currLen--;
						}
						if (byteBuffer.hasRemaining()) {
							// do not read more than requested
							break;
						}
						// immediately refill the buffer to maintain the "has remaining" state
						byteBuffer.clear();
						int read = byteChannel.read(byteBuffer);
						byteBuffer.flip();
						if (read == 0) {
							// We could loop till we can read ALL requested bytes but we will not
							// we simply return with that much bytes that could be retrieved without blocking
							break;
						}
						if (read == -1) {
							if (currLen == len) {
								// not a single byte could be read and we have reached the end of the channel
								return -1;
							}
							break;
						}
					}
					return len - currLen;
				}

				@Override
				public void close() throws IOException {
					byteChannel.close();
				}
			};
			return readFromReader(reader);
		}
		catch (IOException e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		finally {
			tlObjectCollector.dispose(ByteBuffer.class, byteBuffer);
		}
	}

	protected Object readFromStreamLogAfterException(final InputStream is, final String encoding) {
		Reader streamReader = null;
		try {
			is.mark(is.available());
			streamReader = new InputStreamReader(is, encoding);
			return readFromReader(streamReader);
		}
		catch (XmlTypeNotFoundException e) {
			throw e;
		}
		catch (Throwable e) {
			String xmlContent;
			try {
				is.reset();
				byte[] buffer = new byte[8192];
				int length;
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				while ((length = is.read(buffer)) != -1) {
					bos.write(buffer, 0, length);
				}
				xmlContent = new String(bos.toByteArray(), Charset.forName(encoding));
			}
			catch (IOException ex) {
				throw RuntimeExceptionUtil.mask(ex);
			}
			throw RuntimeExceptionUtil.mask(e, xmlContent);
		}
	}

	protected Object readFromStreamLogBeforeException(final InputStream is, final String encoding) {
		final StringBuilder sb = new StringBuilder();
		Reader streamReader = null;
		try {
			streamReader = new InputStreamReader(new InputStream() {
				@Override
				public int available() throws IOException {
					return is.available();
				}

				@Override
				public int read() throws IOException {
					int oneByte = is.read();
					sb.append((char) oneByte);
					return oneByte;
				}

				@Override
				public void close() throws IOException {
					is.close();
				}

				@Override
				public synchronized void mark(int readlimit) {
					is.mark(readlimit);
				}

				@Override
				public boolean markSupported() {
					return is.markSupported();
				}

				@Override
				public int read(byte[] b) throws IOException {
					int count = is.read(b);
					for (int a = 0; a < count; a++) {
						sb.append((char) b[a]);
					}
					return count;
				}

				@Override
				public int read(byte[] b, int off, int len) throws IOException {
					int count = is.read(b, off, len);
					for (int a = off; a < count; a++) {
						sb.append((char) b[a]);
					}
					return count;
				}

				@Override
				public long skip(long n) throws IOException {
					return is.skip(n);
				}

				@Override
				public synchronized void reset() throws IOException {
					is.reset();
				}
			}, encoding);
			return readFromReader(streamReader);
		}
		catch (XmlTypeNotFoundException e) {
			throw e;
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e, sb.toString());
		}
	}

	@Override
	public Object readFromReader(Reader reader) {
		XmlPullParser xmlReader = null;
		try {
			xmlReader = new MXParser();
			xmlReader.setInput(reader);

			DefaultXmlReader pullParserReader =
					new DefaultXmlReader(xmlReader, xmlController, objectFutureHandlerRegistry);
			if (!pullParserReader.nextTag()) {
				return null;
			}
			readPrefix(pullParserReader);
			Object obj = pullParserReader.readObject();
			obj = postProcess(obj, pullParserReader);
			readPostfix(pullParserReader);
			return obj;
		}
		catch (XmlTypeNotFoundException e) {
			throw e;
		}
		catch (Throwable e) {
			if (xmlReader != null) {
				throw RuntimeExceptionUtil.mask(e, "Error while parsing cyclic xml content (line "
						+ xmlReader.getLineNumber() + ", column " + xmlReader.getColumnNumber() + ")");
			}
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected void readPrefix(IReader reader) {
		// Intended blank
	}

	protected void readPostfix(IReader reader) {
		//
	}

	protected Object postProcess(Object obj, IPostProcessReader postProcessReader) {
		if (obj instanceof IObjectFuture) {
			IObjectFuture objectFuture = (IObjectFuture) obj;
			ICommandTypeRegistry commandTypeRegistry = postProcessReader.getCommandTypeRegistry();
			IObjectCommand objectCommand = commandBuilder.build(commandTypeRegistry, objectFuture, null);
			postProcessReader.addObjectCommand(objectCommand);
			postProcessReader.executeObjectCommands();
			obj = objectFuture.getValue();
		}
		else {
			postProcessReader.executeObjectCommands();
		}
		return obj;
	}
}
