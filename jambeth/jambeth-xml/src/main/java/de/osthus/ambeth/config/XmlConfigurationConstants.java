package de.osthus.ambeth.config;

import javax.xml.bind.annotation.XmlRootElement;

import de.osthus.ambeth.annotation.ConfigurationConstants;
import de.osthus.ambeth.ioc.annotation.BootstrapModule;
import de.osthus.ambeth.util.ClasspathScanner;

@ConfigurationConstants
public final class XmlConfigurationConstants
{
	/**
	 * Defines in which packages the Ambeth {@link ClasspathScanner} scans for classes which implement or are annotated with a given class (e.g
	 * {@link XmlRootElement}, {@link BootstrapModule}). Multiple patterns can be provided by separating them with a semicolon ';', each pattern has to be a
	 * regular expression. The default pattern is <code>"de/osthus.*"</code>
	 */
	public static final String PackageScanPatterns = "ambeth.xml.transfer.pattern";

	private XmlConfigurationConstants()
	{
		// Intended blank
	}
}
