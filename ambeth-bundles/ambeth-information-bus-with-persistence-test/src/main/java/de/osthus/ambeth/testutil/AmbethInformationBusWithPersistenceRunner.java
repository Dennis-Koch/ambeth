package de.osthus.ambeth.testutil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.PersistenceException;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import de.osthus.ambeth.annotation.IAnnotationInfo;
import de.osthus.ambeth.appendable.AppendableStringBuilder;
import de.osthus.ambeth.changecontroller.IChangeController;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.config.UtilConfigurationConstants;
import de.osthus.ambeth.exception.MaskingRuntimeException;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.io.FileUtil;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.factory.BeanContextFactory;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.ILoggerCache;
import de.osthus.ambeth.merge.ChangeControllerState;
import de.osthus.ambeth.merge.ILightweightTransaction;
import de.osthus.ambeth.merge.config.MergeConfigurationConstants;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.orm.XmlDatabaseMapper;
import de.osthus.ambeth.persistence.IConnectionDialect;
import de.osthus.ambeth.persistence.jdbc.IConnectionFactory;
import de.osthus.ambeth.persistence.jdbc.IConnectionTestDialect;
import de.osthus.ambeth.persistence.jdbc.JdbcUtil;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import de.osthus.ambeth.persistence.jdbc.connection.IPreparedConnectionHolder;
import de.osthus.ambeth.persistence.jdbc.connector.DialectSelectorModule;
import de.osthus.ambeth.persistence.jdbc.testconnector.DialectSelectorTestModule;
import de.osthus.ambeth.proxy.IMethodLevelBehavior;
import de.osthus.ambeth.proxy.IProxyFactory;
import de.osthus.ambeth.security.DefaultAuthentication;
import de.osthus.ambeth.security.ISecurityContextHolder;
import de.osthus.ambeth.security.PasswordType;
import de.osthus.ambeth.security.SecurityContextType;
import de.osthus.ambeth.security.SecurityFilterInterceptor;
import de.osthus.ambeth.security.SecurityFilterInterceptor.SecurityMethodMode;
import de.osthus.ambeth.security.StringSecurityScope;
import de.osthus.ambeth.security.TestAuthentication;
import de.osthus.ambeth.testutil.datagenerator.TestDataModule;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.NullPrintStream;
import de.osthus.ambeth.util.setup.IDataSetup;
import de.osthus.ambeth.util.setup.SetupModule;
import de.osthus.ambeth.xml.DefaultXmlWriter;

/**
 * TODO: Handle test methods which change the structure
 */
public class AmbethInformationBusWithPersistenceRunner extends AmbethInformationBusRunner
{
	protected static final String MEASUREMENT_BEAN = "measurementBean";

	protected boolean isRebuildDataForThisTestRecommended;

	private Connection connection;

	private IServiceContext schemaContext;

	protected boolean doExecuteStrict = false;

	/** Flag which is set to true after the structure was build. */
	private boolean isStructureRebuildAlreadyHandled = false;

	/** Flag which is set to true after the first test method was executed. */
	private boolean isFirstTestMethodAlreadyExecuted;

	/** Flag which is set if the last test method has triggered a context rebuild. */
	private boolean lastMethodTriggersContextRebuild;

	public AmbethInformationBusWithPersistenceRunner(final Class<?> testClass) throws InitializationError
	{
		super(testClass);
	}

	@Override
	protected void finalize() throws Throwable
	{
		if (connection != null && !connection.isClosed())
		{
			connection.close();
		}
		if (schemaContext != null)
		{
			schemaContext.getRoot().dispose();
			schemaContext = null;
		}
		super.finalize();
	}

	public void setDoExecuteStrict(final boolean doExecuteStrict)
	{
		this.doExecuteStrict = doExecuteStrict;
	}

	public void rebuildData()
	{
		rebuildData(null);
	}

	@SuppressWarnings("resource")
	@Override
	protected void extendPropertiesInstance(FrameworkMethod frameworkMethod, Properties props)
	{
		super.extendPropertiesInstance(frameworkMethod, props);

		String testForkSuffix = props.getString(UtilConfigurationConstants.ForkName);
		if (testForkSuffix != null)
		{
			String databaseUser = props.getString(PersistenceJdbcConfigurationConstants.DatabaseUser);
			props.putString(PersistenceJdbcConfigurationConstants.DatabaseUser, databaseUser + "_" + testForkSuffix);
		}
		DialectSelectorModule.fillProperties(props);

		try
		{
			Connection schemaConnection = connection != null && !connection.isClosed() ? connection : null;
			if (schemaConnection == null || !schemaConnection.isWrapperFor(IPreparedConnectionHolder.class))
			{
				return;
			}
			schemaConnection.unwrap(IPreparedConnectionHolder.class).setPreparedConnection(true);
			ArrayList<Connection> preparedConnections = new ArrayList<Connection>(1);
			preparedConnections.add(schemaConnection);
			props.put(PersistenceJdbcConfigurationConstants.PreparedConnectionInstances, preparedConnections);
		}
		catch (SQLException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}

	}

	@Override
	protected List<Class<? extends IInitializingModule>> buildFrameworkTestModuleList(FrameworkMethod frameworkMethod)
	{
		List<Class<? extends IInitializingModule>> frameworkTestModuleList = super.buildFrameworkTestModuleList(frameworkMethod);
		frameworkTestModuleList.add(DialectSelectorTestModule.class);
		frameworkTestModuleList.add(DataSetupExecutorModule.class);
		frameworkTestModuleList.add(SetupModule.class);
		frameworkTestModuleList.add(TestDataModule.class);
		return frameworkTestModuleList;
	}

	public void rebuildSchemaContext()
	{
		if (schemaContext != null)
		{
			schemaContext.getRoot().dispose();
			schemaContext = null;
		}
		Properties.resetApplication();

		PrintStream oldPrintStream = System.out;
		System.setOut(NullPrintStream.INSTANCE);
		try
		{
			Properties.loadBootstrapPropertyFile();
		}
		finally
		{
			System.setOut(oldPrintStream);
		}

		Properties baseProps = new Properties(Properties.getApplication());
		// baseProps.putString("ambeth.log.level", "WARN");

		extendPropertiesInstance(null, baseProps);

		IServiceContext schemaBootstrapContext = null;
		boolean success = false;
		try
		{
			schemaBootstrapContext = BeanContextFactory.createBootstrap(baseProps);
			schemaContext = schemaBootstrapContext.createService(AmbethPersistenceSchemaModule.class);
			success = true;
		}
		finally
		{
			if (!success && schemaBootstrapContext != null)
			{
				schemaBootstrapContext.dispose();
			}
		}
	}

