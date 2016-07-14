package de.osthus.ambeth.plugin;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.osthus.ambeth.config.CoreConfigurationConstants;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.annotation.BootstrapModule;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.testutil.AbstractIocTest;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;

@TestModule(PluginModule.class)
@TestProperties(name = CoreConfigurationConstants.PluginPaths, value = "${pluginScanResource}/modules.jar;${pluginScanResource}/jars/;${pluginScanResource}/onejarfolder")
public class PluginClasspathScannerTest extends AbstractIocTest
{
	@Autowired
	private IPluginClasspathScanner pluginScanner;
	@Autowired
	private IServiceContext applicationContext;

	@BeforeClass
	public static void before() throws URISyntaxException
	{
		// get the scan resource absolute root path
		String pluginScanResource = new File(PluginClasspathScannerTest.class.getResource("/pluginScanResource").toURI()).getAbsolutePath();
		// for build the scan absolute paths
		System.setProperty("pluginScanResource", pluginScanResource);
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
		// inject as module
		applicationContext.createService("application", bootstrapModuleClasses.toArray(new Class<?>[bootstrapModuleClasses.size()]));
		applicationContext.createService("framework", frameworkModuleClasses.toArray(new Class<?>[frameworkModuleClasses.size()]));
	}
}
