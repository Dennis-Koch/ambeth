package de.osthus.ambeth.testutil;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.config.UtilConfigurationConstants;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.exception.BeanContextInitException;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import de.osthus.ambeth.util.ReflectUtil;

public class RebuildSchema
{
	/**
	 * To fully rebuild schema and data of an application server run with the following program arguments:
	 * 
	 * Local App Server: <code>property.file=src/main/environment/test/iqdf-ui.properties</code>
	 * 
	 * Dev Server (Caution for concurrent users/developers/testers): <code>property.file=src/main/environment/dev/iqdf-ui.properties</code>
	 * 
	 * Demo Server (Caution for concurrent users/developers/testers): <code>property.file=src/main/environment/demo/iqdf-ui.properties</code>
	 * 
	 * @param args
	 * @throws InitializationError
	 * @throws Throwable
	 */
	public static void main(final String[] args, Class<?> testClass, String recommendedPropertyFileName) throws Exception
	{
		Properties.getApplication().fillWithCommandLineArgs(args);
		AmbethInformationBusWithPersistenceRunner runner = new AmbethInformationBusWithPersistenceRunner(testClass)
		{
			@Override
			protected void extendProperties(FrameworkMethod frameworkMethod, Properties props)
			{
				super.extendProperties(frameworkMethod, props);

				// intentionally refill with args a second time
				props.fillWithCommandLineArgs(args);

				String bootstrapPropertyFile = props.getString(UtilConfigurationConstants.BootstrapPropertyFile);
				if (bootstrapPropertyFile == null)
				{
					bootstrapPropertyFile = props.getString(UtilConfigurationConstants.BootstrapPropertyFile.toUpperCase());
				}
				if (bootstrapPropertyFile != null)
				{
					System.out.println("Environment property '" + UtilConfigurationConstants.BootstrapPropertyFile + "' found with value '"
							+ bootstrapPropertyFile + "'");
					props.load(bootstrapPropertyFile, false);
				}
				props.put(PersistenceJdbcConfigurationConstants.IntegratedConnectionFactory, true);
				if (props.get("ambeth.log.level") == null)
				{
					props.put("ambeth.log.level", "INFO");
				}
				// intentionally refill with args a third time
				props.fillWithCommandLineArgs(args);
			}
		};
		try
		{
			runner.rebuildSchemaContext();
		}
		catch (BeanContextInitException e)
		{
			if (!e.getMessage().startsWith("Could not resolve mandatory environment property 'database.schema.name'"))
			{
				throw e;
			}
			IllegalArgumentException ex = new IllegalArgumentException("Please specify the corresponding property file e.g.:\nproperty.file="
					+ recommendedPropertyFileName);
			ex.setStackTrace(RuntimeExceptionUtil.EMPTY_STACK_TRACE);
			throw ex;
		}
		runner.rebuildStructure();
		runner.rebuildData();

		FrameworkMethod method = new FrameworkMethod(ReflectUtil.getDeclaredMethod(false, Object.class, String.class, "toString"));
		DataSetupExecutor.setAutoRebuildData(Boolean.TRUE);
		try
		{
			runner.rebuildContext(method);
		}
		finally
		{
			DataSetupExecutor.setAutoRebuildData(null);
		}
		runner.rebuildContext();
		try
		{
			runner.methodInvoker(method, runner.createTest());
		}
		finally
		{
			runner.disposeContext();
		}
		try
		{
			runner.finalize();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
