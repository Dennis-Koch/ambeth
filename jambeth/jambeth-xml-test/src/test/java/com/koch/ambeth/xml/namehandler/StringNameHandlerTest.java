package com.koch.ambeth.xml.namehandler;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.testutil.AbstractInformationBusTest;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.xml.ICyclicXMLHandler;
import com.koch.ambeth.xml.ioc.BootstrapScannerModule;
import com.koch.ambeth.xml.ioc.XmlModule;

@TestModule({ BootstrapScannerModule.class, XmlModule.class })
public class StringNameHandlerTest extends AbstractInformationBusTest
{
	private static final Charset UTF_8 = Charset.forName("UTF-8");

	private static final String PREFIX = "<root><s i=\"1\"><![CDATA[";

	private static final String POSTFIX = "]]></s></root>";

	@Autowired(XmlModule.CYCLIC_XML_HANDLER)
	protected ICyclicXMLHandler cyclicXmlHandler;

	@Test
	public void testWritesCustom() throws IOException
	{
		String input = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, ...";
		String expected = PREFIX + input + POSTFIX;
		String written = cyclicXmlHandler.write(input);
		Assert.assertNotNull(written);
		Assert.assertEquals(expected, written);

		input = "Lorem ipsum dolor sit [[amet,]] consetetur [[sadipscing]] elitr, ...";
		expected = PREFIX + input + POSTFIX;
		written = cyclicXmlHandler.write(input);
		Assert.assertNotNull(written);
		Assert.assertEquals(expected, written);
	}

	@Test
	public void testReadObject() throws IOException
	{
		String input = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, ...";
		String written = cyclicXmlHandler.write(input);
		Object read = cyclicXmlHandler.read(written);
		Assert.assertNotNull(read);
		Assert.assertTrue(read instanceof String);
		Assert.assertEquals(input, read);

		input = "Lorem ipsum dolor sit [[amet,]] consetetur [[sadipscing]] elitr, ...";
		written = cyclicXmlHandler.write(input);
		read = cyclicXmlHandler.read(written);
		Assert.assertNotNull(read);
		Assert.assertTrue(read instanceof String);
		Assert.assertEquals(input, read);
	}

	@Test
	public void testWritesCustom_cdataEnd() throws IOException
	{
		String input = "Lorem ipsum dolor sit <![CDATA[amet,]]> consetetur <![CDATA[sadipscing]]> elitr, ...";
		String expected = "<root><s i=\"1\"><s><![CDATA[Lorem ipsum dolor sit <![CDATA[amet,]]]]></s><s><![CDATA[> consetetur <![CDATA[sadipscing]]]]></s><s><![CDATA[> elitr, ...]]></s></s></root>";
		String written = cyclicXmlHandler.write(input);
		Assert.assertNotNull(written);
		Assert.assertEquals(expected, written);
	}

	@Test
	public void testReadObject_cdataEnd() throws IOException
	{
		String input = "Lorem ipsum dolor sit <![CDATA[amet,]]> consetetur <![CDATA[sadipscing]]> elitr, ...";
		String written = cyclicXmlHandler.write(input);
		Object read = cyclicXmlHandler.read(written);
		Assert.assertNotNull(read);
		Assert.assertTrue(read instanceof String);
		Assert.assertEquals(input, read);
	}

	@Test
	public void testWritesCustom_longXml() throws IOException
	{
		String longXml = loadLongXml();

		String written = cyclicXmlHandler.write(longXml);
		Assert.assertNotNull(written);
	}

	@Test
	public void testReadObject_longXml() throws IOException
	{
		String longXml = loadLongXml();

		String written = cyclicXmlHandler.write(longXml);

		Object read = cyclicXmlHandler.read(written);
		Assert.assertNotNull(read);
		Assert.assertTrue(read instanceof String);
		Assert.assertEquals(longXml, read);
	}

	protected String loadLongXml() throws IOException
	{
		Path path = Paths.get("src/test/resources/veryLongXml.xml");
		List<String> allLines = Files.readAllLines(path, UTF_8);
		String longXml = allLines.get(0);
		return longXml;
	}
}
