/*******************************************************************
 *                             Notice
 *
 * Copyright Osthus GmbH, All rights reserved.
 *
 * This software is part of the Bayer YSI Bioanalyzer Application
 * realized by Osthus GmbH.
 *
 * Address: Osthus GmbH
 *        : Eisenbahnweg 9 - 11 
 *        : 52068 Aachen
 *        : Germany
 *
 *******************************************************************/
package com.osthus.ambeth.mina;

import static org.junit.Assert.assertEquals;

import java.nio.charset.Charset;

import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.osthus.ambeth.ioc.MinaModule;
import com.osthus.ambeth.mina.client.IMinaClient;
import com.osthus.ambeth.mina.server.IMinaServerNio;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.testutil.AbstractIocTest;
import de.osthus.ambeth.testutil.TestModule;

/**
 * Very simple tests of MinaClient and MinaServer. This tests also demonstrates the use of this two classes
 */
@TestModule({ MinaModule.class })
public class MinaTest extends AbstractIocTest
{

	@Property(name = MinaTestConfigurationConstants.PROPERTY_NAME_TEST_NIO_PORT, defaultValue = "9123")
	protected int nioPort;

	@Autowired
	protected IMinaServerNio minaServer;

	@Autowired
	protected IMinaClient minaClient;

	@Before
	public void beforeTest()
	{
		Charset charset = Charset.defaultCharset(); // the charset must be the same in client and server

		minaServer.run(nioPort, new MinaServerHandler(), new ProtocolCodecFilter(new TextLineCodecFactory(charset, "\r\n", "\r")));

		minaClient.connect(nioPort, null, null, new ProtocolCodecFilter(new TextLineCodecFactory(charset, "\r", "\r\n")));
	}

	@After
	public void afterTest()
	{
		minaClient.close();
		minaServer.stop();
	}

	@Test
	public void testClient_StatusOfInstrument()
	{
		String answer = minaClient.executeCommand("test");
		assertEquals(MinaServerHandler.ANSWER, answer);
	}

}
