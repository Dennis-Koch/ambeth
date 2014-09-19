package de.osthus.ambeth.testutil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.PersistenceException;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import de.osthus.ambeth.annotation.IAnnotationInfo;
import de.osthus.ambeth.cache.ClearAllCachesEvent;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.database.DatabaseCallback;
import de.osthus.ambeth.database.ITransaction;
import de.osthus.ambeth.event.IEventDispatcher;
import de.osthus.ambeth.exception.MaskingRuntimeException;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.io.FileUtil;
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.factory.BeanContextFactory;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.ILoggerCache;
import de.osthus.ambeth.log.LoggerFactory;
import de.osthus.ambeth.model.ISecurityScope;
import de.osthus.ambeth.orm.XmlDatabaseMapper;
import de.osthus.ambeth.persistence.IConnectionDialect;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.jdbc.IConnectionFactory;
import de.osthus.ambeth.persistence.jdbc.IConnectionTestDialect;
import de.osthus.ambeth.persistence.jdbc.JdbcUtil;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import de.osthus.ambeth.persistence.jdbc.connection.LogPreparedStatementInterceptor;
import de.osthus.ambeth.persistence.jdbc.connection.LogStatementInterceptor;
import de.osthus.ambeth.proxy.IMethodLevelBehavior;
import de.osthus.ambeth.proxy.IProxyFactory;
import de.osthus.ambeth.security.DefaultAuthentication;
import de.osthus.ambeth.security.IAuthentication.PasswordType;
import de.osthus.ambeth.security.ISecurityContextHolder;
import de.osthus.ambeth.security.ISecurityScopeProvider;
import de.osthus.ambeth.security.SecurityContext.SecurityContextType;
import de.osthus.ambeth.security.SecurityFilterInterceptor;
import de.osthus.ambeth.security.TestAuthentication;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.ambeth.xml.DefaultXmlWriter;
import de.osthus.ambeth.xml.simple.AppendableStringBuilder;

/**
 * TODO: Handle test methods which change the structure
 */
public class AmbethPersistenceRunner extends AmbethIocRunner
{
	protected static final String MEASUREMENT_BEAN = "measurementBean";

	private Connection connection;

	private IServiceContext schemaContext;

	@Autowired
	protected ISecurityContextHolder securityContextHolder;

	protected boolean doExecuteStrict = false;

	/** Flag which is set to true after the structure was build. */
	private boolean isStructureRebuildAlreadyHandled = false;

	/** Flag which is set to true after the first test method was executed. */
	private boolean isFirstTestMethodAlreadyExecuted;

	/** Flag which is set if the last test method has triggered a context rebuild. */
	private boolean lastMethodTriggersContextRebuild;

	public AmbethPersistenceRunner(final Class<?> testClass) throws InitializationError
	{
		super(testClass);
	}

	public void setDoExecuteStrict(final boolean doExecuteStrict)
	{
		this.doExecuteStrict = doExecuteStrict;
	}

	public void rebuildData()
	{
		rebuildData(null);
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
	}

	@Override
	protected void extendProperties(FrameworkMethod frameworkMethod, Properties props)
	{
		super.extendProperties(frameworkMethod, props);

		DialectSelectorModule.fillTestProperties(props);
	}

