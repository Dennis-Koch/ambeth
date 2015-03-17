package de.osthus.ambeth.extscanner;

import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.IocModule;
import de.osthus.ambeth.ioc.factory.BeanContextFactory;

public class Main
{
	public static final String CURRENT_TIME = "current.time";

	public static void main(String[] args) throws Exception
	{
		Properties props = Properties.getApplication();
		props.fillWithCommandLineArgs(args);
		props.putString(CURRENT_TIME, Long.toString(System.currentTimeMillis()));
		props.putString("ambeth.log.level", "INFO");
		IServiceContext bootstrapContext = BeanContextFactory.createBootstrap(props, IocModule.class);
		try
		{
			bootstrapContext.createService(ScannerModule.class);
		}
		finally
		{
			bootstrapContext.dispose();
		}
	}
}
