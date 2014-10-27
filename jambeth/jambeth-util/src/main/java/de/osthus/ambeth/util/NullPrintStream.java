package de.osthus.ambeth.util;

import java.io.PrintStream;

import de.osthus.ambeth.audit.util.NullOutputStream;

public class NullPrintStream extends PrintStream
{
	public static final NullPrintStream INSTANCE = new NullPrintStream();

	public NullPrintStream()
	{
		super(NullOutputStream.INSTANCE);
	}
}
