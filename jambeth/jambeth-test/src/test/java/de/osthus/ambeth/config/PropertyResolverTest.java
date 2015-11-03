package de.osthus.ambeth.config;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.testutil.AbstractIocTest;

public class PropertyResolverTest extends AbstractIocTest
{
	/**
	 * knownVar=123 partOfVar=Var hugoVar=known${partOfVar} cycle=${cycle}
	 * 
	 * abc${knownVar} => abc123 abc${partOfVar} => abcVar abc${hugoVar} => abcknownVar abc${${hugoVar}=> abc${knownVar} => abc123
	 * 
	 * abc${unknownVar} = > abc${unknownVar}
	 * 
	 * abc${cycle} => EXCEPTION cycle…
	 */
	@LogInstance
	private ILogger log;
	private Properties props;

	@Before
	public void setupProperties()
	{
		props = new Properties();
		props.putString("knownVar", "123");
		props.putString("partOfVar", "Var");
		props.putString("hugoVar", "known${partOfVar}");
		props.putString("cycle", "${cycle}");
		props.putString("nextUnknown", "${unknownVar}");

	}

	@Test
	public void testCorrectResolve()
	{
		Assert.assertEquals("abc123abc123", props.resolvePropertyParts("abc${knownVar}abc${knownVar}"));
		Assert.assertEquals("abcVar", props.resolvePropertyParts("abc${partOfVar}"));
		Assert.assertEquals("abcknownVar", props.resolvePropertyParts("abc${hugoVar}"));
		Assert.assertEquals("abc123", props.resolvePropertyParts("abc${${hugoVar}}"));

		// test no resolve
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCycleException()
	{
		props.resolvePropertyParts("${cycle}");

	}

	@Test
	public void testUnknownProperty()
	{
		// Assert.assertEquals("abc${unknownVar}", props.resolvePropertyParts("abc${unknownVar}"));
		// Assert.assertEquals("test${unknownVar}testabc123test${unknownVar}qwer",
		// props.resolvePropertyParts("test${unknownVar}testabc${${hugoVar}}test${unknownVar}qwer"));

		Assert.assertEquals("test${unknownVar}testabc123test${unknownVar}qwerA${unknownVar}qwerB",
				props.resolvePropertyParts("test${unknownVar}testabc${${hugoVar}}test${unknownVar}qwerA${unknownVar}qwerB"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUnknownPropertyWithCycleException()
	{
		Assert.assertEquals("test${unknownVar}testabc123test${unknownVar}qwer${unknownVar}qwer",
				props.resolvePropertyParts("test${unknownVar}testabc${cycle}${${hugoVar}}test${unknownVar}qwer${unknownVar}qwer"));
	}

	@Test
	public void testUnknownPropertySecondResolve()
	{
		// here nextUnknown is "known" but the next step "unknown" variable is not known, that means that nextUnknown can't be resolved completely and
		// nextUnknown goes back into the string
		Assert.assertEquals("abc${nextUnknown}", props.resolvePropertyParts("abc${nextUnknown}"));
		Assert.assertEquals("abc${nextUnknown}asdfasdfsadf", props.resolvePropertyParts("abc${nextUnknown}asdfasdfsadf"));
		Assert.assertEquals("abc${nextUnknown}asdfasdfVar", props.resolvePropertyParts("abc${nextUnknown}asdfasdf${partOfVar}"));
	}

}
