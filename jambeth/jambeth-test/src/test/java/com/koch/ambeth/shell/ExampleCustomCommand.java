package com.koch.ambeth.shell;

import com.koch.ambeth.shell.core.annotation.Command;
import com.koch.ambeth.shell.core.annotation.CommandArg;

public class ExampleCustomCommand
{

	@Command(name = "throw", description = "throws an exception")
	public void ThrowExceptionCommand(@CommandArg(name = "message", shortName = "m", defaultValue = "ERROR, ERROR, ERROR") String message) throws Exception
	{
		throw new Exception(message);
	}
}
