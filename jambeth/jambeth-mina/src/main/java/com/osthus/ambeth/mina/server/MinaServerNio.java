/*******************************************************************
 *                             Notice
 *
 * Copyright Osthus GmbH, All rights reserved.
 *
 * This software is part of the Bayer YSI Bioanalyzer Application
 * realized by Osthus GmbH.
 *
 * Address: Osthus GmbH
 *        : Eisenbahnweg 9 - 11 
 *        : 52068 Aachen
 *        : Germany
 *
 *******************************************************************/
package com.osthus.ambeth.mina.server;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;

/**
 * “recommended application pattern”
 * <p>
 * <ul>
 * <li>use ExecutorFilter as first in chain unless you need really low latency</li>
 * <li>use ProtocolCodecFilter convert the wire protocol into a Java representation</li>
 * <li>put application logic into an IoHandler</li>
 * <li>store state in the IoSession</li>
 * </ul>
 */
public class MinaServerNio implements IInitializingBean, IMinaServerNio
{

	// ---- INNER CLASSES ------------------------------------------------------

	// ---- CONSTANTS ----------------------------------------------------------

	private static final int DEFAULT_PORT = 9123;

	// ---- VARIABLES ----------------------------------------------------------

	private IoAcceptor acceptor;

	// ---- CONSTRUCTORS -------------------------------------------------------

	// ---- GETTER/SETTER METHODS ----------------------------------------------

	// ---- METHODS ------------------------------------------------------------

	@Override
	public void afterPropertiesSet() throws Throwable
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.osthus.jambeth.mina.server.IMinaServer#run(int, org.apache.mina.core.service.IoHandlerAdapter)
	 */
	@Override
	public void run(int nioPort, IoHandlerAdapter serverHandler, IoFilter ioFilter)
	{
		startNio(nioPort, serverHandler, ioFilter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.osthus.jambeth.mina.server.IMinaServer#stop()
	 */
	@Override
	public void stop()
	{
		acceptor.dispose();
	}

	/**
	 * Test this using "telnet 127.0.0.1 9123"
	 * 
	 * @throws IOException
	 */
	private void startNio(int port, IoHandlerAdapter serverHandler, IoFilter ioFilter)
	{
		acceptor = new NioSocketAcceptor();
		InetSocketAddress portAddress = new InetSocketAddress(port);

		// Set up server
		try
		{
			// acceptor.getFilterChain().addLast("logger", new LoggingFilter());
			acceptor.getFilterChain().addLast("codec", ioFilter);

			acceptor.setHandler(serverHandler);

			acceptor.getSessionConfig().setReadBufferSize(2048);
			acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
			acceptor.bind(portAddress);

		}
		catch (Exception e)
		{
			if (acceptor != null && portAddress != null)
			{
				try
				{
					acceptor.unbind(portAddress);
				}
				finally
				{
					// Ignore errors on shutdown
				}
			}
			handleException(e);
		}
	}

	private static void handleException(Exception e)
	{
		throw RuntimeExceptionUtil.mask(e);
	}

}
