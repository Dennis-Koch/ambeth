package de.osthus.ambeth.threading;

import java.awt.Toolkit;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.config.IocConfigurationConstants;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.testutil.AbstractIocTest;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestRebuildContext;

@TestRebuildContext
public class GuiThreadHelperTest extends AbstractIocTest
{
	@LogInstance
	private ILogger log;

	@Autowired
	protected IGuiThreadHelper guiThreadHelper;

	@Test
	public void hasUi()
	{
		Toolkit.getDefaultToolkit();
		Assert.assertTrue(((GuiThreadHelper) guiThreadHelper).isGuiInitialized());
	}

	@Test
	public void hasNoUi()
	{
		Assert.assertFalse(((GuiThreadHelper) guiThreadHelper).isGuiInitialized());
	}

	@Test
	@TestProperties(name = IocConfigurationConstants.JavaUiActive, value = "false")
	public void hasNoUiBecauseOfProperty()
	{
		Toolkit.getDefaultToolkit();
		Assert.assertFalse(((GuiThreadHelper) guiThreadHelper).isGuiInitialized());
	}
}
