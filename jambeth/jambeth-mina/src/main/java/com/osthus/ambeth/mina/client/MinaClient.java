package com.osthus.ambeth.mina.client;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;
import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatusChecker;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.serial.jssc.JSSCSerialConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class MinaClient implements IInitializingBean, IMinaClient
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IdleStatusChecker idleStatusChecker;

	@de.osthus.ambeth.config.Property(name = MinaConfigurationConstants.PROPERTY_NAME_CONNECT_TIMEOUT_IN_SECONDS, defaultValue = "5")
	protected int connectTimeoutInSeconds;

	@de.osthus.ambeth.config.Property(name = MinaConfigurationConstants.PROPERTY_NAME_COMMAND_TIMEOUT_IN_SECONDS, defaultValue = "10")
	protected int commandTimeoutInSeconds;

	protected IoSession session;

	protected IoHandlerAdapter minaClientHandler;

	protected MinaCommunicationParameter communicationParameter;

	protected final Lock lockForReadAnswer = new ReentrantLock();

	protected final Condition readAnswerMonitor = lockForReadAnswer.newCondition();

	protected List<String> lastAnswers = new ArrayList<String>();

	@Override
	public void afterPropertiesSet() throws Throwable
	{

	}

	@Override
	public void connect(Integer nioPort, String serialPortName, MinaCommunicationParameter communicationParameter, IoHandlerAdapter clientHandler,
			IoFilter ioFilter)
	{
		// Check prerequisites
		if (StringUtils.isBlank(serialPortName) && nioPort == null)
		{
			throw new IllegalArgumentException("nioPort or serialPortName must be set!");
		}
		if (!StringUtils.isBlank(serialPortName) && communicationParameter == null)
		{
			throw new IllegalArgumentException("communicationParameter must be set if serialPortName is set!");
		}
		if (nioPort != null && communicationParameter != null)
		{
			throw new IllegalArgumentException("communicationParameter must not be be set if nioPort is set!");
		}

		// Connect to the server
		final IoConnector connector;
		final SocketAddress portAddress;
		this.communicationParameter = communicationParameter;
		minaClientHandler = clientHandler;
		if (!StringUtils.isBlank(serialPortName))
		{
			connector = new JSSCSerialConnector(idleStatusChecker);
			portAddress = communicationParameter.getSerialAddress(serialPortName);
		}
		else
		{
			connector = new NioSocketConnector();
			portAddress = new InetSocketAddress(nioPort);
		}
		connector.getFilterChain().addLast("codec", ioFilter);

		connector.setHandler(minaClientHandler);

		ConnectFuture future = connector.connect(portAddress);
		future.awaitUninterruptibly(connectTimeoutInSeconds, TimeUnit.SECONDS);
		if (!future.isConnected())
		{
			connector.dispose(true);
			throw new IllegalStateException("Unnable to connect to " + portAddress);
		}

		session = future.getSession();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.osthus.jambeth.mina.IMinaClient#close()
	 */
	@Override
	public void close()
	{
		try
		{
			if (session != null)
			{
				session.close(true).await(1000);
				session = null;
			}
			minaClientHandler = null;
			communicationParameter = null;
		}
		catch (InterruptedException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.osthus.jambeth.mina.IMinaClient#executeCommand(java.lang.String)
	 */
	@Override
	public String executeCommand(String command)
	{
		// Check prerequisites
		if (StringUtils.isBlank(command))
		{
			throw new IllegalArgumentException("Command is missing!");
		}

		lastAnswers.clear();

		// send the command
		final WriteFuture writeFuture = session.write(command);
		writeFuture.awaitUninterruptibly(commandTimeoutInSeconds, TimeUnit.SECONDS);
		if (!writeFuture.isWritten())
		{
			throw new IllegalStateException("Unnable to write to " + session.getRemoteAddress().toString(), writeFuture.getException());
		}

		lockForReadAnswer.lock();
		try
		{
			// wait until the answer is read or the timeout is reached
			if (lastAnswers.size() == 0)
			{
				try
				{
					readAnswerMonitor.await(commandTimeoutInSeconds, TimeUnit.SECONDS);
				}
				catch (InterruptedException e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}
			}
		}
		finally
		{
			lockForReadAnswer.unlock();
		}

		if (lastAnswers.size() > 1)
		{
			throw new RuntimeException("More than one answer received, only one expected.");
		}

		String lastAnswer = null;
		if (lastAnswers.size() == 1)
		{
			lastAnswer = lastAnswers.get(0);
		}
		if (lastAnswer == null || StringUtils.isBlank(lastAnswer))
		{
			String message = "Communication aborted - no answer detected after " + commandTimeoutInSeconds + " seconds.";
			throw new IllegalStateException(message);
		}

		return lastAnswer;
	}

	@Override
	public void answerReceived(String answer)
	{
		lockForReadAnswer.lock();
		try
		{
			lastAnswers.add(answer);
			readAnswerMonitor.signal();
		}
		finally
		{
			lockForReadAnswer.unlock();
		}
	}
}
