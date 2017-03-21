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

import static org.junit.Assert.assertEquals;

import java.nio.charset.Charset;

import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.koch.ambeth.ioc.MinaModule;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.mina.client.IMinaClient;
import com.koch.ambeth.mina.server.IMinaServerNio;
import com.koch.ambeth.testutil.AbstractIocTest;
import com.koch.ambeth.testutil.TestModule;

/**
 * Very simple tests of MinaClient and MinaServer. This tests also demonstrates the use of this two
 * classes
 */
@TestModule({MinaModule.class})
public class MinaTest extends AbstractIocTest {

	@Property(name = MinaTestConfigurationConstants.PROPERTY_NAME_TEST_NIO_PORT,
			defaultValue = "9123")
	protected int nioPort;

	@Autowired
	protected IMinaServerNio minaServer;

	@Autowired
	protected IMinaClient minaClient;

	@Before
	public void beforeTest() {
		Charset charset = Charset.defaultCharset(); // the charset must be the same in client and server

		minaServer.run(nioPort, new MinaServerHandler(),
				new ProtocolCodecFilter(new TextLineCodecFactory(charset, "\r\n", "\r")));

		minaClient.connect(nioPort, null, null, new MinaClientHandler(minaClient),
				new ProtocolCodecFilter(new TextLineCodecFactory(charset, "\r", "\r\n")));
	}

	@After
	public void afterTest() {
		minaClient.close();
		minaServer.stop();
	}

	@Test
	public void testCommunication() {
		String answer = minaClient.executeCommand("test");
		assertEquals(MinaServerHandler.ANSWER, answer);
	}

}
