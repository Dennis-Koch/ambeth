package com.koch.ambeth.extscanner;

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.IocModule;
import com.koch.ambeth.ioc.factory.BeanContextFactory;
import com.koch.ambeth.log.config.Properties;

public class Main {
	public static final String CURRENT_TIME = "current.time";

	public static void main(String[] args) throws Exception {
		Properties props = Properties.getApplication();
		props.fillWithCommandLineArgs(args);
		props.putString(CURRENT_TIME, Long.toString(System.currentTimeMillis()));
		props.putString("ambeth.log.level", "INFO");
		props.putString("ambeth.log.source", "SHORT");
		IServiceContext bootstrapContext = BeanContextFactory.createBootstrap(props, IocModule.class);
		try {
			bootstrapContext.createService(ScannerModule.class);
		}
		finally {
			bootstrapContext.dispose();
		}
	}
}
