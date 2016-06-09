package de.osthus.ambeth.plugin;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ambeth.Ambeth;
import de.osthus.ambeth.bundle.Core;
import de.osthus.ambeth.config.CoreConfigurationConstants;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.BootstrapModule;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.start.IAmbethApplication;

//@TestFrameworkModule({ PluginModule.class })
public class PluginClasspathScannerTest// extends AbstractIocTest
{
	@Test
	public void pluginClassScanInFolderTest() throws Exception
	{
		// get the scan resource absolute root path
		String pluginScanResource = new File(this.getClass().getResource("/pluginScanResource").toURI()).getAbsolutePath();
		// build the scan absolute paths
		String value = "/modules.jar;/classFiles;/jars/*".replaceAll("(^|;)", "$1" + Matcher.quoteReplacement(pluginScanResource));
		value = value.replace('/', File.separatorChar);

		// init ambeth factory
		IAmbethApplication ambethApplication = Ambeth.createBundle(Core.class).withApplicationModules(PluginModule.class)
				.withProperty(CoreConfigurationConstants.ClasspathPluginPath, value).start();
		IServiceContext applicationContext = ambethApplication.getApplicationContext();
		IPluginClasspathScanner pluginScanner = applicationContext.getService(IPluginClasspathScanner.class);

		// scan plugin jar
		List<Class<?>> bootstrapModuleClasses = pluginScanner.scanClassesAnnotatedWith(BootstrapModule.class);
		List<Class<?>> frameworkModuleClasses = pluginScanner.scanClassesAnnotatedWith(FrameworkModule.class);
		Assert.assertFalse(bootstrapModuleClasses.isEmpty());
		Assert.assertFalse(frameworkModuleClasses.isEmpty());

		// inject as bean
		applicationContext.registerBean(bootstrapModuleClasses.get(0));
		applicationContext.registerBean(frameworkModuleClasses.get(0));
	}
}
