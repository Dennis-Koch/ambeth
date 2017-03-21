package com.koch.ambeth.shell;

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

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.shell.AmbethShell;
import com.koch.ambeth.shell.ErrorModeTest.ErrorModeTestModule;
import com.koch.ambeth.shell.core.ShellContext.ErrorMode;
import com.koch.ambeth.shell.core.resulttype.SingleResult;
import com.koch.ambeth.shell.ioc.AmbethShellModule;
import com.koch.ambeth.testutil.AbstractIocTest;
import com.koch.ambeth.testutil.TestModule;

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
