package com.koch.ambeth.util.process;

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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import com.koch.ambeth.util.IDisposable;
import com.koch.ambeth.util.ParamHolder;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.io.SplitOutputStream;

public class ProcessUtil {
	/**
	 * Creates a daemon thread to reads actively from the given src and writes the content
	 *
	 * @param src
	 * @param dest
	 */
	public static IDisposable redirectIO(final InputStream in, final OutputStream out) {
		final ParamHolder<Boolean> disposedPH = new ParamHolder<>();
		final Thread ioPipe = new Thread(new Runnable() {
			@Override
			public void run() {
				byte[] temp = new byte[1024];
				int bytesRead;
				try {
					try {
						try {
							while ((bytesRead = in.read(temp)) != -1) {
								if (Boolean.TRUE.equals(disposedPH.getValue())) {
									return;
								}
								out.write(temp, 0, bytesRead);
							}
						}
						finally {
							in.close();
						}
					}
					finally {
						out.close();
					}
				}
				catch (Throwable e) {
					// intended blank
					return;
				}
			}
		});
		ioPipe.setName(Thread.currentThread().getName() + "-IOPipe");
		ioPipe.setDaemon(true);
		ioPipe.start();
		return new IDisposable() {
			@Override
			public void dispose() {
				disposedPH.setValue(Boolean.TRUE);
				ioPipe.interrupt();
			}
		};
	}

	/**
	 * Runs the specified command on the command line. Both standard out and standard error streams
	 * are returned via the {@link ProcessResult} object. This method blocks until the command has
	 * been finished.
	 *
	 * @param command
	 *          the command line to run
	 * @return the result code, std out and std err streams
	 */
	public static ProcessResult runCli(String... command) {
		ProcessBuilder pb = new ProcessBuilder(command);
		try {
			return ProcessUtil.waitForTermination(pb.start());
		}
		catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	/**
	 * Waits for the given process to terminate. Standard out and standard error streams are returned
	 * via the {@link ProcessResult} object. This method works synchronously, i.e. it waits for the
	 * command to finish before it returns. Errors during the processing of out/err streams are
	 * appended to the returned representations of those streams.
	 *
	 * @param process
	 * @return
	 */
	public static ProcessResult waitForTermination(Process process) {
		IDisposable disposeOut = null, disposeErr = null;
		try {
			return waitForTermination(process, null, null);
		}
		catch (Throwable e) {
			process.destroy();
			throw RuntimeExceptionUtil.mask(e);
		}
		finally {
			if (disposeOut != null) {
				disposeOut.dispose();
			}
			if (disposeErr != null) {
				disposeErr.dispose();
			}
		}
	}

	/**
	 * Waits for the given process to terminate. Standard out and standard error streams are returned
	 * via the {@link ProcessResult} object. This method works synchronously, i.e. it waits for the
	 * command to finish before it returns. Errors during the processing of out/err streams are
	 * appended to the returned representations of those streams. In addition to { @link
	 * {@link #waitForTermination(Process)} this method allows to provide a custom
	 * {@link OutputStream} which receives all console events in real-time. In any case at the end of
	 * the process execution the ProcessResult contains the full console output as if no real-time
	 * stream had been provided.
	 *
	 * @param process
	 * @param outAndErr
	 *          Custom {@link OutputStream} to receive out and err events in real-time and
	 *          concurrently - so be aware of potential threading issues in your application.
	 * @return
	 */
	public static ProcessResult waitForTermination(Process process, OutputStream outAndErr) {
		return waitForTermination(process, outAndErr, outAndErr);
	}

	/**
	 * Waits for the given process to terminate. Standard out and standard error streams are returned
	 * via the {@link ProcessResult} object. This method works synchronously, i.e. it waits for the
	 * command to finish before it returns. Errors during the processing of out/err streams are
	 * appended to the returned representations of those streams. In addition to { @link
	 * {@link #waitForTermination(Process)} this method allows to provide a custom
	 * {@link OutputStream} which receives all console events in real-time. In any case at the end of
	 * the process execution the ProcessResult contains the full console output as if no real-time
	 * stream had been provided.
	 *
	 * @param process
	 * @param out
	 *          Custom {@link OutputStream} to receive out events in real-time and concurrently - so
	 *          be aware of potential threading issues in your application.
	 * @param err
	 *          Custom {@link OutputStream} to receive err events in real-time and concurrently - so
	 *          be aware of potential threading issues in your application.
	 * @return
	 */
	public static ProcessResult waitForTermination(Process process, OutputStream out,
			OutputStream err) {
		IDisposable disposeOut = null, disposeErr = null;
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			ByteArrayOutputStream berr = new ByteArrayOutputStream();

			OutputStream mout = (out == null ? bout : new SplitOutputStream(bout, out));
			OutputStream merr = (err == null ? berr : new SplitOutputStream(berr, err));
			try {
				disposeOut = redirectIO(process.getInputStream(), mout);
				disposeErr = redirectIO(process.getErrorStream(), merr);

				int result = process.waitFor();

				Charset charset = Charset.defaultCharset();
				String outString = bout.toString(charset.name());
				String errString = berr.toString(charset.name());
				return new ProcessResult(outString, errString, result);
			}
			finally {
				mout.close();
				merr.close();
			}
		}
		catch (Throwable e) {
			process.destroy();
			throw RuntimeExceptionUtil.mask(e);
		}
		finally {
			if (disposeOut != null) {
				disposeOut.dispose();
			}
			if (disposeErr != null) {
				disposeErr.dispose();
			}
		}
	}
}
