package com.osthus.ambeth.mina.client;

/**
 * Callback handler for YsiClientHandler
 */
public interface IMinaClientHandlerCallback
{
	/**
	 * This method is called if a complete answer is received
	 * 
	 * @param message
	 *            the answer
	 */
	public void answerReceived(String answer);
}
