package de.osthus.ambeth.plugin;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Matcher;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.osthus.ambeth.Ambeth;
import de.osthus.ambeth.bundle.Core;
import de.osthus.ambeth.config.CoreConfigurationConstants;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.BootstrapModule;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.start.IAmbethApplication;

public class PluginClasspathScannerTest
{
	IServiceContext applicationContext;

	IPluginClasspathScanner pluginScanner;

	@Before
	public void before() throws URISyntaxException
	{
		// get the scan resource absolute root path
		String pluginScanResource = new File(this.getClass().getResource("/pluginScanResource").toURI()).getAbsolutePath();
		// build the scan absolute paths
		String value = "/modules.jar;/jars/*;/onejarfolder".replaceAll("(^|;)", "$1" + Matcher.quoteReplacement(pluginScanResource));
		value = value.replace('/', File.separatorChar);

		// init ambeth factory
		IAmbethApplication ambethApplication = Ambeth.createBundle(Core.class).withApplicationModules(PluginModule.class)
				.withProperty(CoreConfigurationConstants.PluginPaths, value).start();

		applicationContext = ambethApplication.getApplicationContext();
		pluginScanner = applicationContext.getService(IPluginClasspathScanner.class);

	}

	@Test
	public void pluginScanTest() throws Exception
	{
		// scan plugin jar
		List<Class<?>> bootstrapModuleClasses = pluginScanner.scanClassesAnnotatedWith(BootstrapModule.class);
		List<Class<?>> frameworkModuleClasses = pluginScanner.scanClassesAnnotatedWith(FrameworkModule.class);
		Assert.assertFalse(bootstrapModuleClasses.isEmpty());
		Assert.assertFalse(frameworkModuleClasses.isEmpty());
	}

	@Test
	public void pluginClassRegistTest() throws Exception
	{
		List<Class<?>> bootstrapModuleClasses = pluginScanner.scanClassesAnnotatedWith(BootstrapModule.class);
		List<Class<?>> frameworkModuleClasses = pluginScanner.scanClassesAnnotatedWith(FrameworkModule.class);
		// inject as bean
		applicationContext.registerWithLifecycle(bootstrapModuleClasses.get(0).newInstance());
		applicationContext.registerWithLifecycle(frameworkModuleClasses.get(0).newInstance());
	}
}
