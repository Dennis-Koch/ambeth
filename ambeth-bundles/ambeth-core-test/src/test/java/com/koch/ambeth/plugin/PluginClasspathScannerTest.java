package com.koch.ambeth.plugin;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.koch.ambeth.core.config.CoreConfigurationConstants;
import com.koch.ambeth.core.plugin.PluginClasspathScanner;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.annotation.BootstrapModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.testutil.AbstractIocTest;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.testutil.TestProperties;

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