	@Override
	protected List<Class<? extends IInitializingModule>> buildFrameworkTestModuleList(FrameworkMethod frameworkMethod)
	{
		List<Class<? extends IInitializingModule>> frameworkTestModuleList = super.buildFrameworkTestModuleList(frameworkMethod);
		frameworkTestModuleList.add(DialectSelectorModule.class);
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
		Properties.loadBootstrapPropertyFile();

		Properties baseProps = new Properties(Properties.getApplication());
		baseProps.putString(LoggerFactory.logLevelProperty(LogPreparedStatementInterceptor.class), "INFO");
		baseProps.putString(LoggerFactory.logLevelProperty(LogStatementInterceptor.class), "INFO");

		extendProperties(null, baseProps);

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
	protected void rebuildContext(final FrameworkMethod frameworkMethod)
	{
		if (frameworkMethod == null)
		{
			// This is the case if the runner is the parent runner or if rebuildContext is called explicitly
			// (AmbethIocRunner.rebuildContext(null)).
			// Because the parent runner shouldn't create a context unit it is needed by the tests (e.g. to allow
			// "beforeClass" tasks without a context) we exit
			// here.
			// TODO: This is a problem for AGRILOG integration tests where the test runner is abused as a context
			// creator. Maybe we need the possibility to
			// configure this behavior.
			return;
		}
		super.rebuildContext(frameworkMethod);
		try
		{
			if (connection != null)
			{
				connection.close();
				connection = null;
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		beanContext.getService(ITransaction.class).processAndCommit(new DatabaseCallback()
		{

			@Override
			public void callback(final ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap)
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
	protected org.junit.runners.model.Statement withBeforeClasses(org.junit.runners.model.Statement statement)
	{
		checkOS();
		return super.withBeforeClasses(statement);
	}

	@Override
	protected org.junit.runners.model.Statement withAfterClassesWithinContext(final org.junit.runners.model.Statement statement)
	{
		final org.junit.runners.model.Statement resultStatement = super.withAfterClassesWithinContext(statement);
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
						String[] schemaNames = getSchemaNames();
						truncateMainSchema(conn, schemaNames[0]);
						truncateAdditionalSchemas(conn, schemaNames, true);
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
			getOrCreateSchemaContext().getService(IConnectionTestDialect.class).preStructureRebuild(getConnection());
			Connection connection = getConnection();
			ensureSchemaEmpty(connection);

			ISchemaRunnable[] structureRunnables = getStructureRunnables(callingClass, callingClass);
			for (ISchemaRunnable structRunnable : structureRunnables)
			{
				structRunnable.executeSchemaSql(connection);
			}

			ensureExistanceOfNeededDatabaseObjects(connection);

			isStructureRebuildAlreadyHandled = true;
		}
		catch (Exception e)
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

	@Override
	protected void runChildWithContext(final FrameworkMethod frameworkMethod, final RunNotifier notifier, final boolean hasContextBeenRebuild)
	{
		boolean doContextRebuild = false;
		Method method = frameworkMethod.getMethod();
		try
		{
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
			if (doContextRebuild)
			{
				rebuildContext(frameworkMethod);
			}

			// Trigger clearing of other maps and caches (QueryResultCache,...)
			beanContext.getService(IEventDispatcher.class).dispatchEvent(ClearAllCachesEvent.getInstance());

			isFirstTestMethodAlreadyExecuted = true;
		}
		catch (MaskingRuntimeException e)
		{
			notifier.fireTestFailure(new Failure(Description.createTestDescription(getTestClass().getJavaClass(), method.getName()), e.getMessage() == null ? e
					.getCause() : e));
			return;
		}
		catch (Throwable e)
		{
			notifier.fireTestFailure(new Failure(Description.createTestDescription(getTestClass().getJavaClass(), method.getName()), e));
			return;
		}
		super.runChildWithContext(frameworkMethod, notifier, doContextRebuild);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected org.junit.runners.model.Statement withBefores(final FrameworkMethod method, Object target, final org.junit.runners.model.Statement statement)
	{
		return AmbethPersistenceRunner.super.withBefores(method, target, new org.junit.runners.model.Statement()
		{
			@Override
			public void evaluate() throws Throwable
			{
				TestAuthentication authentication = method.getAnnotation(TestAuthentication.class);
				if (authentication == null)
				{
					statement.evaluate();
					return;
				}
				final String scopeName = authentication.scope();
				final ISecurityScope scope = new ISecurityScope()
				{
					@Override
					public String getName()
					{
						return scopeName;
					}
				};
				IMethodLevelBehavior<SecurityContextType> behaviour = new IMethodLevelBehavior<SecurityContextType>()
				{
					@Override
					public SecurityContextType getBehaviourOfMethod(Method method)
					{
						return SecurityContextType.AUTHENTICATED;
					}

					@Override
					public SecurityContextType getDefaultBehaviour()
					{
						return SecurityContextType.AUTHENTICATED;
					}
				};

				SecurityFilterInterceptor interceptor = beanContext.registerAnonymousBean(SecurityFilterInterceptor.class)
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
						return beanContext.getService(ISecurityScopeProvider.class).executeWithSecurityScopes(new IResultingBackgroundWorkerDelegate<Object>()
						{
							@Override
							public Object invoke() throws Throwable
							{
								fStatement.evaluate();
								return null;
							}
						}, scope);
					}
				});
			}
		});
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
			{ Pattern.compile("CREATE( +OR +REPLACE)? +(?:TABLE|VIEW|INDEX|TYPE|SEQUENCE|SYNONYM) +.+", Pattern.CASE_INSENSITIVE),
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

	protected ILogger getLog()
	{
		return schemaContext.getService(ILoggerCache.class).getCachedLogger(schemaContext, AmbethPersistenceRunner.class);
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
			if (!getOrCreateSchemaContext().getService(IConnectionTestDialect.class).createTestUserIfSupported(cause,
					testProps.getString(PersistenceJdbcConfigurationConstants.DatabaseUser),
					testProps.getString(PersistenceJdbcConfigurationConstants.DatabasePass), testProps))
			{
				throw e;
			}
			try
			{
				conn = getOrCreateSchemaContext().getService(IConnectionFactory.class).create();
			}
			catch (Throwable t)
			{
				// throw the initial exception if this fails somehow
				throw e;
			}
		}
		conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		conn.setAutoCommit(false);
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
		IProperties properties = getOrCreateSchemaContext().getService(IProperties.class);
		String schemaProperty = (String) properties.get(PersistenceJdbcConfigurationConstants.DatabaseSchemaName);
		String[] schemaNames = schemaProperty.toUpperCase().split("[:;]");
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

	protected void getSchemaRunnable(final Class<? extends ISchemaRunnable> schemaRunnableType, final String schemaFile,
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
		if (schemaFile != null && schemaFile.length() > 0)
		{
			ISchemaRunnable schemaRunnable = new ISchemaRunnable()
			{

				@Override
				public void executeSchemaSql(final Connection connection) throws Exception
				{
					List<String> sql = readSqlFile(schemaFile, callingClass);
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
				IList<String[]> disabled = getOrCreateSchemaContext().getService(IConnectionDialect.class).disableConstraints(conn, getSchemaNames());
				for (ISchemaRunnable schemaRunnable : schemaRunnables)
				{
					schemaRunnable.executeSchemaSql(conn);
				}
				getOrCreateSchemaContext().getService(IConnectionDialect.class).enableConstraints(conn, disabled);
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
			InputStream sqlStream = FileUtil.openFileStream(fileName);
			if (sqlStream != null)
			{
				br = new BufferedReader(new InputStreamReader(sqlStream));
			}
		}
		catch (IllegalArgumentException e)
		{
			// Opening as Stream failed. Try old file code next.
			br = openSqlAsFile(fileName, callingClass, log);
		}

		if (log.isDebugEnabled())
		{
			if (br != null)
			{
				log.debug("Using sql resource '" + fileName + "'");
			}
			else
			{
				log.debug("Cannot find sql resource '" + fileName + "'");
			}
		}

		StringBuilder sb = new StringBuilder();
		List<String> sql = new ArrayList<String>();
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

	private void createOptimisticLockingTriggers(final Connection conn) throws SQLException
	{
		IConnectionTestDialect connectionDialect = getOrCreateSchemaContext().getService(IConnectionTestDialect.class);

		List<String> tableNames = connectionDialect.getTablesWithoutOptimisticLockTrigger(conn);

		List<String> sql = new ArrayList<String>();
		for (String tableName : tableNames)
		{
			sql.add(connectionDialect.createOptimisticLockTrigger(conn, tableName));
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
		command = getOrCreateSchemaContext().getService(IConnectionTestDialect.class).prepareCommand(command);
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
