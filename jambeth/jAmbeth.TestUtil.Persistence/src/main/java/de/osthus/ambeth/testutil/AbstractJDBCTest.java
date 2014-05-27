package de.osthus.ambeth.testutil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import oracle.jdbc.OracleConnection;

import org.junit.After;
import org.junit.Before;

import de.osthus.ambeth.cache.ClearAllCachesEvent;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.config.IocConfigurationConstants;
import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.database.DatabaseCallback;
import de.osthus.ambeth.database.ITransaction;
import de.osthus.ambeth.event.IEventDispatcher;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LoggerFactory;
import de.osthus.ambeth.oracle.Oracle10gConnectionModule;
import de.osthus.ambeth.persistence.IConnectionDialect;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.jdbc.IConnectionFactory;
import de.osthus.ambeth.persistence.jdbc.JdbcUtil;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import de.osthus.ambeth.service.config.ConfigurationConstants;
import de.osthus.ambeth.xml.DefaultXmlWriter;
import de.osthus.ambeth.xml.simple.AppendableStringBuilder;

@SuppressWarnings("deprecation")
public abstract class AbstractJDBCTest extends AbstractPersistenceAwareTest
{
	public static final String JDBC_TEST_ISOLATED_DATA = "jdbc.test.isolated.data";

	private static final ILogger log = LoggerFactory.getLogger(AbstractJDBCTest.class);

	private static final String nl = System.getProperty("line.separator");

	private static final Pattern binTableName = Pattern.compile("BIN\\$.{22}==\\$0", Pattern.CASE_INSENSITIVE);

	private static final Pattern test2Class = Pattern.compile("Test$");

	private static final Pattern lineSeparator = Pattern.compile(nl);

	private static final Pattern pathSeparator = Pattern.compile(File.pathSeparator);

	private static final Pattern optionSeparator = Pattern.compile("--");

	private static final Pattern optionPattern = Pattern.compile("(.+?)=(.*)");

	private static final Pattern whitespaces = Pattern.compile("[ \\t]+");

	private static final Pattern[] sqlComments = { Pattern.compile("^--[^:].*"), Pattern.compile("^/\\*.*\\*/") };

	private static final Pattern[] ignoreOutside = { Pattern.compile("^/$") };

	private static final Pattern[] ignoreIfContains = { Pattern.compile(".*?DROP CONSTRAINT.*?") };

	private static final Pattern[][] sqlCommands = {
			{ Pattern.compile("CREATE( +OR +REPLACE)? +(?:TABLE|VIEW|INDEX|TYPE|SEQUENCE) +.+", Pattern.CASE_INSENSITIVE), Pattern.compile(".*?([;\\/])") },
			{ Pattern.compile("CREATE( +OR +REPLACE)? +(?:FUNCTION|PROCEDURE|TRIGGER) +.+", Pattern.CASE_INSENSITIVE),
					Pattern.compile("END;", Pattern.CASE_INSENSITIVE) },
			{ Pattern.compile("ALTER +(?:TABLE|VIEW) .+", Pattern.CASE_INSENSITIVE), Pattern.compile(".*?([;\\/])") },
			{ Pattern.compile("(?:INSERT +INTO|UPDATE) .+", Pattern.CASE_INSENSITIVE), Pattern.compile(".*?\\)([;\\/])") },
			{ Pattern.compile("(?:COMMENT) .+", Pattern.CASE_INSENSITIVE), Pattern.compile(".*?([;\\/])") } };

	private static final Pattern[][] sqlIgnoredCommands = { { Pattern.compile("DROP +.+", Pattern.CASE_INSENSITIVE), Pattern.compile(".*?([;\\/])") } };

	private static final Pattern optionLine = Pattern.compile("^--:(.*)");

	private static List<Class<?>> running = new ArrayList<Class<?>>();

	protected static final IPropertiesProvider propertiesProvider = new IPropertiesProvider()
	{

		@Override
		public void fillProperties(Properties props)
		{
			// PersistenceJdbcModule
			props.put(PersistenceJdbcConfigurationConstants.AdditionalConnectionInterfaces, OracleConnection.class.getName());
			props.put(PersistenceJdbcConfigurationConstants.AdditionalConnectionModules, Oracle10gConnectionModule.class.getName());
			props.put(ConfigurationConstants.NetworkClientMode, "false");
			props.put(ConfigurationConstants.SlaveMode, "false");
			props.put(ConfigurationConstants.LogShortNames, "true");

			// IocModule
			props.put(IocConfigurationConstants.UseObjectCollector, "false");
		}
	};

