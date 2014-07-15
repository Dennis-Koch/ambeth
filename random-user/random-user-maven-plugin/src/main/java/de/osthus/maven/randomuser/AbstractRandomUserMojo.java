package de.osthus.maven.randomuser;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;

import de.osthus.ambeth.testutil.RandomUserScript;

public abstract class AbstractRandomUserMojo extends AbstractMojo
{
	private static final String CITEMP_PROPERTIES_NAME = "${basedir}/citemp.properties";

	/**
	 * Location of the citemp properties file.
	 */
	@Parameter(property = "citemp.file", defaultValue = CITEMP_PROPERTIES_NAME, required = true)
	protected File citempFile;

	/**
	 * Location of the test database properties file.
	 */
	@Parameter(property = "property.file", required = true)
	protected File propertyFile;

	@Override
	public void execute() throws MojoExecutionException
	{
		String[] args = getArgsArray();
		try
		{
			RandomUserScript.main(args);
		}
		catch (Throwable e)
		{
			throw new MojoExecutionException(getErrorMessage(), e);
		}
	}

	protected abstract String[] getArgsArray();

	protected abstract String getErrorMessage();
}