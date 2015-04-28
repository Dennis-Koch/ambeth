package de.osthus.ambeth.pg;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;

import org.postgresql.PGConnection;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;

public class PostgresBlobMock implements Blob
{
	private LargeObject obj;

	private long oid;

	public PostgresBlobMock(PGConnection connection) throws SQLException
	{
		LargeObjectManager largeObjectManager = connection.getLargeObjectAPI();
		oid = largeObjectManager.createLO();
		obj = largeObjectManager.open(oid, LargeObjectManager.READWRITE);
	}

	public PostgresBlobMock(PGConnection connection, long oid, int mode) throws SQLException
	{
		LargeObjectManager largeObjectManager = connection.getLargeObjectAPI();
		obj = largeObjectManager.open(oid, mode);
	}

	@Override
	public void free() throws SQLException
	{
		obj.close();
	}

	@Override
	public long length() throws SQLException
	{
		return obj.size64();
	}

	@Override
	public byte[] getBytes(long pos, int length) throws SQLException
	{
		obj.seek64(pos, LargeObject.SEEK_SET);
		return obj.read(length);
	}

	@Override
	public InputStream getBinaryStream() throws SQLException
	{
		return obj.getInputStream();
	}

	@Override
	public InputStream getBinaryStream(long pos, long length) throws SQLException
	{
		obj.seek64(pos, LargeObject.SEEK_SET);
		final InputStream is = obj.getInputStream(length);
		return new InputStream()
		{
			@Override
			public int read() throws IOException
			{
				return is.read();
			}

			@Override
			public int read(byte[] b, int off, int len) throws IOException
			{
				return is.read(b, off, len);
			}

			@Override
			public int read(byte[] b) throws IOException
			{
				return is.read(b);
			}

			@Override
			public int available() throws IOException
			{
				return is.available();
			}

			@Override
			public long skip(long n) throws IOException
			{
				return is.skip(n);
			}

			@Override
			public boolean markSupported()
			{
				return is.markSupported();
			}

			@Override
			public void mark(int readlimit)
			{
				is.mark(readlimit);
			}

			@Override
			public void reset() throws IOException
			{
				is.reset();
			}
		};
	}

	@Override
	public long position(byte[] pattern, long start) throws SQLException
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public long position(Blob pattern, long start) throws SQLException
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public int setBytes(long pos, byte[] bytes) throws SQLException
	{
		obj.seek64(pos, LargeObject.SEEK_SET);
		obj.write(bytes);
		return bytes.length;
	}

	@Override
	public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException
	{
		obj.seek64(pos, LargeObject.SEEK_SET);
		obj.write(bytes, offset, len);
		return len;
	}

	@Override
	public OutputStream setBinaryStream(long pos) throws SQLException
	{
		obj.seek64(pos, LargeObject.SEEK_SET);
		final OutputStream os = obj.getOutputStream();
		return new OutputStream()
		{
			@Override
			public void write(int b) throws IOException
			{
				os.write(b);
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException
			{
				os.write(b, off, len);
			}

			@Override
			public void write(byte[] b) throws IOException
			{
				os.write(b);
			}

			@Override
			public void flush() throws IOException
			{
				os.flush();
			}
		};
	}

	@Override
	public void truncate(long len) throws SQLException
	{
		obj.truncate64(len);
	}
}
