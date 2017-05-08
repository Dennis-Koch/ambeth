package com.koch.ambeth.xml.postprocess;

/*-
 * #%L
 * jambeth-test
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

import org.junit.Test;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.testutil.AbstractInformationBusTest;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.util.exception.MaskingRuntimeException;
import com.koch.ambeth.xml.ICyclicXMLHandler;
import com.koch.ambeth.xml.ioc.BootstrapScannerModule;
import com.koch.ambeth.xml.ioc.XmlModule;

@TestModule({BootstrapScannerModule.class, XmlModule.class, XmlPostProcessTestModule.class})
public class XmlPostProcessTest extends AbstractInformationBusTest {
	@Autowired(XmlModule.CYCLIC_XML_HANDLER)
	protected ICyclicXMLHandler cyclicXmlHandler;

	@Autowired
	protected TestXmlPostProcessor testXmlPostProcessor;

	@Test
	public void testPostProcessTag() {
		testXmlPostProcessor.handledTags.clear();
		String xml = "<root><n/><pp><test1/><test2/><test3/></pp></root>";
		cyclicXmlHandler.read(xml);
		assertEquals(3, testXmlPostProcessor.handledTags.size());
		assertEquals("test1", testXmlPostProcessor.handledTags.get(0));
		assertEquals("test2", testXmlPostProcessor.handledTags.get(1));
		assertEquals("test3", testXmlPostProcessor.handledTags.get(2));
	}

	@Test(expected = IllegalStateException.class)
	public void testPostProcessTag_exception() throws Throwable {
		String xml = "<root><n/><pp><test1/><test2/><test4/></pp></root>";
		try {
			cyclicXmlHandler.read(xml);
		}
		catch (MaskingRuntimeException e) {
			throw e.getCause();
		}
	}
}