	protected static final StringBuilder measurementXML = new StringBuilder();

	protected static final DefaultXmlWriter xmlWriter = new DefaultXmlWriter(new AppendableStringBuilder(measurementXML), null);

	public static void setUpBeforeClassJdbc(Class<?>... moduleTypes) throws Exception
	{
		setUpBeforeClassJdbc(moduleTypes, null, getCallingClass());
	}

	public static <T extends AbstractJDBCTest> void setUpBeforeClassJdbc(Class<?>[] moduleTypes, Class<T> callingClass) throws Exception
	{
		setUpBeforeClassJdbc(moduleTypes, null, callingClass);
	}

	public static <T extends AbstractJDBCTest> void setUpBeforeClassJdbc(Class<?>[] moduleTypes, Class<?>[] childModuleTypes, Class<T> callingClass)
			throws Exception
	{
		addPropertiesProvider(propertiesProvider);
		AbstractPersistenceAwareTest.setUpBeforeClassPersistence(moduleTypes, childModuleTypes);
		setUpBeforeClassJdbcCalling(callingClass);
	}

	public static <T extends AbstractJDBCTest> void setUpBeforeClassJdbcCalling(Class<T> callingClass) throws Exception
	{
		if (connection != null)
		{
			throw new IllegalStateException("No valid connection allowed at this point");
		}
		checkOS();
		// riverManager.registerDriver((Driver)
		// Thread.currentThread().getContextClassLoader().loadClass("oracle.jdbc.OracleDriver").newInstance());
		Thread.currentThread().getContextClassLoader().loadClass("oracle.jdbc.OracleDriver");
		Connection conn = getConnection();
		try
		{
			conn.setAutoCommit(false);
			checkSchemaEmpty(conn);

			running.add(callingClass);

			String[] sqlNames = getSqlNames(callingClass);
			executeSqlFile(sqlNames[0], conn, callingClass); // SQL structure file
			checkAfterStructure(conn);
		}
		catch (Exception e)
		{
			JdbcUtil.close(conn);
			throw e;
		}
		connection = conn;
		beanContext.getService(ITransaction.class).processAndCommit(new DatabaseCallback()
		{

			@Override
			public void callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap)
			{
				// Intended blank
			}
		});
		boolean jdbcTestIsolatedData = Boolean.parseBoolean(beanContext.getService(IProperties.class).getString(JDBC_TEST_ISOLATED_DATA, "true"));
		if (!jdbcTestIsolatedData)
		{
			String[] sqlNames = getSqlNames(callingClass);
			executeSqlFile(sqlNames[1], connection, callingClass); // SQL data file
		}
	}

	/**
	 * Due to a lot of new DB connections during tests /dev/random on CI servers may run low.
	 */
	private static void checkOS()
	{
		String os = System.getProperty("os.name").toLowerCase();
		if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0)
		{
			System.setProperty("java.security.egd", "file:///dev/urandom"); // the 3 '/' are important to make it an URL
		}
	}

	private static void checkAfterStructure(Connection conn) throws SQLException
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
			executeScript(sql, conn);
			sql.clear();
		}
		finally
		{
			JdbcUtil.close(stmt, rs);
		}
		createOptimisticLockingTriggers(conn);
	}

	public static void tearDownAfterClassJdbc() throws Exception
	{
		tearDownAfterClassJdbc(getCallingClass());
	}

	public static <T extends AbstractJDBCTest> void tearDownAfterClassJdbc(Class<T> callingClass) throws Exception
	{
		abstractTearDownAfterClass(callingClass);
		tearDownAfterClassPersistence();
	}

	private static Connection connection = null;

	public static <T extends AbstractJDBCTest> void abstractTearDownAfterClass(Class<T> callingClass) throws Exception
	{
		if (beanContext == null)
		{
			return;
		}
		String measurementFile = beanContext.getService(IProperties.class).getString("measurement.file", "measurements.xml");

		if (measurementXML.length() > 0)
		{
			String measurements = measurementXML.toString();
			measurementXML.setLength(0);

			// Do not use dots in xml element names
			String name = callingClass.getName().replaceAll("\\.", "_");
			xmlWriter.writeOpenElement(name);
			xmlWriter.write(measurements);
			xmlWriter.writeCloseElement(name);

			OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(measurementFile, true), Properties.CHARSET_UTF_8);
			try
			{
				osw.append(measurementXML);
			}
			finally
			{
				osw.close();
			}
		}
		if (connection == null || connection.isClosed())
		{
			connection = beanContext.getService(IConnectionFactory.class).create();
		}
		try
		{
			if (running.contains(callingClass))
			{
				String[] schemaNames = getSchemaNames();
				tearDownAllSQLContents(connection, schemaNames[0]);
				for (int i = schemaNames.length; i-- > 1;)
				{
					String schemaName = schemaNames[i];
					truncateAllTables(connection, schemaName);
				}
			}
		}
		finally
		{
			running.remove(callingClass);
			JdbcUtil.close(connection);
			connection = null;

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

	@Before
	public void abstractSetUp() throws Exception
	{
		if (beanContext == null)
		{
			return;
		}
		boolean jdbcTestIsolatedData = Boolean.parseBoolean(beanContext.getService(IProperties.class).getString(JDBC_TEST_ISOLATED_DATA, "true"));
		if (jdbcTestIsolatedData)
		{
			String[] sqlNames = getSqlNames(this.getClass());
			executeSqlFile(sqlNames[1], connection, this.getClass()); // SQL data file
		}
		// Trigger clearing of other maps and caches (QueryResultCache,...)
		beanContext.getService(IEventDispatcher.class).dispatchEvent(ClearAllCachesEvent.getInstance());
	}

	@After
	public void abstractTearDown() throws Exception
	{
		if (beanContext == null)
		{
			return;
		}
		boolean jdbcTestIsolatedData = Boolean.parseBoolean(beanContext.getService(IProperties.class).getString(JDBC_TEST_ISOLATED_DATA, "true"));
		if (jdbcTestIsolatedData)
		{
			if (running.contains(this.getClass()))
			{
				String[] schemaNames = getSchemaNames();
				for (String schemaName : schemaNames)
				{
					truncateAllTables(connection, schemaName);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static <T extends AbstractJDBCTest> Class<T> getCallingClass() throws ClassNotFoundException
	{
		String className = null;
		Pattern sunReflect = Pattern.compile("^sun\\.reflect\\..+");

		for (StackTraceElement ste : Thread.currentThread().getStackTrace())
		{
			if (sunReflect.matcher(ste.getClassName()).matches())
			{
				break;
			}
			className = ste.getClassName();
		}

		return (Class<T>) Thread.currentThread().getContextClassLoader().loadClass(className);
	}

	protected static void logMeasurement(String name, Object value)
	{
		String elementName = name.replaceAll(" ", "_").replaceAll("\\.", "_").replaceAll("\\(", ":").replaceAll("\\)", ":");
		xmlWriter.writeOpenElement(elementName);
		xmlWriter.writeEscapedXml(value.toString());
		xmlWriter.writeCloseElement(elementName);
	}

	private static Connection getConnection() throws SQLException
	{
		Connection conn = beanContext.getService(IConnectionFactory.class).create();
		conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		return conn;
	}

	private static String[] getSchemaNames()
	{
		IProperties properties = beanContext.getService(IProperties.class);
		String schemaProperty = (String) properties.get(PersistenceJdbcConfigurationConstants.DatabaseSchemaName);
		String[] schemaNames = schemaProperty.toUpperCase().split("[:;]");
		return schemaNames;
	}

	private static void checkSchemaEmpty(Connection conn) throws SQLException
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
					truncateAllTables(conn, schemaName);
				}
			}
		}
	}

	private static boolean checkMainSchemaEmpty(Connection conn) throws SQLException
	{
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			stmt = conn.createStatement();
			stmt.execute("SELECT tname FROM tab");
			rs = stmt.getResultSet();
			while (rs.next())
			{
				if (!binTableName.matcher(rs.getString("tname")).matches())
				{
					return false;
				}
			}
			return true;
		}
		finally
		{
			JdbcUtil.close(stmt, rs);
		}
	}

	private static boolean checkAdditionalSchemaEmpty(Connection conn, String schemaName) throws SQLException
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
				stmt.execute("SELECT * FROM " + schemaName + "." + tableName + " WHERE ROWNUM = 1");
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

	private static String[] getSqlNames(Class<?> callingClass) throws FileNotFoundException
	{
		String name = test2Class.matcher(callingClass.getSimpleName()).replaceFirst("");
		String[] files = { name + "_structure.sql", name + "_data.sql" };
		SqlFiles anno;
		if (null != (anno = callingClass.getAnnotation(SqlFiles.class)))
		{
			if (!anno.structure().isEmpty())
			{
				files[0] = anno.structure();
			}
			if (!anno.data().isEmpty())
			{
				files[1] = anno.data();
			}
		}
		return files;
	}

	private static void executeSqlFile(String fileName, Connection conn, Class<?> callingClass) throws IOException, SQLException
	{
		List<String> sql = readSqlFile(fileName, callingClass);
		if (!sql.isEmpty())
		{
			executeScript(sql, conn);
		}
	}

	private static List<String> readSqlFile(String fileName, Class<?> callingClass) throws IOException
	{
		File sqlFile = null;
		String relativePath = (fileName.startsWith("/") ? "." : callingClass.getPackage().getName().replace(".", File.separator)) + File.separator + fileName;
		String[] classPaths = pathSeparator.split(System.getProperty("java.class.path"));
		for (int i = 0; i < classPaths.length; i++)
		{
			File tempFile = new File(classPaths[i], relativePath);
			if (tempFile.canRead())
			{
				sqlFile = tempFile;
				break;
			}
		}
		if (sqlFile == null)
		{
			if (log.isWarnEnabled())
			{
				String error = "Cannot find '" + relativePath + "' in class path:" + nl;
				for (int i = 0; i < classPaths.length; i++)
				{
					error += classPaths[i] + nl;
				}
				log.warn(error);
			}
			return Collections.<String> emptyList();
		}

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

	private static void tearDownAllSQLContents(Connection conn, String schemaName) throws SQLException
	{
		Statement stmt = null;
		try
		{
			disableConstraints(conn, schemaName);
			dropTables(conn);
			dropOtherObjects(conn);
			purgeRecyclebin(conn);
		}
		finally
		{
			JdbcUtil.close(stmt);
		}
	}

	private static List<String[]> disableConstraints(Connection conn, String schemaName) throws SQLException
	{
		List<String[]> disabled = new ArrayList<String[]>();
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			stmt = conn.createStatement();
			stmt.execute("SELECT OWNER, TABLE_NAME, CONSTRAINT_NAME FROM ALL_CONSTRAINTS WHERE OWNER = '" + schemaName
					+ "' AND STATUS = 'ENABLED' AND CONSTRAINT_TYPE = 'R'");
			rs = stmt.getResultSet();
			List<String> sql = new ArrayList<String>();
			while (rs.next())
			{
				String tableName = rs.getString("TABLE_NAME");
				if (!binTableName.matcher(tableName).matches())
				{
					String constraintName = rs.getString("CONSTRAINT_NAME");
					String fullName = schemaName + '.' + tableName;
					sql.add("ALTER TABLE " + fullName + " MODIFY CONSTRAINT " + constraintName + " DISABLE");
					disabled.add(new String[] { fullName, constraintName });
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
		return disabled;
	}

	private static void enableConstraints(List<String[]> disabled, Connection conn) throws SQLException
	{
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			List<String> sql = new ArrayList<String>();
			for (int i = disabled.size(); i-- > 0;)
			{
				sql.add("ALTER TABLE " + disabled.get(i)[0] + " MODIFY CONSTRAINT " + disabled.get(i)[1] + " ENABLE");
			}
			executeScript(sql, conn);
			sql.clear();
		}
		finally
		{
			JdbcUtil.close(stmt, rs);
		}
	}

	private static void dropTables(Connection conn) throws SQLException
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
					else
					{
						sql.add("DROP TABLE \"" + tableName + "\" CASCADE CONSTRAINTS");
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

	private static void createOptimisticLockingTriggers(Connection conn) throws SQLException
	{
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			stmt = conn.createStatement();
			stmt.execute("SELECT T.TNAME FROM TAB T JOIN COLS C ON T.TNAME = C.TABLE_NAME WHERE C.COLUMN_NAME = 'VERSION'");
			rs = stmt.getResultSet();
			IConnectionDialect connectionDialect = beanContext.getService(IConnectionDialect.class);
			List<String> sql = new ArrayList<String>();
			StringBuilder sb = new StringBuilder();
			while (rs.next())
			{
				String table = rs.getString(1);
				if (!binTableName.matcher(table).matches() && !table.toLowerCase().startsWith("link"))
				{
					if (table.length() >= 32 - 3 - 3) // Substract 3 chars 'TR_' and 3 chars '_OL'
					{
						table = table.substring(0, 32 - 3 - 3);
					}
					sb.append("create or replace TRIGGER \"TR_").append(table).append("_OL\"");
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
			executeScript(sql, conn);
			sql.clear();
		}
		finally
		{
			JdbcUtil.close(stmt, rs);
		}

	}

	private static void dropOtherObjects(Connection conn) throws SQLException
	{
		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			stmt = conn.createStatement();
			stmt.execute("SELECT object_type, object_name FROM user_objects WHERE object_type IN ('FUNCTION', 'INDEX', 'PACKAGE', 'PACKAGE BODY', 'PROCEDURE', 'SEQUENCE', 'TABLE', 'TYPE', 'VIEW')");
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

	private static void purgeRecyclebin(Connection conn) throws SQLException
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

	private static void truncateAllTables(Connection conn, String schemaName) throws SQLException
	{
		List<String> allTableNames = getAllTableNames(conn, schemaName);
		if (allTableNames.isEmpty())
		{
			return;
		}

		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			List<String[]> disabled = disableConstraints(conn, schemaName);
			try
			{
				List<String> sql = new ArrayList<String>();
				for (int i = allTableNames.size(); i-- > 0;)
				{
					String tableName = allTableNames.get(i);
					sql.add("DELETE FROM " + schemaName + "." + tableName + "");
				}
				executeScript(sql, conn);
				sql.clear();
			}
			finally
			{
				enableConstraints(disabled, conn);
			}
		}
		finally
		{
			JdbcUtil.close(stmt, rs);
		}
	}

	private static List<String> getAllTableNames(Connection conn, String schemaName) throws SQLException
	{
		List<String> allTableNames = new ArrayList<String>();

		Statement stmt = null;
		ResultSet rs = null;
		try
		{
			stmt = conn.createStatement();
			stmt.execute("SELECT TABLE_NAME FROM ALL_ALL_TABLES WHERE OWNER = '" + schemaName + "'");
			rs = stmt.getResultSet();
			while (rs.next())
			{
				String tableName = rs.getString("TABLE_NAME");
				if (!binTableName.matcher(tableName).matches())
				{
					allTableNames.add(tableName);
				}
			}
		}
		finally
		{
			JdbcUtil.close(stmt, rs);
		}

		return allTableNames;
	}

	private static void executeScript(List<String> sql, Connection conn) throws SQLException
	{
		Statement stmt = null;
		List<Throwable> exceptions = new ArrayList<Throwable>();
		Map<String, Object> defaultOptions = new HashMap<String, Object>();
		defaultOptions.put("loop", 1);
		try
		{
			stmt = conn.createStatement();
			List<String> done = new ArrayList<String>();
			do
			{
				done.clear();
				exceptions.clear();
				for (String command : sql)
				{
					try
					{
						handleSqlCommand(command, stmt, defaultOptions);
						done.add(command);
					}
					catch (Throwable e)
					{
						exceptions.add(e);
					}
				}
				sql.removeAll(done);
			}
			while (sql.size() > 0 && done.size() > 0);

			if (sql.isEmpty())
			{
				conn.commit();
			}
			else
			{
				conn.rollback();
				if (exceptions.size() > 1)
				{
					for (Throwable e : exceptions)
					{
						e.printStackTrace();
					}
				}
				else if (exceptions.size() == 1)
				{
					throw new SQLException("Uncorrectable SQL exception(s)", exceptions.get(0));
				}
				else
				{
					throw new SQLException("Uncorrectable SQL exception(s)");
				}
			}
		}
		finally
		{
			JdbcUtil.close(stmt);
		}
	}

	private static void handleSqlCommand(String command, Statement stmt, Map<String, Object> defaultOptions) throws SQLException
	{
		Map<String, Object> options = defaultOptions;
		Matcher optionLine = AbstractJDBCTest.optionLine.matcher(command.trim());
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
		for (int i = loopCount; i-- > 0;)
		{
			stmt.addBatch(command);
		}
		stmt.executeBatch();
	}
}
