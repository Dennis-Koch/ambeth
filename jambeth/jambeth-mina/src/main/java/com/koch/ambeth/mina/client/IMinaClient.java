package com.koch.ambeth.mina.client;

/*-
 * #%L
 * jambeth-mina
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

import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.service.IoHandlerAdapter;

public interface IMinaClient {

	/**
	 * Create a new session
	 *
	 * @param nioPort Port to use; only mandatory if no communication parameter is set
	 * @param serialPortName Port name (e.g. COM1); only mandatory if no nioPort is set
	 * @param communicationParameter Communication parameter; only mandatory if no nioPort is set
	 * @param clientHandler The client handler
	 * @param ioFilter The MINA IoFilter
	 */
	public abstract void connect(Integer nioPort, String serialPortName,
			MinaCommunicationParameter communicationParameter, IoHandlerAdapter clientHandler,
			IoFilter ioFilter);

	/**
	 * Closes the session
	 */
	public abstract void close();

	/**
	 * Execute the given command and retrieve the answer.
	 *
	 * @param command Command to execute (if multidrop the node address followed by a whitespace has
	 *        to be used as prefix); mandatory
	 * @return Answer of the given command
	 */
	public abstract String executeCommand(String command);

	/**
	 * This method is called if a complete answer is received
	 *
	 * @param message the answer
	 */
	public void answerReceived(String answer);

}
