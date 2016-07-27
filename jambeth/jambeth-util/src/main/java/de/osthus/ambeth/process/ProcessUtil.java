package de.osthus.ambeth.process;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.util.IDisposable;

public class ProcessUtil
{
	/**
	 * Creates a daemon thread to reads actively from the given src and writes the content
	 * 
	 * @param src
	 * @param dest
	 */
	public static IDisposable redirectIO(final InputStream in, final OutputStream out)
	{
		final Thread ioPipe = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				byte[] temp = new byte[1024];
				int bytesRead;
				try
				{
					while ((bytesRead = in.read(temp)) != -1)
					{
						out.write(temp, 0, bytesRead);
					}
				}
				catch (Throwable e)
				{
					// intended blank
					return;
				}
			}
		});
		ioPipe.setName(Thread.currentThread().getName() + "-IOPipe");
		ioPipe.setDaemon(true);
		ioPipe.start();
		return new IDisposable()
		{
			@Override
			public void dispose()
			{
				ioPipe.interrupt();
			}
		};
	}

	/**
	 * Runs the specified command on the command line. Both standard out and standard error streams are returned via the {@link ProcessResult} object. This
	 * method blocks until the command has been finished.
	 * 
	 * @param command
	 *            the command line to run
	 * @return the result code, std out and std err streams
	 */
	public static ProcessResult runCli(String... command)
	{
		ProcessBuilder pb = new ProcessBuilder(command);
		try
		{
			return ProcessUtil.waitForTermination(pb.start());
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	/**
	 * Waits for the given process to terminate. Standard out and standard error streams are returned via the {@link ProcessResult} object. This method works
	 * synchronously, i.e. it waits for the command to finish before it returns. Errors during the processing of out/err streams are appended to the returned
	 * representations of those streams.
	 * 
	 * @param process
	 * @return
	 */
	public static ProcessResult waitForTermination(Process process)
	{
		IDisposable disposeOut = null, disposeErr = null;
		try
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ByteArrayOutputStream err = new ByteArrayOutputStream();

			disposeOut = redirectIO(process.getInputStream(), out);
			disposeErr = redirectIO(process.getErrorStream(), err);

			int result = process.waitFor();

			Charset charset = Charset.defaultCharset();
			String outString = out.toString(charset.name());
			String errString = err.toString(charset.name());
			return new ProcessResult(outString, errString, result);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			if (disposeOut != null)
			{
				disposeOut.dispose();
			}
			if (disposeErr != null)
			{
				disposeErr.dispose();
			}
		}
	}
}
