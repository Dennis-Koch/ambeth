package de.osthus.ambeth.testutil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
import de.osthus.ambeth.collections.HashSet;
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
import de.osthus.ambeth.ioc.IInitializingModule;
import de.osthus.ambeth.ioc.IPropertyLoadingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.IocBootstrapModule;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.factory.BeanContextFactory;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LoggerFactory;
import de.osthus.ambeth.oracle.Oracle10gThinDialect;
import de.osthus.ambeth.persistence.IConnectionDialect;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.jdbc.IConnectionFactory;
import de.osthus.ambeth.persistence.jdbc.JdbcUtil;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import de.osthus.ambeth.persistence.jdbc.connection.ConnectionFactory;
import de.osthus.ambeth.testutil.RandomUserScript.RandomUserModule;
import de.osthus.ambeth.util.IPersistenceExceptionUtil;
import de.osthus.ambeth.util.PersistenceExceptionUtil;
import de.osthus.ambeth.xml.DefaultXmlWriter;
import de.osthus.ambeth.xml.simple.AppendableStringBuilder;

/**
 * Please use <code>de.osthus.ambeth.testutil.NewAmbethPersistenceRunner</code> instead
 */
@Deprecated
public class AmbethPersistenceRunner extends AmbethIocRunner
{
	protected static final String MEASUREMENT_BEAN = "measurementBean";

