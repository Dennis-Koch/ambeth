package de.osthus.esmeralda;

import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.IocBootstrapModule;
import de.osthus.ambeth.ioc.factory.BeanContextFactory;
import de.osthus.esmeralda.ioc.EsmeraldaCoreModule;

public class Main
{
	public static final String CURRENT_TIME = "current.time";

	public static void main(String[] args) throws Exception
	{
		Properties props = Properties.getApplication();
		props.fillWithCommandLineArgs(args);
		props.putString(CURRENT_TIME, Long.toString(System.currentTimeMillis()));
		props.putString("ambeth.log.level", "INFO");
		IServiceContext bootstrapContext = BeanContextFactory.createBootstrap(props, IocBootstrapModule.class);
		try
		{
			bootstrapContext.createService(EsmeraldaCoreModule.class);
		}
		finally
		{
			bootstrapContext.dispose();
		}
	}
}
