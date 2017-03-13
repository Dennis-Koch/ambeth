package com.koch.ambeth.threading;

import java.awt.EventQueue;
import java.awt.Toolkit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.IocConfigurationConstants;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.testutil.AbstractIocTest;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestRebuildContext;
import com.koch.ambeth.util.threading.GuiThreadHelper;
import com.koch.ambeth.util.threading.IGuiThreadHelper;

@TestRebuildContext
public class GuiThreadHelperTest extends AbstractIocTest
{
	@LogInstance
	private ILogger log;

	@Autowired
	protected IGuiThreadHelper guiThreadHelper;

	@After
	public void after() throws Throwable
	{
		long maxWait = System.currentTimeMillis() + 30000;
		while (GuiThreadHelper.hasUiThread())
		{
			if (maxWait <= System.currentTimeMillis())
			{
				throw new IllegalStateException("Timeout while waiting for the shutdown sequence of the UI thread");
			}
			Thread.sleep(250);
		}
	}

	@Test
	public void toolkitAllowed()
	{
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Assert.assertNotNull(toolkit);
		Assert.assertFalse(((GuiThreadHelper) guiThreadHelper).isGuiInitialized());
	}

	@Test
	public void eventQueueAllowed()
	{
		EventQueue eventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();
		Assert.assertNotNull(eventQueue);
		Assert.assertFalse(((GuiThreadHelper) guiThreadHelper).isGuiInitialized());
	}

	@Test
	public void dispatchThreadRecognized() throws Throwable
	{
		EventQueue.invokeAndWait(new Runnable()
		{
			@Override
			public void run()
			{
				Assert.assertTrue(((GuiThreadHelper) guiThreadHelper).isGuiInitialized());
			}
		});
		Assert.assertTrue(((GuiThreadHelper) guiThreadHelper).isGuiInitialized());
	}

	@Test
	@TestProperties(name = IocConfigurationConstants.JavaUiActive, value = "false")
	public void hasNoUiBecauseOfProperty() throws Throwable
	{
		EventQueue.invokeAndWait(new Runnable()
		{
			@Override
			public void run()
			{
				Assert.assertFalse(((GuiThreadHelper) guiThreadHelper).isGuiInitialized());
			}
		});
		Assert.assertFalse(((GuiThreadHelper) guiThreadHelper).isGuiInitialized());
	}
}
