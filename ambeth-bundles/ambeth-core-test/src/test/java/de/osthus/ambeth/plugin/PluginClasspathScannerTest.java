package de.osthus.ambeth.plugin;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.osthus.ambeth.config.CoreConfigurationConstants;
import de.osthus.ambeth.config.Properties;
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
	protected PluginClasspathScanner pluginScanner;

	@Autowired
	protected IServiceContext applicationContext;

	protected List<Class<?>> bootstrapModuleClasses;

	protected List<Class<?>> frameworkModuleClasses;

	@BeforeClass
	public static void beforeClass() throws URISyntaxException
	{
		// get the scan resource absolute root path
		String pluginScanResource = new File(PluginClasspathScannerTest.class.getResource("/pluginScanResource").toURI()).getAbsolutePath();
		// for build the scan absolute paths
		((Properties) Properties.getSystem()).putString("pluginScanResource", pluginScanResource);
	}

	@Before
	public void before()
	{
		// scan plugin jar
		bootstrapModuleClasses = pluginScanner.scanClassesAnnotatedWith(BootstrapModule.class);
		frameworkModuleClasses = pluginScanner.scanClassesAnnotatedWith(FrameworkModule.class);
	}

	@Test
	public void pluginScanTest() throws Exception
	{
		Assert.assertEquals(bootstrapModuleClasses.size(), 1);
		Assert.assertEquals(frameworkModuleClasses.size(), 1);
	}

	@Test
	public void pluginClassRegistTest() throws Exception
	{
		// inject as module
		applicationContext.createService("application", bootstrapModuleClasses.toArray(new Class<?>[bootstrapModuleClasses.size()]));
		applicationContext.createService("framework", frameworkModuleClasses.toArray(new Class<?>[frameworkModuleClasses.size()]));
	}

	@Test
	public void pluginRegistModuleTest() throws Exception
	{
		applicationContext.createService("framework", frameworkModuleClasses.get(0));
		applicationContext.createService("application", bootstrapModuleClasses.get(0));
	}
}