	@FrameworkModule
	public static class AmbethPersistenceSchemaModule implements IInitializingModule, IPropertyLoadingBean
	{
		@Override
		public void applyProperties(Properties contextProperties)
		{
			String databaseConnection = contextProperties.getString(PersistenceJdbcConfigurationConstants.DatabaseConnection);
			if (databaseConnection == null)
			{
				contextProperties.put(PersistenceJdbcConfigurationConstants.DatabaseConnection, "${" + PersistenceJdbcConfigurationConstants.DatabaseProtocol
						+ "}:@" + "${" + PersistenceJdbcConfigurationConstants.DatabaseHost + "}" + ":" + "${"
						+ PersistenceJdbcConfigurationConstants.DatabasePort + "}" + ":" + "${" + PersistenceJdbcConfigurationConstants.DatabaseName + "}");
			}
		}

		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			beanContextFactory.registerAnonymousBean(IocBootstrapModule.class);
			beanContextFactory.registerBean("connectionDialect", Oracle10gThinDialect.class).autowireable(IConnectionDialect.class);
			beanContextFactory.registerBean("connectionFactory", ConnectionFactory.class).autowireable(IConnectionFactory.class);
			beanContextFactory.registerBean("persistenceExceptionUtil", PersistenceExceptionUtil.class).autowireable(IPersistenceExceptionUtil.class);
		}
	}

	protected Connection connection;

	protected IServiceContext schemaContext;

	protected boolean isClassLevelDataValid, isClassLevelStructureValid, lastTestContextRebuild;

	protected boolean doCleanSchema = true;

	protected boolean doExecuteStrict = false;

	public AmbethPersistenceRunner(Class<?> testClass) throws InitializationError
	{
		super(testClass);
		if (AbstractJDBCTest.class.isAssignableFrom(testClass))
		{
			throw new IllegalArgumentException("This runner does not support tests which inherit from " + AbstractJDBCTest.class.getName());
		}
	}

	public void setDoCleanSchema(boolean doCleanSchema)
	{
		this.doCleanSchema = doCleanSchema;
	}

	public void setDoExecuteStrict(boolean doExecuteStrict)
	{
		this.doExecuteStrict = doExecuteStrict;
	}

	public void rebuildStructure()
	{
		rebuildStructure(null);
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

	protected void rebuildSchemaContext()
	{
		if (schemaContext != null)
		{
			schemaContext.getRoot().dispose();
			schemaContext = null;
		}
		Properties.resetApplication();
		Properties.loadBootstrapPropertyFile();

		Properties baseProps = new Properties(Properties.getApplication());

		// Definition was moved from properties file to PersistenceJdbcModule. But that is read to late for test setup.
		baseProps.put("database.connection", "${database.protocol}:@${database.host}:${database.port}:${database.name}");

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
	protected void rebuildContext(FrameworkMethod frameworkMethod)
	{
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
			public void callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap)
			{
				// Intended blank
			}
		});
	}

	@Override
	protected void rebuildContextDetails(IBeanContextFactory childContextFactory)
	{
		super.rebuildContextDetails(childContextFactory);

		childContextFactory.registerBean(MEASUREMENT_BEAN, Measurement.class).propertyValue("TestClassName", getTestClass().getJavaClass())
				.autowireable(IMeasurement.class);
	}

	@Override
	protected org.junit.runners.model.Statement withBeforeClasses(org.junit.runners.model.Statement statement)
	{
		checkOS();
		// riverManager.registerDriver((Driver)
		// Thread.currentThread().getContextClassLoader().loadClass("oracle.jdbc.OracleDriver").newInstance());
		// TODO: allow RandomUsertScript to create a database user
		// beforeClass();
		rebuildStructure(null);

		final org.junit.runners.model.Statement parentStatement = super.withBeforeClasses(statement);
		return new org.junit.runners.model.Statement()
		{

			@Override
			public void evaluate() throws Throwable
			{
				try
				{
					beanContext.getService(ITransaction.class).processAndCommit(new DatabaseCallback()
					{

						@Override
						public void callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap)
						{
							// Intended blank
						}
					});
				}
				catch (Exception e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}
				parentStatement.evaluate();
			}
		};
	}

	@Override
	protected org.junit.runners.model.Statement withAfterClassesWithinContext(org.junit.runners.model.Statement statement)
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
					Class<?> callingClass = getTestClass().getJavaClass();

					try
					{
						if (running.contains(callingClass))
						{
							String[] schemaNames = getSchemaNames();
							for (int i = schemaNames.length; i-- > 1;)
							{
								String[] configuredSynonymNames = getConfiguredSynonymNames(callingClass);
								String schemaName = schemaNames[i];
								truncateAllTables(getConnection(), configuredSynonymNames, schemaName);
							}
							tearDownAllSQLContents(getConnection(), schemaNames[0]);
							// TODO: allow RandomUsertScript to drop the created database user
							// afterClass();
						}
					}
					finally
					{
						running.remove(callingClass);
						JdbcUtil.close(connection);
						connection = null;
						if (schemaContext != null)
						{
							schemaContext.dispose();
							schemaContext = null;
						}

						// Enumeration<Driver> drivers = DriverManager.getDrivers();
						// while (drivers.hasMoreElements())
						// {
						// Driver driver = drivers.nextElement();
						// try
						// {
						// DriverManager.deregisterDriver(driver);
						// }
						// catch (SQLException e)
						// {
						// if (log.isErrorEnabled())
						// {
						// log.error("Error deregistering driver " + driver, e);
						// }
						// }
						// }
					}
				}
				catch (Exception e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}
			}
		};
	}

	protected void rebuildStructure(FrameworkMethod frameworkMethod)
	{
		if (!doCleanSchema)
		{
			return;
		}

		Class<?> callingClass = getTestClass().getJavaClass();
		try
		{
			running.add(callingClass);

			Connection connection = getConnection();

			HashSet<Annotation> alreadyProcessedAnnotations = new HashSet<Annotation>();

			if (!isClassLevelStructureValid)
			{
				checkSchemaEmpty(connection);

				ISchemaRunnable[] structureRunnables = getStructureRunnables(callingClass, callingClass, null, alreadyProcessedAnnotations);
				for (ISchemaRunnable structRunnable : structureRunnables)
				{
					structRunnable.executeSchemaSql(connection);
				}
				isClassLevelStructureValid = true;
			}
			ISchemaRunnable[] structureRunnables = getStructureRunnables(callingClass, null, frameworkMethod, alreadyProcessedAnnotations);
			if (structureRunnables.length > 0)
			{
				for (ISchemaRunnable structRunnable : structureRunnables)
				{
					structRunnable.executeSchemaSql(connection);
				}
				isClassLevelStructureValid = false;
			}
			checkAfterStructure(connection);
		}
		catch (Exception e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected void rebuildData(FrameworkMethod frameworkMethod)
	{
		if (!doCleanSchema)
		{
			return;
		}

		Class<?> callingClass = getTestClass().getJavaClass();
		try
		{
			String[] schemaNames = getSchemaNames();
			String[] configuredSynonymNames = getConfiguredSynonymNames(callingClass);
			truncateAllTables(getConnection(), configuredSynonymNames, schemaNames);
			ISchemaRunnable[] dataRunnables = getDataRunnables(callingClass, callingClass, frameworkMethod);
			executeWithDeferredConstraints(dataRunnables);
		}
		catch (Exception e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		isClassLevelDataValid = true;
	}

	@Override
	protected void runChildWithContext(FrameworkMethod frameworkMethod, RunNotifier notifier, boolean hasContextBeenRebuild)
	{
		boolean doContextRebuild = false;
		Method method = frameworkMethod.getMethod();
		try
		{
			boolean doStructureRebuild = method.isAnnotationPresent(SQLStructure.class) || method.isAnnotationPresent(SQLStructureList.class);
			doContextRebuild = lastTestContextRebuild || doStructureRebuild || method.isAnnotationPresent(TestModule.class)
					|| method.isAnnotationPresent(TestProperties.class) || method.isAnnotationPresent(TestPropertiesList.class);
			boolean sqlDataPresent = method.isAnnotationPresent(SQLData.class) || method.isAnnotationPresent(SQLDataList.class);
			boolean doDataRebuild = true;
			lastTestContextRebuild = false;

			SQLDataRebuild dataRebuild = frameworkMethod.getAnnotation(SQLDataRebuild.class);
			if (dataRebuild != null)
			{
				doDataRebuild = dataRebuild.value();
				if (sqlDataPresent)
				{
					throw new IllegalStateException("It is not valid to annotate the same method '" + method.toString() + "' with both "
							+ SQLDataRebuild.class.getSimpleName() + "=false and " + SQLData.class.getSimpleName() + " or " + SQLDataList.class.getSimpleName());
				}
			}
			else if (!sqlDataPresent)
			{
				List<IAnnotationInfo<?>> sqlDataRebuilds = findAnnotations(getTestClass().getJavaClass(), SQLDataRebuild.class);
				if (sqlDataRebuilds.size() > 0)
				{
					// No data specified at method level and rebuild=true/false specified on class level. so do no
					// rebuild here of false
					IAnnotationInfo<?> topDataRebuild = sqlDataRebuilds.get(sqlDataRebuilds.size() - 1);
					doDataRebuild = ((SQLDataRebuild) topDataRebuild.getAnnotation()).value();
				}
			}
			if (doStructureRebuild)
			{
				rebuildStructure(frameworkMethod);
			}
			if (doContextRebuild)
			{
				rebuildContext(frameworkMethod);
				lastTestContextRebuild = true;
				if (!doDataRebuild)
				{
					beanContext.getService(ITransaction.class).processAndCommit(new DatabaseCallback()
					{

						@Override
						public void callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap)
						{
							// Intended blank
						}
					});
				}
			}
			if (doDataRebuild || !isClassLevelDataValid)
			{
				rebuildData(frameworkMethod);
			}

			// Trigger clearing of other maps and caches (QueryResultCache,...)
			beanContext.getService(IEventDispatcher.class).dispatchEvent(ClearAllCachesEvent.getInstance());
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

	private final String nl = System.getProperty("line.separator");

	private final Pattern binTableName = Pattern.compile("(?:BIN\\$.{22}==\\$0)|(?:DR\\$[^\\$]+\\$\\w)", Pattern.CASE_INSENSITIVE);

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

	private final Set<Class<?>> running = new HashSet<Class<?>>();

	protected final StringBuilder measurementXML = new StringBuilder();

	protected final DefaultXmlWriter xmlWriter = new DefaultXmlWriter(new AppendableStringBuilder(measurementXML), null);

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

	private void checkAfterStructure(Connection conn) throws SQLException
	{
		List<String> required = new ArrayList<String>(Arrays.asList("TSEQ", "TSEQSET", "GETROWS"));
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			stmt = conn.createStatement();
			stmt.execute("SELECT object_name FROM user_objects WHERE object_type IN ('FUNCTION', 'PROCEDURE', 'TYPE')");
			rs = stmt.getResultSet();

			while (rs.next())
			{
				required.remove(rs.getString("object_name"));
			}
			JdbcUtil.close(stmt, rs);

			List<String> sql = new ArrayList<String>();
			StringBuilder command = new StringBuilder();
			for (int i = 0; i < required.size(); i++)
			{
				String missing = required.get(i);
				if ("TSEQ".equalsIgnoreCase(missing))
				{
					command.append("Create TYPE TSeq AS OBJECT ( rowIndex Number(12) )");
				}
				else if ("TSEQSET".equalsIgnoreCase(missing))
				{
					command.append("Create TYPE TSeqSet AS TABLE OF TSeq");
				}
				else if ("GETROWS".equalsIgnoreCase(missing))
				{
					command.append("CREATE or replace FUNCTION getRows(ARowCount Number) RETURN TSeqSet ");
					command.append("PIPELINED IS ");
					command.append("out_rec TSeq := TSeq(null);");
					command.append("i Number(12);");
					command.append("BEGIN ");
					command.append("i := ARowCount; ");
					command.append("while (i > 0) loop ");
					command.append("out_rec.rowIndex := i;");
					command.append("PIPE ROW(out_rec);");
					command.append("i := i - 1;");
					command.append("end loop;");
					command.append("return;");
					command.append("END;");
				}
				else
				{
					throw new IllegalArgumentException("Unhandled missing database object: " + missing);
				}
				if (command.length() > 0)
				{
					sql.add(command.toString());
					command.setLength(0);
				}
			}
			executeScript(sql, conn, false);
			sql.clear();
		}
		finally
		{
			JdbcUtil.close(stmt, rs);
		}
		createOptimisticLockingTriggers(conn);
		schemaContext.getService(IConnectionDialect.class).preProcessConnection(conn, getSchemaNames(), true);
	}

	protected void logMeasurement(String name, Object value)
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
		try
		{
			Thread.currentThread().getContextClassLoader().loadClass("oracle.jdbc.OracleDriver");
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		if (schemaContext == null || schemaContext.isDisposed())
		{
			rebuildSchemaContext();
		}
		Connection conn;
		try
		{
			conn = schemaContext.getService(IConnectionFactory.class).create();
		}
		catch (MaskingRuntimeException e)
		{
			if (!(e.getCause() instanceof SQLException))
			{
				throw e;
			}
			SQLException ex = (SQLException) e.getCause();
			if (ex.getErrorCode() != 1017) // ORA-01017: invalid username/password; logon denied
			{
				throw e;
			}
			// try to recover by trying to create the necessary user with the default credentials of sys
			try
			{
				IProperties testProps = schemaContext.getService(IProperties.class);
				Properties createUserProps = new Properties(testProps);
				createUserProps.put(RandomUserScript.SCRIPT_IS_CREATE, "true");
				createUserProps.put(RandomUserScript.SCRIPT_USER_NAME, testProps.getString(PersistenceJdbcConfigurationConstants.DatabaseUser));
				createUserProps.put(RandomUserScript.SCRIPT_USER_PASS, testProps.getString(PersistenceJdbcConfigurationConstants.DatabasePass));
				createUserProps.put(PersistenceJdbcConfigurationConstants.DatabaseUser, "sys as sysdba");
				createUserProps.put(PersistenceJdbcConfigurationConstants.DatabasePass, "developer");
				IServiceContext bootstrapContext = BeanContextFactory.createBootstrap(createUserProps);
				try
				{
					bootstrapContext.createService("randomUser", RandomUserModule.class, IocBootstrapModule.class);
				}
				finally
				{
					bootstrapContext.dispose();
				}
				conn = schemaContext.getService(IConnectionFactory.class).create();
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

	private String[] getSchemaNames()
	{
		IProperties properties = schemaContext.getService(IProperties.class);
		String schemaProperty = (String) properties.get(PersistenceJdbcConfigurationConstants.DatabaseSchemaName);
		String[] schemaNames = schemaProperty.toUpperCase().split("[:;]");
		return schemaNames;
	}

	private String[] getConfiguredSynonymNames(Class<?> type)
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

	private void checkSchemaEmpty(Connection conn) throws SQLException
	{
		boolean preserveContents = false;

		String[] schemaNames = getSchemaNames();

		if (!checkMainSchemaEmpty(conn))
		{
			if (preserveContents)
			{
				throw new IllegalStateException("Main schema is not empty!");
			}
			else
			{
				tearDownAllSQLContents(conn, schemaNames[0]);
			}
		}

		for (int i = schemaNames.length; i-- > 1;)
		{
			String schemaName = schemaNames[i];
			if (!checkAdditionalSchemaEmpty(conn, schemaName))
			{
				if (preserveContents)
				{
					throw new IllegalStateException("Schema '" + schemaName + "' is not empty!");
				}
				else
				{
					Class<?> callingClass = getTestClass().getJavaClass();
					String[] configuredSynonymNames = getConfiguredSynonymNames(callingClass);
					truncateAllTables(conn, configuredSynonymNames, schemaName);
				}
			}
		}
	}

	private boolean checkMainSchemaEmpty(Connection conn) throws SQLException
	{
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT tname FROM tab");
			while (rs.next())
			{
				if (!binTableName.matcher(rs.getString("tname")).matches())
				{
					return false;
				}
			}
			JdbcUtil.close(rs);
			rs = stmt
					.executeQuery("SELECT object_type, object_name FROM user_objects WHERE object_type IN ('FUNCTION', 'INDEX', 'PACKAGE', 'PACKAGE BODY', 'PROCEDURE', 'SEQUENCE', 'TABLE', 'TYPE', 'VIEW')");
			return !rs.next();
		}
		finally
		{
			JdbcUtil.close(stmt, rs);
		}
	}

	private boolean checkAdditionalSchemaEmpty(Connection conn, String schemaName) throws SQLException
	{
		List<String> allTableNames = getAllTableNames(conn, schemaName);

		for (int i = allTableNames.size(); i-- > 0;)
		{
			String tableName = allTableNames.get(i);
			Statement stmt = null;
			ResultSet rs = null;
			try
			{
				stmt = conn.createStatement();
				stmt.execute("SELECT * FROM " + tableName + " WHERE ROWNUM = 1");
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

	protected ISchemaRunnable[] getStructureRunnables(Class<?> callingClass, Class<?> type, FrameworkMethod frameworkMethod,
			Set<Annotation> alreadyProcessedAnnotations)
	{
		List<ISchemaRunnable> schemaRunnables = new ArrayList<ISchemaRunnable>();

		List<IAnnotationInfo<?>> annotations = findAnnotations(type, frameworkMethod != null ? frameworkMethod.getMethod() : null, SQLStructureList.class,
				SQLStructure.class);
		for (IAnnotationInfo<?> schemaItem : annotations)
		{
			Annotation annotation = schemaItem.getAnnotation();
			if (alreadyProcessedAnnotations != null && !alreadyProcessedAnnotations.add(annotation))
			{
				continue;
			}
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

	protected ISchemaRunnable[] getDataRunnables(Class<?> callingClass, Class<?> type, FrameworkMethod frameworkMethod)
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

	protected void getSchemaRunnable(Class<? extends ISchemaRunnable> schemaRunnableType, final String schemaFile, List<ISchemaRunnable> schemaRunnables,
			final AnnotatedElement callingClass, boolean doCommitBehavior)
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
				public void executeSchemaSql(Connection connection) throws Exception
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

	private void executeWithDeferredConstraints(ISchemaRunnable... schemaRunnables)
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
				IList<String[]> disabled = disableConstraints(conn);
				for (ISchemaRunnable schemaRunnable : schemaRunnables)
				{
					schemaRunnable.executeSchemaSql(conn);
				}
				enableConstraints(disabled, conn);
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

	private List<String> readSqlFile(String fileName, AnnotatedElement callingClass) throws IOException
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
				return readSqlFile("/" + fileName, callingClass);
			}
			if (sqlFile == null)
			{
				ILogger log = LoggerFactory.getLogger(AmbethPersistenceRunner.class);
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
				return Collections.<String> emptyList();
			}
		}

		ILogger log = LoggerFactory.getLogger(AmbethPersistenceRunner.class);

		if (log.isDebugEnabled())
		{
			log.debug("Using sql file: " + sqlFile.getAbsolutePath());
		}

		StringBuilder sb = new StringBuilder();
		List<String> sql = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new FileReader(sqlFile));
		try
		{
			String line = null;
			Pattern endToLookFor = null;
			boolean ignoreThisCommand = false;
			IProperties properties = schemaContext.getService(IProperties.class);
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

	private void tearDownAllSQLContents(Connection conn, String schemaName) throws SQLException
	{
		// disableConstraints(conn, schemaName);
		dropTables(conn);
		dropOtherObjects(conn);
		purgeRecyclebin(conn);
	}

	private IList<String[]> disableConstraints(Connection conn) throws SQLException
	{
		return schemaContext.getService(IConnectionDialect.class).disableConstraints(conn);
	}

	private void enableConstraints(IList<String[]> disabled, Connection conn) throws SQLException
	{
		schemaContext.getService(IConnectionDialect.class).enableConstraints(conn, disabled);
	}

	private void dropTables(Connection conn) throws SQLException
	{
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			stmt = conn.createStatement();
			stmt.execute("SELECT TNAME, TABTYPE FROM TAB");
			rs = stmt.getResultSet();
			List<String> sql = new ArrayList<String>();
			while (rs.next())
			{
				String tableName = rs.getString(1);
				if (!binTableName.matcher(tableName).matches())
				{
					String tableType = rs.getString(2);
					if ("VIEW".equalsIgnoreCase(tableType))
					{
						sql.add("DROP VIEW \"" + tableName + "\" CASCADE CONSTRAINTS");
					}
					else if ("TABLE".equalsIgnoreCase(tableType))
					{
						sql.add("DROP TABLE \"" + tableName + "\" CASCADE CONSTRAINTS");
					}
					else if ("SYNONYM".equalsIgnoreCase(tableType))
					{
						sql.add("DROP SYNONYM \"" + tableName + "\"");
					}
					else
					{
						throw new IllegalStateException("Table type not supported: '" + tableType + "'");
					}
				}
			}
			JdbcUtil.close(stmt, rs);
			executeScript(sql, conn);
			sql.clear();
		}
		finally
		{
			JdbcUtil.close(stmt, rs);
		}
	}

	private void createOptimisticLockingTriggers(Connection conn) throws SQLException
	{
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			stmt = conn.createStatement();
			stmt.execute("SELECT T.TNAME FROM TAB T JOIN COLS C ON T.TNAME = C.TABLE_NAME WHERE C.COLUMN_NAME = 'VERSION'");
			rs = stmt.getResultSet();
			IConnectionDialect connectionDialect = schemaContext.getService(IConnectionDialect.class);
			int maxNameLength = conn.getMetaData().getMaxProcedureNameLength();
			List<String> sql = new ArrayList<String>();
			StringBuilder sb = new StringBuilder();
			while (rs.next())
			{
				String table = rs.getString(1);
				if (!binTableName.matcher(table).matches() && !table.toLowerCase().startsWith("link"))
				{
					String forTriggerName = table;
					if (forTriggerName.length() >= maxNameLength - 3 - 3) // Substract 3 chars 'TR_' and 3 chars '_OL'
					{
						forTriggerName = forTriggerName.substring(0, maxNameLength - 3 - 3);
					}
					sb.append("create or replace TRIGGER \"TR_").append(forTriggerName).append("_OL\"");
					sb.append("	BEFORE UPDATE ON \"").append(table).append("\" FOR EACH ROW");
					sb.append(" BEGIN");
					sb.append(" if( :new.\"VERSION\" <= :old.\"VERSION\" ) then");
					sb.append(" raise_application_error( -");
					sb.append(connectionDialect.getOptimisticLockErrorCode()).append(", 'Optimistic Lock Exception');");
					sb.append(" end if;");
					sb.append(" END;");
					sql.add(sb.toString());
					sb.setLength(0);
				}
			}
			JdbcUtil.close(stmt, rs);
			executeScript(sql, conn, false);
			sql.clear();
		}
		finally
		{
			JdbcUtil.close(stmt, rs);
		}
	}

	private void dropOtherObjects(Connection conn) throws SQLException
	{
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			stmt = conn.createStatement();
			stmt.execute("SELECT object_type, object_name FROM user_objects WHERE object_type IN ('FUNCTION', 'INDEX', 'PACKAGE', 'PACKAGE BODY', 'PROCEDURE', 'SEQUENCE', 'SYNONYM', 'TABLE', 'TYPE', 'VIEW')");
			rs = stmt.getResultSet();
			List<String> sql = new ArrayList<String>();
			while (rs.next())
			{
				String objectType = rs.getString("object_type");
				String objectName = rs.getString("object_name");
				if (binTableName.matcher(objectName).matches())
				{
					continue;
				}
				sql.add("DROP " + objectType + " " + objectName);
			}
			JdbcUtil.close(stmt, rs);
			executeScript(sql, conn);
			sql.clear();
		}
		finally
		{
			JdbcUtil.close(stmt, rs);
		}
	}

	private void purgeRecyclebin(Connection conn) throws SQLException
	{
		Statement stmt = null;
		try
		{
			stmt = conn.createStatement();
			stmt.execute("PURGE RECYCLEBIN");
		}
		catch (SQLException e)
		{
			conn.rollback();
			throw e;
		}
		finally
		{
			conn.commit();
			JdbcUtil.close(stmt);
		}
	}

	protected void truncateAllTables(Connection conn, String[] configuresSynonymNames, String... schemaNames) throws SQLException
	{
		List<String> allTableNames = getAllTableNames(conn, schemaNames);
		allTableNames.addAll(Arrays.asList(configuresSynonymNames));
		if (allTableNames.isEmpty())
		{
			return;
		}
		final List<String> sql = new ArrayList<String>();
		for (int i = allTableNames.size(); i-- > 0;)
		{
			String tableName = allTableNames.get(i);
			sql.add("DELETE FROM " + tableName);
		}

		executeWithDeferredConstraints(new ISchemaRunnable()
		{

			@Override
			public void executeSchemaSql(Connection connection) throws Exception
			{
				executeScript(sql, connection, false);
				sql.clear();
			}
		});
	}

	private List<String> getAllTableNames(Connection conn, String... schemaNames) throws SQLException
	{
		List<String> allTableNames = new ArrayList<String>();

		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			stmt = conn.createStatement();

			StringBuilder sb = new StringBuilder();
			sb.append("SELECT OWNER, TABLE_NAME FROM ALL_ALL_TABLES WHERE ");
			buildOwnerInClause(sb, schemaNames);
			stmt.execute(sb.toString());
			rs = stmt.getResultSet();
			while (rs.next())
			{
				String schemaName = rs.getString("OWNER");
				String tableName = rs.getString("TABLE_NAME");
				if (!binTableName.matcher(tableName).matches())
				{
					allTableNames.add("\"" + schemaName + "\".\"" + tableName + "\"");
				}
			}
		}
		finally
		{
			JdbcUtil.close(stmt, rs);
		}

		return allTableNames;
	}

	protected void buildOwnerInClause(StringBuilder sb, String... schemaNames)
	{
		sb.append("OWNER IN (");
		boolean first = true;
		for (int a = schemaNames.length; a-- > 0;)
		{
			if (!first)
			{
				sb.append(',');
			}
			sb.append('\'').append(schemaNames[a]).append('\'');
			first = false;
		}
		sb.append(')');
	}

	private void executeScript(List<String> sql, Connection conn) throws SQLException
	{
		executeScript(sql, conn, true);
	}

	private void executeScript(List<String> sql, Connection conn, boolean doCommitBehavior) throws SQLException
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
							ILogger log = LoggerFactory.getLogger(AmbethPersistenceRunner.class);

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
						for (Entry<String, List<Throwable>> entry : commandToExceptionMap)
						{
							List<Throwable> exceptionsOfCommand = entry.getValue();
							for (Throwable e : exceptionsOfCommand)
							{
								e.printStackTrace();
							}
						}
					}
					else if (commandToExceptionMap.size() == 1)
					{
						PersistenceException pe = new PersistenceException("Uncorrectable SQL exception(s)", commandToExceptionMap.iterator().next().getValue()
								.get(0));
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
					PersistenceException pe = new PersistenceException(errorMessage, commandToExceptionMap.iterator().next().getValue().get(0));
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

	private boolean canFailBeTolerated(String command)
	{
		return sqlTryOnlyCommands[0][0].matcher(command).matches();
	}

	private void handleSqlCommand(String command, Statement stmt, Map<String, Object> defaultOptions) throws SQLException
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
	//
	// private static final String RANDOM_USER_TEST_PROPERTIES = "c:/temp/random_user_test.properties";
	//
	// public static void beforeClass()
	// {
	// String[] args = new String[] { RandomUserScript.SCRIPT_IS_CREATE + "=true", RandomUserScript.SCRIPT_USER_PASS +
	// "=pw1,pw2",
	// RandomUserScript.SCRIPT_USER_PROPERTYFILE + "=" + RANDOM_USER_TEST_PROPERTIES };
	// runRandomUserScript(args);
	// }
	//
	// public static void afterClass()
	// {
	// String[] args = new String[] { RandomUserScript.SCRIPT_IS_CREATE + "=false",
	// RandomUserScript.SCRIPT_USER_PROPERTYFILE + "=" + RANDOM_USER_TEST_PROPERTIES };
	// runRandomUserScript(args);
	// }
	//
	// private static void runRandomUserScript(String[] args)
	// {
	// try
	// {
	// RandomUserScript.main(args);
	// }
	// catch (Throwable e)
	// {
	// throw RuntimeExceptionUtil.mask(e);
	// }
	// }

}