	@Override
	protected void rebuildContext(FrameworkMethod frameworkMethod)
	{
		if (frameworkMethod == null)
		{
			return;
		}
		if (isRebuildDataForThisTestRecommended)
		{
			DataSetupExecutor.setAutoRebuildData(Boolean.TRUE);
			try
			{
				super.rebuildContext(frameworkMethod);
			}
			finally
			{
				DataSetupExecutor.setAutoRebuildData(null);
				isRebuildDataForThisTestRecommended = false;
			}
		}
		else
		{
			super.rebuildContext(frameworkMethod);
		}
		try
		{
			if (connection != null && !connection.isClosed())
			{
				connection.rollback();
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		beanContext.getService(ILightweightTransaction.class).runInTransaction(new IBackgroundWorkerDelegate()
		{
			@Override
			public void invoke() throws Throwable
			{
				// Intended blank
			}
		});
	}

	@Override
	protected void rebuildContextDetails(final IBeanContextFactory childContextFactory)
	{
		super.rebuildContextDetails(childContextFactory);

		childContextFactory.registerBean(MEASUREMENT_BEAN, Measurement.class).propertyValue("TestClassName", getTestClass().getJavaClass())
				.autowireable(IMeasurement.class);
	}

	@Override
	protected void runChild(FrameworkMethod method, RunNotifier notifier)
	{
		isRebuildDataForThisTestRecommended = false;
		super.runChild(method, notifier);
	}

	@Override
	protected org.junit.runners.model.Statement withBeforeClasses(org.junit.runners.model.Statement statement)
	{
		checkOS();
		return super.withBeforeClasses(statement);
	}

	@Override
	protected org.junit.runners.model.Statement withAfterClasses(final org.junit.runners.model.Statement statement)
	{
		final org.junit.runners.model.Statement resultStatement = super.withAfterClasses(statement);
		return new org.junit.runners.model.Statement()
		{
			@Override
			public void evaluate() throws Throwable
			{
				resultStatement.evaluate();
				try
				{
					try
					{
						// After all test methods of the test class have been executed we probably have to delete the
						// test data
						Connection conn = getConnection();

						if (testUserHasBeenCreated)
						{
							JdbcUtil.close(connection);
							connection = null;
							IProperties testProps = getOrCreateSchemaContext().getService(IProperties.class);
							getOrCreateSchemaContext().getService(IConnectionTestDialect.class).dropCreatedTestUser(
									testProps.getString(PersistenceJdbcConfigurationConstants.DatabaseUser),
									testProps.getString(PersistenceJdbcConfigurationConstants.DatabasePass), testProps);
							testUserHasBeenCreated = false;
						}
						else
						{
							String[] schemaNames = getSchemaNames();
							truncateMainSchema(conn, schemaNames[0]);
							truncateAdditionalSchemas(conn, schemaNames, true);
						}
					}
					finally
					{
						JdbcUtil.close(connection);
						connection = null;
						if (schemaContext != null)
						{
							schemaContext.getRoot().dispose();
							schemaContext = null;
						}
					}
				}
				catch (Exception e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}
			}
		};
	}

	private boolean hasStructureAnnotation()
	{
		return !findAnnotations(getTestClass().getJavaClass(), SQLStructureList.class, SQLStructure.class).isEmpty();

	}

	public void rebuildStructure()
	{
		Class<?> callingClass = getTestClass().getJavaClass();
		try
		{
			Connection connection = getConnection();

			boolean oldAutoCommit = connection.getAutoCommit();
			if (!oldAutoCommit)
			{
				connection.setAutoCommit(true);
			}
			try
			{
				ensureSchemaEmpty(connection);

				getOrCreateSchemaContext().getService(IConnectionTestDialect.class).preStructureRebuild(connection);
				ISchemaRunnable[] structureRunnables = getStructureRunnables(callingClass, callingClass);
				for (ISchemaRunnable structRunnable : structureRunnables)
				{
					structRunnable.executeSchemaSql(connection);
				}

				ensureExistanceOfNeededDatabaseObjects(connection);
			}
			finally
			{
				if (!oldAutoCommit)
				{
					connection.setAutoCommit(false);
				}
			}
			isStructureRebuildAlreadyHandled = true;
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected void rebuildData(final FrameworkMethod frameworkMethod)
	{
		try
		{
			Class<?> callingClass = getTestClass().getJavaClass();
			Connection conn = getConnection();

			truncateAllTablesBySchema(conn, getSchemaNames());
			truncateAllTablesExplicitlyGiven(conn, getConfiguredExternalTableNames(callingClass));

			ISchemaRunnable[] dataRunnables = getDataRunnables(callingClass, callingClass, frameworkMethod);
			executeWithDeferredConstraints(dataRunnables);
		}
		catch (Exception e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected void executeAdditionalDataRunnables(final FrameworkMethod frameworkMethod)
	{
		try
		{
			ISchemaRunnable[] dataRunnables = getDataRunnables(getTestClass().getJavaClass(), null, frameworkMethod);
			executeWithDeferredConstraints(dataRunnables);
		}
		catch (Exception e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	// @Override
	// protected void runChildWithContext(FrameworkMethod frameworkMethod, RunNotifier notifier, boolean hasContextBeenRebuild, boolean doDataRebuild)
	// {
	// boolean doContextRebuild = false;
	// Method method = frameworkMethod.getMethod();
	// try
	// {
	// boolean doStructureRebuild = !isStructureRebuildAlreadyHandled && hasStructureAnnotation();
	// boolean methodTriggersContextRebuild = method.isAnnotationPresent(TestModule.class) || method.isAnnotationPresent(TestProperties.class)
	// || method.isAnnotationPresent(TestPropertiesList.class);
	// doContextRebuild = beanContext == null || beanContext.isDisposed() || doStructureRebuild || methodTriggersContextRebuild
	// || lastMethodTriggersContextRebuild;
	// lastMethodTriggersContextRebuild = methodTriggersContextRebuild;
	// doDataRebuild |= isDataRebuildDemanded();
	// if (!doDataRebuild) // handle the special cases for SQLDataRebuild=false
	// {
	// // If SQL data on class level -> run data SQL before the first test method
	// if (!isFirstTestMethodAlreadyExecuted)
	// {
	// doDataRebuild = !findAnnotations(getTestClass().getJavaClass(), SQLDataList.class, SQLData.class).isEmpty();
	// }
	// }
	// boolean doAddAdditionalMethodData = false; // Flag if SQL method data should be inserted (without deleting
	// // existing database entries)
	// if (!doDataRebuild) // included in data rebuild -> only check if data rebuild isn't done
	// {
	// doAddAdditionalMethodData = method.isAnnotationPresent(SQLData.class) || method.isAnnotationPresent(SQLDataList.class);
	// }
	//
	// if (doStructureRebuild)
	// {
	// rebuildStructure();
	// }
	// if (doDataRebuild)
	// {
	// rebuildData(frameworkMethod);
	// }
	// if (doAddAdditionalMethodData)
	// {
	// executeAdditionalDataRunnables(frameworkMethod);
	// }
	// // Do context rebuild after the database changes have been made because the beans may access the data e.g.
	// // in their afterStarted method
	// if (doContextRebuild)
	// {
	// rebuildContext(frameworkMethod, doDataRebuild);
	// doDataRebuild = false;
	// }
	// // Trigger clearing of other maps and caches (QueryResultCache,...)
	// beanContext.getService(IEventDispatcher.class).dispatchEvent(ClearAllCachesEvent.getInstance());
	//
	// isFirstTestMethodAlreadyExecuted = true;
	// }
	// catch (MaskingRuntimeException e)
	// {
	// notifier.fireTestFailure(new Failure(Description.createTestDescription(getTestClass().getJavaClass(), method.getName()), e.getMessage() == null ? e
	// .getCause() : e));
	// return;
	// }
	// catch (Throwable e)
	// {
	// notifier.fireTestFailure(new Failure(Description.createTestDescription(getTestClass().getJavaClass(), method.getName()), e));
	// return;
	// }
	// super.runChildWithContext(frameworkMethod, notifier, doContextRebuild, doDataRebuild);
	// }

	@Override
	protected org.junit.runners.model.Statement methodBlock(final FrameworkMethod frameworkMethod)
	{
		final org.junit.runners.model.Statement statement = super.methodBlock(frameworkMethod);
		return new org.junit.runners.model.Statement()
		{
			@Override
			public void evaluate() throws Throwable
			{
				boolean doContextRebuild = false;
				Method method = frameworkMethod.getMethod();
				boolean doStructureRebuild = !isStructureRebuildAlreadyHandled && hasStructureAnnotation();
				boolean methodTriggersContextRebuild = method.isAnnotationPresent(TestModule.class) || method.isAnnotationPresent(TestProperties.class)
						|| method.isAnnotationPresent(TestPropertiesList.class);
				doContextRebuild = beanContext == null || beanContext.isDisposed() || doStructureRebuild || methodTriggersContextRebuild
						|| lastMethodTriggersContextRebuild;
				lastMethodTriggersContextRebuild = methodTriggersContextRebuild;
				boolean doDataRebuild = isDataRebuildDemanded();
				if (!doDataRebuild) // handle the special cases for SQLDataRebuild=false
				{
					// If SQL data on class level -> run data SQL before the first test method
					if (!isFirstTestMethodAlreadyExecuted)
					{
						doDataRebuild = !findAnnotations(getTestClass().getJavaClass(), SQLDataList.class, SQLData.class).isEmpty();
					}
				}
				boolean doAddAdditionalMethodData = false; // Flag if SQL method data should be inserted (without deleting
															// existing database entries)
				if (!doDataRebuild) // included in data rebuild -> only check if data rebuild isn't done
				{
					doAddAdditionalMethodData = method.isAnnotationPresent(SQLData.class) || method.isAnnotationPresent(SQLDataList.class);
				}

				if (doStructureRebuild)
				{
					rebuildStructure();
				}
				if (doDataRebuild)
				{
					rebuildData(frameworkMethod);
				}
				if (doAddAdditionalMethodData)
				{
					executeAdditionalDataRunnables(frameworkMethod);
				}
				// Do context rebuild after the database changes have been made because the beans may access the data e.g.
				// in their afterStarted method
				isRebuildContextForThisTestRecommended = doContextRebuild;
				isFirstTestMethodAlreadyExecuted = true;
				isRebuildDataForThisTestRecommended = doDataRebuild;

				try
				{
					statement.evaluate();
				}
				catch (MaskingRuntimeException e)
				{
					throw RuntimeExceptionUtil.mask(e, Throwable.class); // potentially unwraps redundant MaskingRuntimeException
				}
			}
		};
	}

	@Override
	protected org.junit.runners.model.Statement methodInvoker(final FrameworkMethod method, Object test)
	{
		final org.junit.runners.model.Statement parentStatement = AmbethInformationBusWithPersistenceRunner.super.methodInvoker(method, test);
		final org.junit.runners.model.Statement statement = new org.junit.runners.model.Statement()
		{
			@Override
			public void evaluate() throws Throwable
			{
				IDataSetup dataSetup = beanContext.getParent().getService(IDataSetup.class, false);
				if (dataSetup != null)
				{
					dataSetup.refreshEntityReferences();
				}
				parentStatement.evaluate();
			}
		};
		return new org.junit.runners.model.Statement()
		{
			@Override
			public void evaluate() throws Throwable
			{
				if (isRebuildDataForThisTestRecommended)
				{
					beanContext.getService(DataSetupExecutor.class).rebuildData();
					isRebuildDataForThisTestRecommended = false;
				}
				boolean securityActive = Boolean.parseBoolean(beanContext.getService(IProperties.class).getString(MergeConfigurationConstants.SecurityActive,
						"false"));
				if (!securityActive)
				{
					statement.evaluate();
					return;
				}

				ChangeControllerState changeControllerState = method.getAnnotation(ChangeControllerState.class);

				boolean changeControllerActiveTest = false;
				final IChangeController changeController = beanContext.getService(IChangeController.class);
				if (changeControllerState != null)
				{
					if (changeController != null)
					{
						IConversionHelper conversionHelper = beanContext.getService(IConversionHelper.class);
						Boolean active = conversionHelper.convertValueToType(Boolean.class, changeControllerState.active());
						if (Boolean.TRUE.equals(active))
						{
							changeControllerActiveTest = true;
						}
					}

				}
				final boolean changeControllerActive = changeControllerActiveTest;

				TestAuthentication authentication = method.getAnnotation(TestAuthentication.class);
				if (authentication == null)
				{
					Class<?> testClass = getTestClass().getJavaClass();
					authentication = testClass.getAnnotation(TestAuthentication.class);
				}
				if (authentication == null)
				{
					statement.evaluate();
					return;
				}
				final ISecurityScope scope = new StringSecurityScope(authentication.scope());

				IMethodLevelBehavior<SecurityMethodMode> behaviour = new IMethodLevelBehavior<SecurityMethodMode>()
				{
					private final SecurityMethodMode mode = new SecurityMethodMode(SecurityContextType.AUTHENTICATED, -1, -1, null, -1, scope);

					@Override
					public SecurityMethodMode getBehaviourOfMethod(Method method)
					{
						return mode;
					}

					@Override
					public SecurityMethodMode getDefaultBehaviour()
					{
						return mode;
					}
				};

				SecurityFilterInterceptor interceptor = beanContext.registerBean(SecurityFilterInterceptor.class)
						.propertyValue("MethodLevelBehaviour", behaviour).propertyValue("Target", statement).finish();
				org.junit.runners.model.Statement stmt = (org.junit.runners.model.Statement) beanContext.getService(IProxyFactory.class).createProxy(
						new Class<?>[] { org.junit.runners.model.Statement.class }, interceptor);
				final org.junit.runners.model.Statement fStatement = stmt;
				ISecurityContextHolder securityContextHolder = beanContext.getService(ISecurityContextHolder.class);
				securityContextHolder.setScopedAuthentication(new DefaultAuthentication(authentication.name(), authentication.password().toCharArray(),
						PasswordType.PLAIN), new IResultingBackgroundWorkerDelegate<Object>()
				{
					@Override
					public Object invoke() throws Throwable
					{
						if (changeControllerActive && changeController != null)
						{
							changeController.runWithoutEDBL(new IResultingBackgroundWorkerDelegate<Object>()
							{

								@Override
								public Object invoke() throws Throwable
								{
									fStatement.evaluate();
									return null;
								}
							});
						}
						else
						{
							fStatement.evaluate();
						}
						return null;

					}
				});
			}
		};
	}

	/**
	 * @return Flag if data rebuild is demanded (checks the test class annotation or returns the default value).
	 */
	private boolean isDataRebuildDemanded()
	{
		boolean result = true; // default value if no annotation is found
		List<IAnnotationInfo<?>> sqlDataRebuilds = findAnnotations(getTestClass().getJavaClass(), SQLDataRebuild.class);
		if (sqlDataRebuilds.size() > 0)
		{
			IAnnotationInfo<?> topDataRebuild = sqlDataRebuilds.get(sqlDataRebuilds.size() - 1);
			result = ((SQLDataRebuild) topDataRebuild.getAnnotation()).value();
		}
		return result;
	}

	/**
	 * @return Flag if a truncate of the data tables (on test class level) is demanded (checks the test class annotation or returns the default value).
	 */
	private boolean isTruncateOnClassDemanded()
	{
		boolean result = true; // default value
		List<IAnnotationInfo<?>> sqlDataRebuilds = findAnnotations(getTestClass().getJavaClass(), SQLDataRebuild.class);
		if (sqlDataRebuilds.size() > 0)
		{
			IAnnotationInfo<?> topDataRebuild = sqlDataRebuilds.get(sqlDataRebuilds.size() - 1);
			if (topDataRebuild.getAnnotation() instanceof SQLDataRebuild)
			{
				result = ((SQLDataRebuild) topDataRebuild.getAnnotation()).truncateOnClass();
			}
		}
		return result;
	}

	private final String nl = System.getProperty("line.separator");

	private final Pattern lineSeparator = Pattern.compile(nl);

	private final Pattern pathSeparator = Pattern.compile(File.pathSeparator);

	private final Pattern optionSeparator = Pattern.compile("--");

	private final Pattern optionPattern = Pattern.compile("(.+?)=(.*)");

	private final Pattern whitespaces = Pattern.compile("[ \\t]+");

	private final Pattern[] sqlComments = { Pattern.compile("^--[^:].*"), Pattern.compile("^/\\*.*\\*/"), Pattern.compile(" *@@@ *") };

	private final Pattern[] ignoreOutside = { Pattern.compile("^/$") };

	private final Pattern[] ignoreIfContains = { Pattern.compile(".*?DROP CONSTRAINT.*?") };

	private final Pattern[][] sqlCommands = {
			{ Pattern.compile("CREATE( +OR +REPLACE)? +(?:TABLE|VIEW|INDEX|TYPE|SEQUENCE|SYNONYM|TABLESPACE) +.+", Pattern.CASE_INSENSITIVE),
					Pattern.compile(".*?([;\\/]|@@@)") },
			{ Pattern.compile("CREATE( +OR +REPLACE)? +(?:FUNCTION|PROCEDURE|TRIGGER) +.+", Pattern.CASE_INSENSITIVE),
					Pattern.compile(".*?END(?:[;\\/]|@@@)", Pattern.CASE_INSENSITIVE) },
			{ Pattern.compile("ALTER +(?:TABLE|VIEW) .+", Pattern.CASE_INSENSITIVE), Pattern.compile(".*?([;\\/]|@@@)") },
			{ Pattern.compile("CALL +.+", Pattern.CASE_INSENSITIVE), Pattern.compile(".*?([;\\/]|@@@)") },
			{ Pattern.compile("(?:INSERT +INTO|UPDATE) .+", Pattern.CASE_INSENSITIVE), Pattern.compile(".*?([;\\/]|@@@)") },
			{ Pattern.compile("(?:COMMENT) .+", Pattern.CASE_INSENSITIVE), Pattern.compile(".*?([;\\/]|@@@)") } };

	private final Pattern[][] sqlIgnoredCommands = { { Pattern.compile("DROP +.+", Pattern.CASE_INSENSITIVE), Pattern.compile(".*?([;\\/]|@@@)") } };

	// TODO Check only implemented for first array element
	private final Pattern[][] sqlTryOnlyCommands = { { Pattern.compile("CREATE OR REPLACE *.*") } };

	private final Pattern optionLine = Pattern.compile("^--:(.*)");

	protected final StringBuilder measurementXML = new StringBuilder();

	protected final DefaultXmlWriter xmlWriter = new DefaultXmlWriter(new AppendableStringBuilder(measurementXML), null);

	protected boolean testUserHasBeenCreated;

	protected ILogger getLog()
	{
		return schemaContext.getService(ILoggerCache.class).getCachedLogger(schemaContext, AmbethInformationBusWithPersistenceRunner.class);
	}

	/**
	 * Due to a lot of new DB connections during tests /dev/random on CI servers may run low.
	 */
	private void checkOS()
	{
		String os = System.getProperty("os.name").toLowerCase();
		if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0)
		{
			System.setProperty("java.security.egd", "file:///dev/urandom"); // the 3 '/' are important to make it an URL
		}
	}

	private void ensureExistanceOfNeededDatabaseObjects(final Connection conn) throws SQLException
	{
		createOptimisticLockingTriggers(conn);
		createPermissionGroups(conn);
		getOrCreateSchemaContext().getService(IConnectionDialect.class).preProcessConnection(conn, getSchemaNames(), true);
		getOrCreateSchemaContext().getService(IConnectionTestDialect.class).preProcessConnectionForTest(conn, getSchemaNames(), true);
	}

	protected void logMeasurement(final String name, final Object value)
	{
		String elementName = name.replaceAll(" ", "_").replaceAll("\\.", "_").replaceAll("\\(", ":").replaceAll("\\)", ":");
		xmlWriter.writeOpenElement(elementName);
		xmlWriter.writeEscapedXml(value.toString());
		xmlWriter.writeCloseElement(elementName);
	}

	private Connection getConnection() throws SQLException
	{
		if (connection != null && !connection.isClosed())
		{
			return connection;
		}
		Connection conn;
		try
		{
			conn = getOrCreateSchemaContext().getService(IConnectionFactory.class).create();
		}
		catch (MaskingRuntimeException e)
		{
			Throwable cause = e.getCause();
			while (cause instanceof MaskingRuntimeException)
			{
				cause = cause.getCause();
			}
			IProperties testProps = getOrCreateSchemaContext().getService(IProperties.class);
			testUserHasBeenCreated = getOrCreateSchemaContext().getService(IConnectionTestDialect.class).createTestUserIfSupported(cause,
					testProps.getString(PersistenceJdbcConfigurationConstants.DatabaseUser),
					testProps.getString(PersistenceJdbcConfigurationConstants.DatabasePass), testProps);
			if (!testUserHasBeenCreated)
			{
				throw e;
			}
			try
			{
				conn = getOrCreateSchemaContext().getService(IConnectionFactory.class).create();
			}
			catch (Throwable t)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		connection = conn;
		return connection;
	}

	/**
	 * Get the already existing schema context or call <code>rebuildSchemaContext</code> to create a new one.
	 * 
	 * @return Schema content
	 */
	protected IServiceContext getOrCreateSchemaContext()
	{
		if (schemaContext == null || schemaContext.isDisposed())
		{
			rebuildSchemaContext();
		}
		return schemaContext;
	}

	private String[] getSchemaNames()
	{
		IServiceContext schemaContext = getOrCreateSchemaContext();
		IProperties properties = schemaContext.getService(IProperties.class);
		String schemaProperty = (String) properties.get(PersistenceJdbcConfigurationConstants.DatabaseSchemaName);
		String[] schemaNames = schemaContext.getService(IConnectionDialect.class).toDefaultCase(schemaProperty).split("[:;]");
		return schemaNames;
	}

	private String[] getConfiguredExternalTableNames(final Class<?> type)
	{
		String[] configuredSynonymNames;
		List<IAnnotationInfo<?>> annotations = findAnnotations(type, SQLTableSynonyms.class);
		if (annotations.size() == 1)
		{
			IAnnotationInfo<?> annoInfo = annotations.get(0);
			SQLTableSynonyms anno = (SQLTableSynonyms) annoInfo.getAnnotation();
			configuredSynonymNames = anno.value();
		}
		else
		{
			configuredSynonymNames = new String[0];
		}

		return configuredSynonymNames;
	}

	private void ensureSchemaEmpty(final Connection conn) throws SQLException
	{
		String[] schemaNames = getSchemaNames();
		if (!getOrCreateSchemaContext().getService(IConnectionTestDialect.class).isEmptySchema(conn))
		{
			truncateMainSchema(conn, schemaNames[0]);
		}
		truncateAdditionalSchemas(conn, schemaNames, false);
	}

	private void truncateMainSchema(final Connection conn, String mainSchemaName) throws SQLException
	{
		if (hasStructureAnnotation())
		{
			getOrCreateSchemaContext().getService(IConnectionTestDialect.class).dropAllSchemaContent(conn, mainSchemaName);
		}
		else
		{
			if (isTruncateOnClassDemanded())
			{
				truncateAllTablesBySchema(conn, null, mainSchemaName);
			}
		}
	}

	private void truncateAdditionalSchemas(final Connection conn, String[] schemaNames, boolean skipEmptyCheck) throws SQLException
	{
		truncateAllTablesExplicitlyGiven(conn, getConfiguredExternalTableNames(getTestClass().getJavaClass()));

		if (schemaNames != null)
		{
			boolean truncateOnClassDemanded = isTruncateOnClassDemanded();
			for (int i = schemaNames.length; i-- > 1;)
			{
				String schemaName = schemaNames[i];
				if (skipEmptyCheck || !checkAdditionalSchemaEmpty(conn, schemaName))
				{
					if (truncateOnClassDemanded)
					{
						truncateAllTablesBySchema(conn, schemaName);
					}
				}
			}
		}
	}

	private boolean checkAdditionalSchemaEmpty(final Connection conn, final String schemaName) throws SQLException
	{
		List<String> allTableNames = getOrCreateSchemaContext().getService(IConnectionDialect.class).getAllFullqualifiedTableNames(conn, schemaName);

		for (int i = allTableNames.size(); i-- > 0;)
		{
			String tableName = allTableNames.get(i);
			Statement stmt = null;
			ResultSet rs = null;
			try
			{
				stmt = conn.createStatement();
				stmt.execute("SELECT * FROM " + XmlDatabaseMapper.escapeName(tableName) + " WHERE ROWNUM = 1");
				rs = stmt.getResultSet();
				if (rs.next())
				{
					return false;
				}
			}
			finally
			{
				JdbcUtil.close(stmt, rs);
			}
		}

		return true;
	}

	protected ISchemaRunnable[] getStructureRunnables(final Class<?> callingClass, final Class<?> type)
	{
		List<ISchemaRunnable> schemaRunnables = new ArrayList<ISchemaRunnable>();

		List<IAnnotationInfo<?>> annotations = findAnnotations(type, SQLStructureList.class, SQLStructure.class);
		for (IAnnotationInfo<?> schemaItem : annotations)
		{
			Annotation annotation = schemaItem.getAnnotation();
			if (annotation instanceof SQLStructureList)
			{
				SQLStructureList sqlStructureList = (SQLStructureList) annotation;

				SQLStructure[] value = sqlStructureList.value();
				for (SQLStructure sqlStructure : value)
				{
					getSchemaRunnable(sqlStructure.type(), sqlStructure.value(), schemaRunnables, schemaItem.getAnnotatedElement(), true);
				}
			}
			else
			{
				SQLStructure sqlStructure = (SQLStructure) annotation;
				getSchemaRunnable(sqlStructure.type(), sqlStructure.value(), schemaRunnables, schemaItem.getAnnotatedElement(), true);
			}
		}
		return schemaRunnables.toArray(new ISchemaRunnable[schemaRunnables.size()]);
	}

	protected ISchemaRunnable[] getDataRunnables(final Class<?> callingClass, final Class<?> type, final FrameworkMethod frameworkMethod)
	{
		List<ISchemaRunnable> schemaRunnables = new ArrayList<ISchemaRunnable>();

		List<IAnnotationInfo<?>> annotations = findAnnotations(type, frameworkMethod != null ? frameworkMethod.getMethod() : null, SQLDataList.class,
				SQLData.class);
		for (IAnnotationInfo<?> schemaItem : annotations)
		{
			Annotation annotation = schemaItem.getAnnotation();
			if (annotation instanceof SQLDataList)
			{
				SQLDataList sqlDataList = (SQLDataList) annotation;

				SQLData[] value = sqlDataList.value();
				for (SQLData sqlData : value)
				{
					getSchemaRunnable(sqlData.type(), sqlData.value(), schemaRunnables, schemaItem.getAnnotatedElement(), true);
				}
			}
			else
			{
				SQLData sqlData = (SQLData) annotation;

				getSchemaRunnable(sqlData.type(), sqlData.value(), schemaRunnables, schemaItem.getAnnotatedElement(), true);
			}
		}
		return schemaRunnables.toArray(new ISchemaRunnable[schemaRunnables.size()]);
	}

	protected void getSchemaRunnable(final Class<? extends ISchemaRunnable> schemaRunnableType, final String[] schemaFiles,
			final List<ISchemaRunnable> schemaRunnables, final AnnotatedElement callingClass, final boolean doCommitBehavior)
	{
		if (schemaRunnableType != null && !ISchemaRunnable.class.equals(schemaRunnableType))
		{
			try
			{
				ISchemaRunnable schemaRunnable = schemaRunnableType.newInstance();
				schemaRunnables.add(schemaRunnable);
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		for (String schemaFile : schemaFiles)
		{
			if (schemaFile == null || schemaFile.length() == 0)
			{
				continue;
			}
			final String fSchemaFile = schemaFile;
			ISchemaRunnable schemaRunnable = new ISchemaRunnable()
			{

				@Override
				public void executeSchemaSql(final Connection connection) throws Exception
				{
					List<String> sql = readSqlFile(fSchemaFile, callingClass);
					if (!sql.isEmpty())
					{
						executeScript(sql, connection, false);
					}
				}
			};
			schemaRunnables.add(schemaRunnable);
		}
	}

	private void executeWithDeferredConstraints(final ISchemaRunnable... schemaRunnables)
	{
		if (schemaRunnables.length == 0)
		{
			return;
		}
		try
		{
			Connection conn = getConnection();
			boolean success = false;
			try
			{
				IList<String> enableConstraintsSQL = getOrCreateSchemaContext().getService(IConnectionDialect.class).disableConstraints(conn, getSchemaNames());
				for (ISchemaRunnable schemaRunnable : schemaRunnables)
				{
					schemaRunnable.executeSchemaSql(conn);
				}
				getOrCreateSchemaContext().getService(IConnectionDialect.class).enableConstraints(conn, enableConstraintsSQL);
				conn.commit();
				success = true;
			}
			finally
			{
				if (!success)
				{
					conn.rollback();
				}
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	private List<String> readSqlFile(final String fileName, final AnnotatedElement callingClass) throws IOException
	{
		ILogger log = getLog();

		BufferedReader br = null;
		try
		{
			String lookupName = fileName.startsWith("/") ? fileName.substring(1) : fileName;
			InputStream sqlStream = FileUtil.openFileStream(lookupName, log);
			if (sqlStream != null)
			{
				br = new BufferedReader(new InputStreamReader(sqlStream));
				log = null;
			}
		}
		catch (IllegalArgumentException e)
		{
			// Opening as Stream failed. Try old file code next.
			br = openSqlAsFile(fileName, callingClass, log);
		}

		if (log != null && log.isDebugEnabled())
		{
			if (br != null)
			{
				log.debug("Using sql resource '" + fileName + "'");
			}
		}
		if (br == null)
		{
			throw new FileNotFoundException(fileName);
		}
		StringBuilder sb = new StringBuilder();
		ArrayList<String> sql = new ArrayList<String>();
		try
		{
			String line = null;
			Pattern endToLookFor = null;
			boolean ignoreThisCommand = false;
			IProperties properties = getOrCreateSchemaContext().getService(IProperties.class);
			allLines: while (null != (line = br.readLine()))
			{
				line = line.trim();
				if (line.isEmpty())
				{
					continue allLines;
				}
				for (Pattern comment : sqlComments)
				{
					if (comment.matcher(line).matches())
					{
						continue allLines;
					}
				}
				if (endToLookFor == null)
				{
					for (Pattern ignore : ignoreOutside)
					{
						if (ignore.matcher(line).matches())
						{
							continue allLines;
						}
					}
				}

				if (!optionLine.matcher(line).matches())
				{
					sb.append(line + " ");
				}
				else
				{
					sb.append(line + nl);
				}

				if (endToLookFor == null)
				{
					for (Pattern[] command : sqlCommands)
					{
						if (command[0].matcher(line).matches())
						{
							endToLookFor = command[1];
							break;
						}
					}
				}
				if (endToLookFor == null)
				{
					for (Pattern[] command : sqlIgnoredCommands)
					{
						if (command[0].matcher(line).matches())
						{
							endToLookFor = command[1];
							ignoreThisCommand = true;
							break;
						}
					}
				}
				if (endToLookFor != null)
				{
					for (Pattern part : ignoreIfContains)
					{
						if (part.matcher(line).matches())
						{
							ignoreThisCommand = true;
							break;
						}
					}
				}

				if (endToLookFor != null && endToLookFor.matcher(line).matches())
				{
					if (!ignoreThisCommand)
					{
						Matcher lineEnd = endToLookFor.matcher(line);
						int toCut = 1; // Trailing space
						if (lineEnd.find() && lineEnd.groupCount() == 1)
						{
							toCut += lineEnd.group(1).length();
						}
						sb.setLength(sb.length() - toCut);
						String commandRaw = sb.toString();
						commandRaw = properties.resolvePropertyParts(commandRaw);
						String commandRep = whitespaces.matcher(commandRaw).replaceAll(" ");
						sql.add(commandRep);
					}
					sb.setLength(0);
					endToLookFor = null;
					ignoreThisCommand = false;
				}
			}
		}
		finally
		{
			br.close();
		}

		return sql;
	}

	protected BufferedReader openSqlAsFile(String fileName, AnnotatedElement callingClass, ILogger log) throws IOException, FileNotFoundException
	{
		File sqlFile = null;
		File tempFile = new File(fileName);
		if (tempFile.canRead())
		{
			sqlFile = tempFile;
		}
		if (sqlFile == null)
		{
			String callingNamespace;
			if (callingClass instanceof Class)
			{
				callingNamespace = ((Class<?>) callingClass).getPackage().getName();
			}
			else if (callingClass instanceof Method)
			{
				callingNamespace = ((Method) callingClass).getDeclaringClass().getPackage().getName();
			}
			else if (callingClass instanceof Field)
			{
				callingNamespace = ((Field) callingClass).getDeclaringClass().getPackage().getName();
			}
			else
			{
				throw new IllegalStateException("Value not supported: " + callingClass);
			}
			String relativePath = fileName.startsWith("/") ? "." + fileName : callingNamespace.replace(".", File.separator) + File.separator + fileName;
			String[] classPaths = pathSeparator.split(System.getProperty("java.class.path"));
			for (int i = 0; i < classPaths.length; i++)
			{
				tempFile = new File(classPaths[i], relativePath);
				if (tempFile.canRead())
				{
					sqlFile = tempFile;
					break;
				}
			}
			if (sqlFile == null)
			{
				Pattern fileSuffixPattern = Pattern.compile(".+\\.(?:[^\\.]*)");
				Matcher matcher = fileSuffixPattern.matcher(relativePath);
				if (!matcher.matches())
				{
					relativePath += ".sql";
					for (int i = 0; i < classPaths.length; i++)
					{
						tempFile = new File(classPaths[i], relativePath);
						if (tempFile.canRead())
						{
							sqlFile = tempFile;
							break;
						}
					}
				}
			}
			if (sqlFile == null && !fileName.startsWith("/"))
			{
				// Path is not with root-slash specified. Try to add this before giving up:
				return openSqlAsFile("/" + fileName, callingClass, log);
			}
			if (sqlFile == null)
			{
				if (log.isWarnEnabled())
				{
					String error = "Cannot find '" + relativePath + "' in class path:" + nl;
					Arrays.sort(classPaths);
					for (int i = 0; i < classPaths.length; i++)
					{
						error += "\t" + classPaths[i] + nl;
					}
					log.warn(error);
				}
				return null;
			}
		}

		BufferedReader br = null;
		if (sqlFile != null)
		{
			br = new BufferedReader(new FileReader(sqlFile));
		}

		return br;
	}

	protected void createOptimisticLockingTriggers(final Connection conn) throws SQLException
	{
		IConnectionTestDialect connectionDialect = getOrCreateSchemaContext().getService(IConnectionTestDialect.class);

		List<String> tableNames = connectionDialect.getTablesWithoutOptimisticLockTrigger(conn);

		ArrayList<String> sql = new ArrayList<String>();
		for (String tableName : tableNames)
		{
			sql.addAll(connectionDialect.createOptimisticLockTrigger(conn, tableName));
			sql.addAll(connectionDialect.createAdditionalTriggers(conn, tableName));
		}
		executeScript(sql, conn, false);
	}

	protected void createPermissionGroups(final Connection conn) throws SQLException
	{
		IConnectionTestDialect connectionDialect = getOrCreateSchemaContext().getService(IConnectionTestDialect.class);

		List<String> tableNames = connectionDialect.getTablesWithoutPermissionGroup(conn);

		ArrayList<String> sql = new ArrayList<String>();
		for (String tableName : tableNames)
		{
			sql.addAll(connectionDialect.createPermissionGroup(conn, tableName));
		}
		executeScript(sql, conn, false);
	}

	/**
	 * Delete the content from all tables within the given schema.
	 * 
	 * @param conn
	 *            SQL connection
	 * @param schemaNames
	 *            Schema names to use
	 * @throws SQLException
	 */
	protected void truncateAllTablesBySchema(final Connection conn, final String... schemaNames) throws SQLException
	{
		List<String> allTableNames = getOrCreateSchemaContext().getService(IConnectionDialect.class).getAllFullqualifiedTableNames(conn, schemaNames);
		if (allTableNames.isEmpty())
		{
			return;
		}
		final List<String> sql = new ArrayList<String>();

		for (int i = allTableNames.size(); i-- > 0;)
		{
			String tableName = allTableNames.get(i);
			sql.add("DELETE FROM " + XmlDatabaseMapper.escapeName(tableName) + " CASCADE");
		}
		executeWithDeferredConstraints(new ISchemaRunnable()
		{

			@Override
			public void executeSchemaSql(final Connection connection) throws Exception
			{
				executeScript(sql, connection, false);
				sql.clear();
			}
		});
	}

	/**
	 * Delete the content from the given tables.
	 * 
	 * @param conn
	 *            SQL connection
	 * @param explicitTableNames
	 *            Table name with schema (or synonym)
	 * @throws SQLException
	 */
	protected void truncateAllTablesExplicitlyGiven(final Connection conn, final String[] explicitTableNames) throws SQLException
	{
		if (explicitTableNames == null || explicitTableNames.length == 0)
		{
			return;
		}
		final List<String> sql = new ArrayList<String>();
		for (int i = explicitTableNames.length; i-- > 0;)
		{
			String tableName = explicitTableNames[i];
			sql.add("DELETE FROM " + XmlDatabaseMapper.escapeName(tableName));
		}

		executeWithDeferredConstraints(new ISchemaRunnable()
		{

			@Override
			public void executeSchemaSql(final Connection connection) throws Exception
			{
				executeScript(sql, connection, false);
				sql.clear();
			}
		});
	}

	void executeScript(final List<String> sql, final Connection conn) throws SQLException
	{
		executeScript(sql, conn, true);
	}

	void executeScript(final List<String> sql, final Connection conn, final boolean doCommitBehavior) throws SQLException
	{
		if (sql.size() == 0)
		{
			return;
		}
		Statement stmt = null;
		// Must be a linked map to maintain sequential order while iterating
		IMap<String, List<Throwable>> commandToExceptionMap = new de.osthus.ambeth.collections.LinkedHashMap<String, List<Throwable>>();
		Map<String, Object> defaultOptions = new HashMap<String, Object>();
		defaultOptions.put("loop", 1);
		try
		{
			stmt = conn.createStatement();
			List<String> done = new ArrayList<String>();
			do
			{
				done.clear();
				for (String command : sql)
				{
					if (command == null || command.length() == 0)
					{
						done.add(command);
						continue;
					}
					try
					{
						handleSqlCommand(command, stmt, defaultOptions);
						done.add(command);
						// If the command was successful, remove the key from the exception log
						commandToExceptionMap.remove(command);
					}
					catch (PersistenceException e)
					{
						// When executing multiple sql files some statements collide and cannot all be executed
						if (!doExecuteStrict && canFailBeTolerated(command))
						{
							ILogger log = getLog();

							if (log.isWarnEnabled())
							{
								log.warn("SQL statement failed: '" + command + "'");
							}
							done.add(command);
							commandToExceptionMap.remove(command);
							continue;
						}
						// Store only first exception per command (the exception itself can change for the same command
						// depending on the state of the schema - but we want the FIRST exception)
						List<Throwable> exceptionsOfCommand = commandToExceptionMap.get(command);
						if (exceptionsOfCommand == null)
						{
							exceptionsOfCommand = new ArrayList<Throwable>();
							commandToExceptionMap.put(command, exceptionsOfCommand);
						}
						exceptionsOfCommand.add(e);
					}
				}
				sql.removeAll(done);
			}
			while (sql.size() > 0 && done.size() > 0);

			if (doCommitBehavior)
			{
				if (sql.isEmpty())
				{
					conn.commit();
				}
				else
				{
					conn.rollback();
					if (commandToExceptionMap.size() > 1)
					{
						for (List<Throwable> exceptionsOfCommand : commandToExceptionMap.values())
						{
							for (Throwable e : exceptionsOfCommand)
							{
								e.printStackTrace();
							}
						}
					}
					else if (commandToExceptionMap.size() == 1)
					{
						PersistenceException pe = new PersistenceException("Uncorrectable SQL exception(s)", commandToExceptionMap.values().get(0).get(0));
						pe.setStackTrace(new StackTraceElement[0]);
						throw pe;
					}
					else
					{
						throw new PersistenceException("Uncorrectable SQL exception(s)");
					}
				}
			}
			else if (!sql.isEmpty())
			{
				if (commandToExceptionMap.size() > 0)
				{
					String errorMessage = "Uncorrectable SQL exception(s)";
					if (commandToExceptionMap.size() > 1)
					{
						errorMessage += ". There are " + commandToExceptionMap.size() + " exceptions! The first one is:";
					}
					PersistenceException pe = new PersistenceException(errorMessage, commandToExceptionMap.values().get(0).get(0));
					pe.setStackTrace(new StackTraceElement[0]);
					throw pe;
				}
			}
		}
		finally
		{
			JdbcUtil.close(stmt);
		}
	}

	private boolean canFailBeTolerated(final String command)
	{
		return sqlTryOnlyCommands[0][0].matcher(command).matches();
	}

	private void handleSqlCommand(String command, final Statement stmt, final Map<String, Object> defaultOptions) throws SQLException
	{
		Map<String, Object> options = defaultOptions;
		Matcher optionLine = this.optionLine.matcher(command.trim());
		if (optionLine.find())
		{
			options = new HashMap<String, Object>(defaultOptions);
			String optionString = optionLine.group(1).replace(" ", "");
			String[] preSqls = optionSeparator.split(optionString);
			command = lineSeparator.split(command, 2)[1];
			Object value;
			for (int i = preSqls.length; i-- > 0;)
			{
				Matcher keyValue = optionPattern.matcher(preSqls[i]);
				value = null;
				if (keyValue.find())
				{
					if ("loop".equals(keyValue.group(1)))
					{
						value = Integer.parseInt(keyValue.group(2));
					}
					options.put(keyValue.group(1), value);
				}
			}
		}
		command = getOrCreateSchemaContext().getService(IConnectionDialect.class).prepareCommand(command);
		int loopCount = ((Integer) options.get("loop")).intValue();
		if (loopCount == 1)
		{
			stmt.execute(command);
		}
		else
		{
			for (int i = loopCount; i-- > 0;)
			{
				stmt.addBatch(command);
			}
			stmt.executeBatch();
		}
	}

}
