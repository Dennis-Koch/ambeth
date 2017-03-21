package com.koch.ambeth.mina.server;

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

public interface IMinaServerNio
{

	/**
	 * Starts the server with NIO
	 * 
	 * @param nioPort
	 *            The port to use with NIO
	 * @param serverHandler
	 *            The server handler
	 * @param ioFilter
	 *            The MINA IoFilter
	 */
	public abstract void run(int nioPort, IoHandlerAdapter serverHandler, IoFilter ioFilter);

	/**
	 * Stops the server
	 */
	public abstract void stop();

}
