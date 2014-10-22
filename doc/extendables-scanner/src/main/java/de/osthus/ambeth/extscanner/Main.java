package de.osthus.ambeth.extscanner;

import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.IocBootstrapModule;
import de.osthus.ambeth.ioc.factory.BeanContextFactory;

public class Main
{
	public static void main(String[] args) throws Exception
	{
		Properties props = Properties.getApplication();
		props.fillWithCommandLineArgs(args);
		IServiceContext bootstrapContext = BeanContextFactory.createBootstrap(props);
		try
		{
			bootstrapContext.createService(ExtScannerModule.class, IocBootstrapModule.class);
		}
		finally
		{
			bootstrapContext.dispose();
		}
	}
}
