package de.osthus.ambeth.process;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;

public class ProcessUtil
{
	public static void redirectIO(final InputStream src, final PrintStream dest)
	{
		Thread ioPipe = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				byte[] temp = new byte[1024];
				int bytesRead;
				try
				{
					while ((bytesRead = src.read(temp)) != -1)
					{
						dest.append(new String(temp, 0, bytesRead));
					}
				}
				catch (IOException e)
				{
					return;
				}
			}
		});
		ioPipe.setName(Thread.currentThread().getName() + "-IOPipe");
		ioPipe.setDaemon(true);
		ioPipe.start();
	}

	public static ProcessResult waitForTermination(Process process)
	{
		try
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ByteArrayOutputStream err = new ByteArrayOutputStream();
			Charset charset = Charset.defaultCharset();
			PrintStream outStream = new PrintStream(out, false, charset.name());
			PrintStream errStream = new PrintStream(err, false, charset.name());

			redirectIO(process.getInputStream(), outStream);
			redirectIO(process.getErrorStream(), errStream);

			int result = process.waitFor();
			outStream.close();
			errStream.close();
			String outString = out.toString(charset.name());
			String errString = err.toString(charset.name());
			return new ProcessResult(outString, errString, result);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
