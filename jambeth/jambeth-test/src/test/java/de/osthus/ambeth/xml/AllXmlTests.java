package de.osthus.ambeth.xml;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.osthus.ambeth.xml.postprocess.XmlPostProcessTest;

@RunWith(Suite.class)
@SuiteClasses({ CyclicXmlDictionaryTest.class, CyclicXmlReaderTest.class, CyclicXmlWriterTest.class, XmlPostProcessTest.class })
public class AllXmlTests
{
}
