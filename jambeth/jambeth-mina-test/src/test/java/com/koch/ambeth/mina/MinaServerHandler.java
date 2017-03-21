package com.koch.ambeth.mina;

/*-
 * #%L
 * jambeth-mina-test
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
