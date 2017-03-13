package com.koch.ambeth.util.audit.util;

import java.io.IOException;
import java.io.OutputStream;
import java.security.Signature;
import java.security.SignatureException;

/**
 * This class provides an outputstream which writes everything to a Signature as well as to an underlying stream.
 */
public class SignatureOutputStream extends OutputStream
{
	protected final OutputStream target;
	protected final Signature sig;

	/**
	 * creates a new SignatureOutputStream which writes to a target OutputStream and updates the Signature object.
	 */
	public SignatureOutputStream(OutputStream target, Signature sig)
	{
		this.target = target;
		this.sig = sig;
	}

	@Override
	public void write(int b) throws IOException
	{
		write(new byte[] { (byte) b });
	}

	@Override
	public void write(byte[] b) throws IOException
	{
		write(b, 0, b.length);
	}

	@Override
	public void write(byte[] b, int offset, int len) throws IOException
	{
		target.write(b, offset, len);
		try
		{
			sig.update(b, offset, len);
		}
		catch (SignatureException ex)
		{
			throw new IOException(ex);
		}
	}

	@Override
	public void flush() throws IOException
	{
		target.flush();
	}

	@Override
	public void close() throws IOException
	{
		target.close();
	}
}
