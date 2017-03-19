package com.koch.ambeth.mina;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import com.koch.ambeth.mina.client.IMinaClient;

/**
 * Handles the answers (messages) on the client.
 */
public class MinaClientHandler extends IoHandlerAdapter
{

	private IMinaClient minaClient;

	/**
	 * Default constructor.
	 * 
	 * @param minaClient
	 *            The mina client that is called when the message is completely received.
	 */
	public MinaClientHandler(IMinaClient minaClient)
	{
		this.minaClient = minaClient;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.mina.core.service.IoHandlerAdapter#messageReceived(org.apache.mina.core.session.IoSession, java.lang.Object)
	 */
	@Override
	public void messageReceived(IoSession session, Object message) throws Exception
	{
		String answer = message.toString();
		minaClient.answerReceived(answer);
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception
	{
		throw new IllegalStateException("Exception when waiting for an answer to a message.", cause);
	}
}
