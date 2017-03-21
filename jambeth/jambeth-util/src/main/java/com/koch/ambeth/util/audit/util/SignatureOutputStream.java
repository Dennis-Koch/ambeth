package com.koch.ambeth.util.audit.util;

/*-
 * #%L
 * jambeth-util
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

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
