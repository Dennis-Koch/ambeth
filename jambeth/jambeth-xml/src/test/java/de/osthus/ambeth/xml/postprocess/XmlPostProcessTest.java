package de.osthus.ambeth.xml.postprocess;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.osthus.ambeth.exception.MaskingRuntimeException;
import de.osthus.ambeth.ioc.BootstrapScannerModule;
import de.osthus.ambeth.ioc.XmlModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.testutil.AbstractIocTest;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.xml.ICyclicXMLHandler;
import de.osthus.ambeth.xml.XmlTestModule;

@TestModule({ BootstrapScannerModule.class, XmlModule.class, XmlTestModule.class, XmlPostProcessTestModule.class })
public class XmlPostProcessTest extends AbstractIocTest
{
	@Autowired(XmlModule.CYCLIC_XML_HANDLER)
	protected ICyclicXMLHandler cyclicXmlHandler;

	@Autowired
	protected TestXmlPostProcessor testXmlPostProcessor;

	@Test
	public void testPostProcessTag()
	{
		testXmlPostProcessor.handledTags.clear();
		String xml = "<root><n/><pp><test1/><test2/><test3/></pp></root>";
		cyclicXmlHandler.read(xml);
		assertEquals(3, testXmlPostProcessor.handledTags.size());
		assertEquals("test1", testXmlPostProcessor.handledTags.get(0));
		assertEquals("test2", testXmlPostProcessor.handledTags.get(1));
		assertEquals("test3", testXmlPostProcessor.handledTags.get(2));
	}

	@Test(expected = IllegalStateException.class)
	public void testPostProcessTag_exception() throws Throwable
	{
		String xml = "<root><n/><pp><test1/><test2/><test4/></pp></root>";
		try
		{
			cyclicXmlHandler.read(xml);
		}
		catch (MaskingRuntimeException e)
		{
			throw e.getCause();
		}
	}
}
