package de.osthus.maven.randomuser;

import org.apache.maven.plugins.annotations.Mojo;

/**
 * Goal which removes an oracle db user with a random name.
 * 
 * Direct invocation is needed since there is no post-test phase to link the "remove" goal to.
 * 
 */
@Mojo(name = "remove", requiresDirectInvocation = true, aggregator = true)
public class RemoveRandomUser extends AbstractRandomUserMojo
{
	@Override
	protected String[] getArgsArray()
	{
		String[] args = { "script.create=false", //
				"script.user.propertyfile=" + citempFile, //
				"property.file=" + propertyFile };

		return args;
	}

	@Override
	protected String getErrorMessage()
	{
		return "Error during Oracle DB temp user creation";
	}
}
