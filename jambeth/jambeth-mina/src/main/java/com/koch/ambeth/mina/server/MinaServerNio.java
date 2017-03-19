package com.koch.ambeth.mina.server;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

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
public class MinaServerNio implements IInitializingBean, IMinaServerNio {

	// ---- INNER CLASSES ------------------------------------------------------

	// ---- CONSTANTS ----------------------------------------------------------

	private static final int DEFAULT_PORT = 9123;

	// ---- VARIABLES ----------------------------------------------------------

	private IoAcceptor acceptor;

	// ---- CONSTRUCTORS -------------------------------------------------------

	// ---- GETTER/SETTER METHODS ----------------------------------------------

	// ---- METHODS ------------------------------------------------------------

	@Override
	public void afterPropertiesSet() throws Throwable {
	}

	@Override
	public void run(int nioPort, IoHandlerAdapter serverHandler, IoFilter ioFilter) {
		startNio(nioPort, serverHandler, ioFilter);
	}

	@Override
	public void stop() {
		acceptor.dispose();
	}

	/**
	 * Test this using "telnet 127.0.0.1 9123"
	 *
	 * @throws IOException
	 */
	private void startNio(int port, IoHandlerAdapter serverHandler, IoFilter ioFilter) {
		acceptor = new NioSocketAcceptor();
		InetSocketAddress portAddress = new InetSocketAddress(port);

		// Set up server
		try {
			// acceptor.getFilterChain().addLast("logger", new LoggingFilter());
			acceptor.getFilterChain().addLast("codec", ioFilter);

			acceptor.setHandler(serverHandler);

			acceptor.getSessionConfig().setReadBufferSize(2048);
			acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
			acceptor.bind(portAddress);

		}
		catch (Exception e) {
			if (acceptor != null && portAddress != null) {
				try {
					acceptor.unbind(portAddress);
				}
				finally {
					// Ignore errors on shutdown
				}
			}
			handleException(e);
		}
	}

	private static void handleException(Exception e) {
		throw RuntimeExceptionUtil.mask(e);
	}

}
