package com.osthus.ambeth.mina.server;

import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.service.IoHandlerAdapter;

public interface IMinaServerNio
{

	/**
	 * Starts the server with NIO
	 * 
	 * @param nioPort
	 *            The port to use with NIO
	 * @param ioFilter
	 *            The MINA IoFilter
	 */
	public abstract void run(int nioPort, IoHandlerAdapter serverHandler, IoFilter ioFilter);

	/**
	 * Stops the server
	 */
	public abstract void stop();

}