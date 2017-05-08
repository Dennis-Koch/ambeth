package com.koch.ambeth.testutil;

/*-
 * #%L
 * jambeth-information-bus-with-persistence-test
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import com.koch.ambeth.ioc.exception.BeanContextInitException;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.util.ReflectUtil;
import com.koch.ambeth.util.config.UtilConfigurationConstants;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class RebuildSchema {
	/**
	 * To fully rebuild schema and data of an application server run with the following program
	 * arguments:
	 *
	 * Local App Server: <code>property.file=src/main/environment/test/iqdf-ui.properties</code>
	 *
	 * Dev Server (Caution for concurrent users/developers/testers):
	 * <code>property.file=src/main/environment/dev/iqdf-ui.properties</code>
	 *
	 * Demo Server (Caution for concurrent users/developers/testers):
	 * <code>property.file=src/main/environment/demo/iqdf-ui.properties</code>
	 *
	 * @param args
	 * @throws InitializationError
	 * @throws Throwable
	 */
	public static void main(final String[] args, Class<?> testClass,
			String recommendedPropertyFileName) throws Exception {
		Properties.getApplication().fillWithCommandLineArgs(args);
		AmbethInformationBusWithPersistenceRunner runner =
				new AmbethInformationBusWithPersistenceRunner(testClass) {
					@Override
					protected void extendPropertiesInstance(FrameworkMethod frameworkMethod,
							Properties props) {
						super.extendPropertiesInstance(frameworkMethod, props);

						// intentionally refill with args a second time
						props.fillWithCommandLineArgs(args);

						String bootstrapPropertyFile =
								props.getString(UtilConfigurationConstants.BootstrapPropertyFile);
						if (bootstrapPropertyFile == null) {
							bootstrapPropertyFile =
									props.getString(UtilConfigurationConstants.BootstrapPropertyFile.toUpperCase());
						}
						if (bootstrapPropertyFile != null) {
							System.out.println(
									"Environment property '" + UtilConfigurationConstants.BootstrapPropertyFile
											+ "' found with value '" + bootstrapPropertyFile + "'");
							props.load(bootstrapPropertyFile, false);
						}
						props.put(PersistenceJdbcConfigurationConstants.IntegratedConnectionFactory, true);
						if (props.get("ambeth.log.level") == null) {
							props.put("ambeth.log.level", "INFO");
						}
						// intentionally refill with args a third time
						props.fillWithCommandLineArgs(args);
					}
				};
		try {
			runner.rebuildSchemaContext();
		}
		catch (BeanContextInitException e) {
			if (!e.getMessage()
					.startsWith("Could not resolve mandatory environment property 'database.schema.name'")) {
				throw e;
			}
			IllegalArgumentException ex = new IllegalArgumentException(
					"Please specify the corresponding property file e.g.:\nproperty.file="
							+ recommendedPropertyFileName);
			ex.setStackTrace(RuntimeExceptionUtil.EMPTY_STACK_TRACE);
			throw ex;
		}
		runner.rebuildStructure();
		runner.rebuildData();

		FrameworkMethod method = new FrameworkMethod(
				ReflectUtil.getDeclaredMethod(false, Object.class, String.class, "toString"));
		DataSetupExecutor.setAutoRebuildData(Boolean.TRUE);
		try {
			runner.rebuildContext(method);
		}
		finally {
			DataSetupExecutor.setAutoRebuildData(null);
		}
		runner.rebuildContext();
		try {
			runner.methodInvoker(method, runner.createTest());
		}
		finally {
			runner.disposeContext();
		}
		try {
			runner.finalize();
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
