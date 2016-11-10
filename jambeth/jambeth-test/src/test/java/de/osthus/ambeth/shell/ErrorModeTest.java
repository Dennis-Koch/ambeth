package de.osthus.ambeth.shell;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.shell.ErrorModeTest.ErrorModeTestModule;
import de.osthus.ambeth.shell.core.ShellContext.ErrorMode;
import de.osthus.ambeth.shell.core.resulttype.SingleResult;
import de.osthus.ambeth.shell.ioc.AmbethShellModule;
import de.osthus.ambeth.testutil.AbstractIocTest;
import de.osthus.ambeth.testutil.TestModule;

@TestModule(ErrorModeTestModule.class)
public class ErrorModeTest extends AbstractIocTest
{
	@Autowired
	protected AmbethShell shell;

	public static class ErrorModeTestModule implements IInitializingModule
	{
		@Override
		public void afterPropertiesSet(IBeanContextFactory bcf) throws Throwable
		{
			bcf.registerBean(AmbethShellModule.class);
			bcf.registerBean(ErrorModeTest.class).autowireable(ErrorModeTest.class);
			bcf.registerBean(ExampleCustomCommand.class);
		}
	}

	@Test(expected = java.lang.Exception.class)
	public void testErrorMode_THROW_EXCEPTION()
	{
		shell.executeCommand("set", "error.mode=" + ErrorMode.THROW_EXCPETION);
		shell.executeCommand("echo", "error mode is '$error.mode'");
		shell.executeCommand("throw", "message=ohhhh look, an exception!");
	}

	@Test
	public void testErrorMode_THROW_EXCEPTION_AND_CONTINUE()
	{
		try
		{
			shell.executeCommand("set", "error.mode=" + ErrorMode.THROW_EXCPETION);
			shell.executeCommand("throw", "message=ohhhh look, another exception!");
		}
		catch (Exception e)
		{

		}

		String value = "foo";
		SingleResult result = (SingleResult) shell.executeCommand("echo", value);
		result.getValue();
		Assert.assertEquals(result.getValue(), value);
	}

	@Test
	public void testErrorMode_LOG_ONLY()
	{
		shell.executeCommand("set", "error.mode=" + ErrorMode.LOG_ONLY);
		shell.executeCommand("echo", "error mode is '$error.mode'");
		shell.executeCommand("throw", "message=ohhhh look, an exception!");
	}
}
