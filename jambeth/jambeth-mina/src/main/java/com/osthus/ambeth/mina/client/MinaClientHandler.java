package com.osthus.ambeth.mina.client;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

/**
 * Handles the answers (messages) on the client.
 */
public class MinaClientHandler extends IoHandlerAdapter
{

	private StringBuilder answerBuilder;

	private IMinaClientHandlerCallback clientHandlerCallback;

	/**
	 * Default constructor. The session is closed immediately after the first answer.
	 */
	public MinaClientHandler(IMinaClientHandlerCallback clientHandlerCallback)
	{
		answerBuilder = new StringBuilder();
		this.clientHandlerCallback = clientHandlerCallback;
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
		String trimmedAnswer = answer.trim();
		if (trimmedAnswer.length() > 0 && answerBuilder.length() > 0)
		{
			answerBuilder.append("\r\n"); // if there is already a line received then add CR/LF
		}
		answerBuilder.append(answer);

		if (!trimmedAnswer.endsWith("\\"))
		{
			// a backslash indicates that a further line will be transmitted
			clientHandlerCallback.answerReceived(answerBuilder.toString());
			answerBuilder.setLength(0);
		}
	}

}
