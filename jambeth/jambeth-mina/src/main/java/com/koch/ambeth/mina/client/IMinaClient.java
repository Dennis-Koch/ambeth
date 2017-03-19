package com.koch.ambeth.mina.client;

import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.service.IoHandlerAdapter;

public interface IMinaClient
{

	/**
	 * Create a new session
	 * 
	 * @param nioPort
	 *            Port to use; only mandatory if no communication parameter is set
	 * @param serialPortName
	 *            Port name (e.g. COM1); only mandatory if no nioPort is set
	 * @param communicationParameter
	 *            Communication parameter; only mandatory if no nioPort is set
	 * @param clientHandler
	 *            The client handler
	 * @param ioFilter
	 *            The MINA IoFilter
	 */
	public abstract void connect(Integer nioPort, String serialPortName, MinaCommunicationParameter communicationParameter, IoHandlerAdapter clientHandler,
			IoFilter ioFilter);

	/**
	 * Closes the session
	 */
	public abstract void close();

	/**
	 * Execute the given command and retrieve the answer.
	 * 
	 * @param command
	 *            Command to execute (if multidrop the node address followed by a whitespace has to be used as prefix); mandatory
	 * @return Answer of the given command
	 */
	public abstract String executeCommand(String command);

	/**
	 * This method is called if a complete answer is received
	 * 
	 * @param message
	 *            the answer
	 */
	public void answerReceived(String answer);

}