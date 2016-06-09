package de.osthus.ambeth.config;

import javax.xml.bind.annotation.XmlRootElement;

import de.osthus.ambeth.annotation.ConfigurationConstants;
import de.osthus.ambeth.start.IClasspathInfo;
import de.osthus.ambeth.util.IClasspathScanner;

@ConfigurationConstants
public final class CoreConfigurationConstants
{
	/**
	 * Defines which implementation of the {@link IClasspathScanner} is used during startup. The default is the
	 * {@link de.osthus.ambeth.start.CoreClasspathScanner}.
	 */
	public static final String ClasspathScannerClass = "ambeth.classpath.scanner.class";

	/**
	 * Defines which implementation of the {@link IClasspathInfo} is used during startup. The default is the {@link de.osthus.ambeth.start.SystemClasspathInfo}.
	 */
	public static final String ClasspathInfoClass = "ambeth.classpath.info.class";
	
	/**
	 * Defines which path to scann for the plugin class, every path separated  by semicolon, for example: c:\dev\plugins\*;c:\dev\others\hello.jar
	 */
	public static final String ClasspathPluginPath = "ambeth.classpath.plugin.path";

	/**
	 * Defines in which packages the Ambeth {@link IClasspathScanner} scans for classes which implement or are annotated with a given class (e.g
	 * {@link XmlRootElement}, {@link ApplicationModule}). Multiple patterns can be provided by separating them with a semicolon ';', each pattern has to be a
	 * regular expression. The default pattern is <code>"de/osthus.*"</code>
	 */
	public static final String PackageScanPatterns = "ambeth.classpath.scanner.pattern";

	private CoreConfigurationConstants()
	{
		// Intended blank
	}
}
