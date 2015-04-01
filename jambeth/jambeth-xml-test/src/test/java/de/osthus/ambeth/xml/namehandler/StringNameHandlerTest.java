package de.osthus.ambeth.xml.namehandler;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.ioc.BootstrapScannerModule;
import de.osthus.ambeth.ioc.XmlModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.testutil.AbstractInformationBusTest;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.xml.ICyclicXMLHandler;

@TestModule({ BootstrapScannerModule.class, XmlModule.class })
public class StringNameHandlerTest extends AbstractInformationBusTest
{
	private static final Charset UTF_8 = Charset.forName("UTF-8");

	@Autowired(XmlModule.CYCLIC_XML_HANDLER)
	protected ICyclicXMLHandler cyclicXmlHandler;

	private String xml;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		Path path = Paths.get("src/test/resources/veryLongXml.xml");
		List<String> allLines = Files.readAllLines(path, UTF_8);
		xml = allLines.get(0);
	}

	@Test
	public void testWritesCustom() throws IOException
	{
		String written = cyclicXmlHandler.write(xml);
		Assert.assertNotNull(written);
		System.out.println(written);
	}

	@Test
	public void testReadObject()
	{
		String written = cyclicXmlHandler.write(xml);

		Object read = cyclicXmlHandler.read(written);
		Assert.assertNotNull(read);
		Assert.assertTrue(read instanceof String);
		Assert.assertEquals(xml, read);
	}
}
