package com.koch.ambeth.util;

import java.io.PrintStream;

import com.koch.ambeth.util.audit.util.NullOutputStream;

public class NullPrintStream extends PrintStream
{
	public static final NullPrintStream INSTANCE = new NullPrintStream();

	public NullPrintStream()
	{
		super(NullOutputStream.INSTANCE);
	}
}
