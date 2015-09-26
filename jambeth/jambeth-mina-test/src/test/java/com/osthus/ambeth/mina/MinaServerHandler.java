package com.osthus.ambeth.mina;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

/**
 * Handles the requests (messages) on the server. Uses a mock implementation to answer the requests.
 */
public class MinaServerHandler extends IoHandlerAdapter
{

	public static final String ANSWER = "My answer";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.mina.core.service.IoHandlerAdapter#messageReceived(org.apache.mina.core.session.IoSession, java.lang.Object)
	 */
	@Override
	public void messageReceived(IoSession session, Object message) throws Exception
	{
		if (message instanceof String)
		{
			// always return the same answer
			String answer = ANSWER;
			session.write(answer);
		}
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception
	{
		throw new IllegalStateException("MinaServerHandler did not work properly.", cause);
	}

}
