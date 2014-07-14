package de.osthus.maven.randomuser;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import de.osthus.ambeth.testutil.RandomUserScript;

/**
 * Goal which creates a oracle db user with a random name.
 * 
 * Direct invocation is needed since there is no post-test phase to link the "remove" goal to.
 * 
 */
@Mojo(name = "remove", requiresDirectInvocation = true)
public class RemoveRandomUser extends AbstractMojo
{
	private static final String CITEMP_PROPERTIES_NAME = "${project.build.directory}/citemp.properties";

	/**
	 * Location of the citemp properties file.
	 */
	@Parameter(property = "citemp.properties", defaultValue = CITEMP_PROPERTIES_NAME, required = true)
	private File citempProperties;

	/**
	 * Location of the test database properties file.
	 */
	@Parameter(property = "property.file", required = true)
	private File propertyFile;

	@Override
	public void execute() throws MojoExecutionException
	{
		String[] args = { "script.create=false", //
				"script.user.propertyfile=" + citempProperties, //
				"property.file=" + propertyFile };
		try
		{
			RandomUserScript.main(args);
		}
		catch (Throwable e)
		{
			throw new MojoExecutionException("Error during Oracle DB temp user removal", e);
		}
	}
}
