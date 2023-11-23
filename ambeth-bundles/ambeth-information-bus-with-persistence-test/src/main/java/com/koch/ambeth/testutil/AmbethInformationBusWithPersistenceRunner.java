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

import com.koch.ambeth.informationbus.persistence.datagenerator.TestDataModule;
import com.koch.ambeth.informationbus.persistence.setup.AmbethPersistenceSchemaModule;
import com.koch.ambeth.informationbus.persistence.setup.AmbethPersistenceSetup;
import com.koch.ambeth.informationbus.persistence.setup.DataSetupExecutor;
import com.koch.ambeth.informationbus.persistence.setup.DataSetupExecutorModule;
import com.koch.ambeth.informationbus.persistence.setup.DialectSelectorTestModule;
import com.koch.ambeth.informationbus.persistence.setup.ISchemaFileProvider;
import com.koch.ambeth.informationbus.persistence.setup.ISchemaRunnable;
import com.koch.ambeth.informationbus.persistence.setup.SQLData;
import com.koch.ambeth.informationbus.persistence.setup.SQLDataList;
import com.koch.ambeth.informationbus.persistence.setup.SQLStructure;
import com.koch.ambeth.informationbus.persistence.setup.SQLStructureList;
import com.koch.ambeth.informationbus.persistence.setup.SQLTableSynonyms;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.factory.BeanContextFactory;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.ioc.log.ILoggerCache;
import com.koch.ambeth.ioc.util.ImmutableTypeSet;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.log.io.FileUtil;
import com.koch.ambeth.merge.ChangeControllerState;
import com.koch.ambeth.merge.ILightweightTransaction;
import com.koch.ambeth.merge.changecontroller.IChangeController;
import com.koch.ambeth.merge.config.MergeConfigurationConstants;
import com.koch.ambeth.merge.util.setup.IDataSetup;
import com.koch.ambeth.merge.util.setup.SetupModule;
import com.koch.ambeth.persistence.IConnectionDialect;
import com.koch.ambeth.persistence.connection.IPreparedConnectionHolder;
import com.koch.ambeth.persistence.jdbc.IConnectionFactory;
import com.koch.ambeth.persistence.jdbc.IConnectionTestDialect;
import com.koch.ambeth.persistence.jdbc.JdbcUtil;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.persistence.jdbc.connector.DatabaseProtocolResolver;
import com.koch.ambeth.persistence.jdbc.connector.DialectSelectorModule;
import com.koch.ambeth.security.DefaultAuthentication;
import com.koch.ambeth.security.ISecurityContextHolder;
import com.koch.ambeth.security.PasswordType;
import com.koch.ambeth.security.SecurityContextType;
import com.koch.ambeth.security.StringSecurityScope;
import com.koch.ambeth.security.TestAuthentication;
import com.koch.ambeth.security.server.SecurityFilterInterceptor;
import com.koch.ambeth.security.server.SecurityFilterInterceptor.SecurityMethodMode;
import com.koch.ambeth.service.proxy.IMethodLevelBehavior;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.NullPrintStream;
import com.koch.ambeth.util.annotation.IAnnotationInfo;
import com.koch.ambeth.util.appendable.AppendableStringBuilder;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.collections.LinkedHashMap;
import com.koch.ambeth.util.collections.LinkedHashSet;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.util.config.UtilConfigurationConstants;
import com.koch.ambeth.util.exception.MaskingRuntimeException;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.proxy.IProxyFactory;
import com.koch.ambeth.util.state.IStateRollback;
import com.koch.ambeth.util.state.StateRollback;
import com.koch.ambeth.xml.DefaultXmlWriter;
import jakarta.persistence.PersistenceException;
import lombok.SneakyThrows;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * TODO: Handle test methods which change the structure
 */
public class AmbethInformationBusWithPersistenceRunner extends AmbethInformationBusRunner {
    protected static final String MEASUREMENT_BEAN = "measurementBean";
    private static final String nl = System.getProperty("line.separator");
    private static final Pattern lineSeparator = Pattern.compile(nl);
    private static final Pattern pathSeparator = Pattern.compile(File.pathSeparator);
    private static final Pattern optionSeparator = Pattern.compile("--");
    private static final Pattern optionPattern = Pattern.compile("(.+?)=(.*)");
    private static final Pattern whitespaces = Pattern.compile("[ \\t]+");
    private static final Pattern[] sqlComments = {
            Pattern.compile("^--[^:].*"), Pattern.compile("^/\\*.*\\*/"), Pattern.compile(" *@@@ *")
    };
    private static final Pattern[] ignoreOutside = { Pattern.compile("^/$") };
    private static final Pattern[] ignoreIfContains = { Pattern.compile(".*?DROP CONSTRAINT.*?") };
    private static final Pattern[][] sqlCommands = {
            {
                    Pattern.compile("CREATE( +OR +REPLACE)? +(?:TABLE|VIEW|INDEX|TYPE|SEQUENCE|SYNONYM|TABLESPACE) +.+", Pattern.CASE_INSENSITIVE), Pattern.compile(".*?([;\\/]|@@@)")
            }, {
            Pattern.compile("CREATE( +OR +REPLACE)? +(?:FUNCTION|PROCEDURE|TRIGGER) +.+", Pattern.CASE_INSENSITIVE), Pattern.compile(".*?END(?:[;\\/]|@@@)", Pattern.CASE_INSENSITIVE)
    }, {
            Pattern.compile("ALTER +(?:TABLE|VIEW) .+", Pattern.CASE_INSENSITIVE), Pattern.compile(".*?([;\\/]|@@@)")
    }, { Pattern.compile("CALL +.+", Pattern.CASE_INSENSITIVE), Pattern.compile(".*?([;\\/]|@@@)") }, {
            Pattern.compile("(?:INSERT +INTO|UPDATE) .+", Pattern.CASE_INSENSITIVE), Pattern.compile(".*?([;\\/]|@@@)")
    }, {
            Pattern.compile("(?:COMMENT) .+", Pattern.CASE_INSENSITIVE), Pattern.compile(".*?([;\\/]|@@@)")
    }
    };
    private static final Pattern[][] sqlIgnoredCommands = {
            {
                    Pattern.compile("DROP +.+", Pattern.CASE_INSENSITIVE), Pattern.compile(".*?([;\\/]|@@@)")
            }
    };
    // TODO Check only implemented for first array element
    private static final Pattern[][] sqlTryOnlyCommands = {
            { Pattern.compile("CREATE OR REPLACE *.*") }
    };
    private static final Pattern optionLine = Pattern.compile("^--:(.*)");
    private static String[] sqlExecutionOrder;

    public static String[] getSqlExecutionOrder() {
        if (sqlExecutionOrder == null) {
            return sqlExecutionOrder;
        }
        return sqlExecutionOrder.clone();
    }

    private static boolean canFailBeTolerated(final String command) {
        return sqlTryOnlyCommands[0][0].matcher(command).matches();
    }

    protected static void createOptimisticLockingTriggers(final Connection conn, List<String> sqlExecutionOrder, IConnectionDialect connectionDialect, IConnectionTestDialect connectionTestDialect,
            ILogger log, boolean doExecuteStrict) throws SQLException {
        List<String> tableNames = connectionTestDialect.getTablesWithoutOptimisticLockTrigger(conn);

        ArrayList<String> sql = new ArrayList<>();
        for (String tableName : tableNames) {
            sql.addAll(connectionTestDialect.createOptimisticLockTrigger(conn, tableName));
            sql.addAll(connectionTestDialect.createAdditionalTriggers(conn, tableName));
        }
        executeScript(sql, conn, false, null, sqlExecutionOrder, connectionDialect, log, doExecuteStrict);
    }

    protected static void createPermissionGroups(final Connection conn, List<String> sqlExecutionOrder, IConnectionDialect connectionDialect, IConnectionTestDialect connectionTestDialect, ILogger log,
            boolean doExecuteStrict) throws SQLException {
        List<String> tableNames = connectionTestDialect.getTablesWithoutPermissionGroup(conn);

        ArrayList<String> sql = new ArrayList<>();
        for (String tableName : tableNames) {
            sql.addAll(connectionTestDialect.createPermissionGroup(conn, tableName));
        }
        executeScript(sql, conn, false, null, sqlExecutionOrder, connectionDialect, log, doExecuteStrict);
    }

    private static void ensureExistenceOfNeededDatabaseObjects(final Connection conn, List<String> sqlExecutionOrder, IConnectionDialect connectionDialect,
            IConnectionTestDialect connectionTestDialect, ILogger log, boolean doExecuteStrict) throws SQLException {
        createOptimisticLockingTriggers(conn, sqlExecutionOrder, connectionDialect, connectionTestDialect, log, doExecuteStrict);
        createPermissionGroups(conn, sqlExecutionOrder, connectionDialect, connectionTestDialect, log, doExecuteStrict);
    }

    static void executeScript(final List<String> sql, final Connection conn, final boolean doCommitBehavior, Map<String, String> sqlToSourceMap, List<String> sqlExecutionOrder,
            IConnectionDialect connectionDialect, ILogger log, boolean doExecuteStrict) throws SQLException {
        if (sql.isEmpty()) {
            return;
        }
        Statement stmt = null;
        // Must be a linked map to maintain sequential order while iterating
        IMap<String, List<Throwable>> commandToExceptionMap = new LinkedHashMap<>();
        Map<String, Object> defaultOptions = new HashMap<>();
        defaultOptions.put("loop", 1);
        try {
            stmt = conn.createStatement();
            List<String> done = new ArrayList<>();
            do {
                done.clear();
                for (String command : sql) {
                    if (command == null || command.length() == 0) {
                        done.add(command);
                        continue;
                    }
                    try {
                        handleSqlCommand(command, stmt, defaultOptions, connectionDialect);
                        done.add(command);
                        // If the command was successful, remove the key from the exception log
                        commandToExceptionMap.remove(command);
                        if (sqlExecutionOrder != null) {
                            sqlExecutionOrder.add(command);
                        }
                    } catch (PersistenceException e) {
                        // When executing multiple sql files some statements collide and cannot all
                        // be executed
                        if (!doExecuteStrict && canFailBeTolerated(command)) {
                            if (log.isWarnEnabled()) {
                                log.warn("SQL statement failed: '" + command + "'");
                            }
                            done.add(command);
                            commandToExceptionMap.remove(command);
                            continue;
                        }
                        // Store only first exception per command (the exception itself can change
                        // for the same
                        // command
                        // depending on the state of the schema - but we want the FIRST exception)
                        List<Throwable> exceptionsOfCommand = commandToExceptionMap.get(command);
                        if (exceptionsOfCommand == null) {
                            exceptionsOfCommand = new ArrayList<>();
                            commandToExceptionMap.put(command, exceptionsOfCommand);
                        }
                        exceptionsOfCommand.add(e);
                    }
                }
                sql.removeAll(done);
            } while (!sql.isEmpty() && !done.isEmpty());

            if (doCommitBehavior) {
                if (sql.isEmpty()) {
                    conn.commit();
                } else {
                    conn.rollback();
                    if (commandToExceptionMap.size() > 1) {
                        for (List<Throwable> exceptionsOfCommand : commandToExceptionMap.values()) {
                            for (Throwable e : exceptionsOfCommand) {
                                e.printStackTrace();
                            }
                        }
                    } else if (commandToExceptionMap.size() == 1) {
                        PersistenceException pe = new PersistenceException("Uncorrectable SQL exception(s)", commandToExceptionMap.values().get(0).get(0));
                        pe.setStackTrace(new StackTraceElement[0]);
                        throw pe;
                    } else {
                        throw new PersistenceException("Uncorrectable SQL exception(s)");
                    }
                }
            } else if (!sql.isEmpty()) {
                if (!commandToExceptionMap.isEmpty()) {
                    String errorMessage = "Uncorrectable SQL exception(s)";
                    Entry<String, List<Throwable>> firstEntry = commandToExceptionMap.iterator().next();
                    if (sqlToSourceMap != null) {
                        String source = sqlToSourceMap.get(firstEntry.getKey());
                        if (source != null) {
                            errorMessage += " from source '" + source + "'";
                        }
                    }
                    if (commandToExceptionMap.size() > 1) {
                        errorMessage += ". There are " + commandToExceptionMap.size() + " exceptions! The first one is:";
                    }
                    PersistenceException pe = new PersistenceException(errorMessage, firstEntry.getValue().get(0));
                    pe.setStackTrace(new StackTraceElement[0]);
                    throw pe;
                }
            }
        } finally {
            JdbcUtil.close(stmt);
        }
    }

    protected static void getSchemaRunnable(IServiceContext schemaContext, Class<? extends ISchemaRunnable> schemaRunnableType, Class<? extends ISchemaFileProvider> valueProviderType,
            String[] schemaFiles, List<ISchemaRunnable> schemaRunnables, final AnnotatedElement callingClass, final boolean doCommitBehavior, IList<String> allSQL, IMap<String, String> sqlToSourceMap,
            final IConnectionDialect connectionDialect, final IProperties properties, final ILogger log, final boolean doExecuteStrict) {
        try {
            if (schemaRunnableType != null && !ISchemaRunnable.class.equals(schemaRunnableType)) {
                var schemaRunnable = schemaContext.registerBean(schemaRunnableType).finish();
                schemaRunnables.add(schemaRunnable);
            }
            if (valueProviderType != null && !ISchemaFileProvider.class.equals(valueProviderType)) {
                var valueProvider = schemaContext.registerBean(valueProviderType).finish();
                var additionalSchemaFiles = valueProvider.getSchemaFiles();
                var set = new LinkedHashSet<>(schemaFiles);
                set.addAll(additionalSchemaFiles);
                schemaFiles = set.toArray(String.class);
            }
            for (String schemaFile : schemaFiles) {
                if (schemaFile == null || schemaFile.length() == 0) {
                    continue;
                }
                var sql = readSqlFile(schemaFile, callingClass, properties, log);
                allSQL.addAll(sql);
                for (String oneSql : sql) {
                    sqlToSourceMap.put(oneSql, schemaFile);
                }
            }
        } catch (Exception e) {
            throw RuntimeExceptionUtil.mask(e);
        }
    }

    private static void handleSqlCommand(String command, final Statement stmt, final Map<String, Object> defaultOptions, IConnectionDialect connectionDialect) throws SQLException {
        var options = defaultOptions;
        var optionLine = AmbethInformationBusWithPersistenceRunner.optionLine.matcher(command.trim());
        if (optionLine.find()) {
            options = new HashMap<>(defaultOptions);
            var optionString = optionLine.group(1).replace(" ", "");
            var preSqls = optionSeparator.split(optionString);
            command = lineSeparator.split(command, 2)[1];
            Object value;
            for (int i = preSqls.length; i-- > 0; ) {
                var keyValue = optionPattern.matcher(preSqls[i]);
                value = null;
                if (keyValue.find()) {
                    if ("loop".equals(keyValue.group(1))) {
                        value = Integer.parseInt(keyValue.group(2));
                    }
                    options.put(keyValue.group(1), value);
                }
            }
        }
        command = connectionDialect.prepareCommand(command);
        if (command == null || command.length() == 0) {
            return;
        }
        int loopCount = ((Integer) options.get("loop")).intValue();
        if (loopCount == 1) {
            stmt.execute(command);
        } else {
            for (int i = loopCount; i-- > 0; ) {
                stmt.addBatch(command);
            }
            stmt.executeBatch();
        }
    }

    protected static BufferedReader openSqlAsFile(String fileName, AnnotatedElement callingClass, ILogger log) throws IOException, FileNotFoundException {
        File sqlFile = null;
        var tempFile = new File(fileName);
        if (tempFile.canRead()) {
            sqlFile = tempFile;
        }
        if (sqlFile == null) {
            String callingNamespace;
            if (callingClass instanceof Class) {
                callingNamespace = ((Class<?>) callingClass).getPackage().getName();
            } else if (callingClass instanceof Method) {
                callingNamespace = ((Method) callingClass).getDeclaringClass().getPackage().getName();
            } else if (callingClass instanceof Field) {
                callingNamespace = ((Field) callingClass).getDeclaringClass().getPackage().getName();
            } else {
                throw new IllegalStateException("Value not supported: " + callingClass);
            }
            var relativePath = fileName.startsWith("/") ? "." + fileName : callingNamespace.replace(".", File.separator) + File.separator + fileName;
            var forwardPath = relativePath.replace('\\', '/');
            var classPaths = pathSeparator.split(System.getProperty("java.class.path"));
            for (int i = 0; i < classPaths.length; i++) {
                var classpathEntry = new File(classPaths[i]);
                if (classpathEntry.isDirectory()) {
                    tempFile = new File(classPaths[i], relativePath);
                    if (tempFile.canRead()) {
                        sqlFile = tempFile;
                        break;
                    }
                } else if (classpathEntry.isFile()) {
                    var keepOpen = false;
                    var jis = new ZipInputStream(new FileInputStream(classpathEntry));
                    try {
                        ZipEntry jarEntry;
                        while ((jarEntry = jis.getNextEntry()) != null) {
                            var jarEntryName = jarEntry.getName();
                            if (jarEntryName.equals(forwardPath)) {
                                keepOpen = true;
                                return new BufferedReader(new InputStreamReader(jis, Charset.forName("UTF-8")));
                            }
                        }
                    } finally {
                        if (!keepOpen) {
                            jis.close();
                        }
                    }
                }
            }
            if (sqlFile == null) {
                var fileSuffixPattern = Pattern.compile(".+\\.(?:[^\\.]*)");
                var matcher = fileSuffixPattern.matcher(relativePath);
                if (!matcher.matches()) {
                    relativePath += ".sql";
                    for (int i = 0; i < classPaths.length; i++) {
                        tempFile = new File(classPaths[i], relativePath);
                        if (tempFile.canRead()) {
                            sqlFile = tempFile;
                            break;
                        }
                    }
                }
            }
            if (sqlFile == null && !fileName.startsWith("/")) {
                // Path is not with root-slash specified. Try to add this before giving up:
                return openSqlAsFile("/" + fileName, callingClass, log);
            }
            if (sqlFile == null) {
                if (log.isWarnEnabled()) {
                    var error = "Cannot find '" + relativePath + "' in class path:" + nl;
                    Arrays.sort(classPaths);
                    for (int i = 0; i < classPaths.length; i++) {
                        error += "\t" + classPaths[i] + nl;
                    }
                    log.warn(error);
                }
                return null;
            }
        }

        BufferedReader br = null;
        if (sqlFile != null) {
            br = new BufferedReader(new FileReader(sqlFile));
        }

        return br;
    }

    private static List<String> readSqlFile(final String fileName, final AnnotatedElement callingClass, IProperties properties, ILogger log) throws IOException {
        BufferedReader br = null;
        try {
            var lookupName = fileName.startsWith("/") ? fileName.substring(1) : fileName;
            var sqlStream = FileUtil.openFileStream(lookupName, log);
            if (sqlStream != null) {
                br = new BufferedReader(new InputStreamReader(sqlStream));
                log = null;
            }
        } catch (IllegalArgumentException e) {
            // Opening as Stream failed. Try old file code next.
            br = openSqlAsFile(fileName, callingClass, log);
        }

        if (log != null && log.isDebugEnabled()) {
            if (br != null) {
                log.debug("Using sql resource '" + fileName + "'");
            }
        }
        if (br == null) {
            throw new FileNotFoundException(fileName);
        }
        var sb = new StringBuilder();
        var sql = new ArrayList<String>();
        try {
            String line = null;
            Pattern endToLookFor = null;
            boolean ignoreThisCommand = false;
            allLines:
            while (null != (line = br.readLine())) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue allLines;
                }
                for (Pattern comment : sqlComments) {
                    if (comment.matcher(line).matches()) {
                        continue allLines;
                    }
                }
                if (endToLookFor == null) {
                    for (Pattern ignore : ignoreOutside) {
                        if (ignore.matcher(line).matches()) {
                            continue allLines;
                        }
                    }
                }

                if (!optionLine.matcher(line).matches()) {
                    sb.append(line + " ");
                } else {
                    sb.append(line + nl);
                }

                if (endToLookFor == null) {
                    for (var command : sqlCommands) {
                        if (command[0].matcher(line).matches()) {
                            endToLookFor = command[1];
                            break;
                        }
                    }
                }
                if (endToLookFor == null) {
                    for (var command : sqlIgnoredCommands) {
                        if (command[0].matcher(line).matches()) {
                            endToLookFor = command[1];
                            ignoreThisCommand = true;
                            break;
                        }
                    }
                }
                if (endToLookFor != null) {
                    for (var part : ignoreIfContains) {
                        if (part.matcher(line).matches()) {
                            ignoreThisCommand = true;
                            break;
                        }
                    }
                }

                if (endToLookFor != null && endToLookFor.matcher(line).matches()) {
                    if (!ignoreThisCommand) {
                        var lineEnd = endToLookFor.matcher(line);
                        int toCut = 1; // Trailing space
                        if (lineEnd.find() && lineEnd.groupCount() == 1) {
                            toCut += lineEnd.group(1).length();
                        }
                        sb.setLength(sb.length() - toCut);
                        var commandRaw = sb.toString();
                        commandRaw = properties.resolvePropertyParts(commandRaw);
                        var commandRep = whitespaces.matcher(commandRaw).replaceAll(" ");
                        sql.add(commandRep);
                    }
                    sb.setLength(0);
                    endToLookFor = null;
                    ignoreThisCommand = false;
                }
            }
        } finally {
            br.close();
        }

        return sql;
    }

    protected final StringBuilder measurementXML = new StringBuilder();
    protected final DefaultXmlWriter xmlWriter = new DefaultXmlWriter(new AppendableStringBuilder(measurementXML), null, new ImmutableTypeSet());
    protected boolean isRebuildDataForThisTestRecommended;
    protected boolean doExecuteStrict = false;
    protected boolean testUserHasBeenCreated;
    private Connection connection;
    private IServiceContext schemaContext;
    /**
     * Flag which is set to true after the structure was build.
     */
    private boolean isStructureRebuildAlreadyHandled = false;
    /**
     * Flag which is set to true after the first test method was executed.
     */
    private boolean isFirstTestMethodAlreadyExecuted;
    /**
     * Flag which is set if the last test method has triggered a context rebuild.
     */
    private boolean lastMethodTriggersContextRebuild;

    private JdbcDatabaseContainer<?> jdbcDatabaseContainer;

    private AmbethPersistenceSetup ambethPersistenceSetup;

    public AmbethInformationBusWithPersistenceRunner(final Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    protected List<Class<? extends IInitializingModule>> buildFrameworkTestModuleList(FrameworkMethod frameworkMethod) {
        List<Class<? extends IInitializingModule>> frameworkTestModuleList = super.buildFrameworkTestModuleList(frameworkMethod);
        frameworkTestModuleList.add(DialectSelectorTestModule.class);
        frameworkTestModuleList.add(DataSetupExecutorModule.class);
        frameworkTestModuleList.add(SetupModule.class);
        frameworkTestModuleList.add(TestDataModule.class);
        return frameworkTestModuleList;
    }

    private boolean checkAdditionalSchemaEmpty(final Connection conn, final String schemaName) throws SQLException {
        IConnectionDialect connectionDialect = getOrCreateSchemaContext().getService(IConnectionDialect.class);
        List<String> allTableNames = connectionDialect.getAllFullqualifiedTableNames(conn, schemaName);

        for (int i = allTableNames.size(); i-- > 0; ) {
            String tableName = allTableNames.get(i);
            Statement stmt = null;
            ResultSet rs = null;
            try {
                stmt = conn.createStatement();
                stmt.execute("SELECT * FROM " + connectionDialect.escapeName(tableName) + " WHERE ROWNUM = 1");
                rs = stmt.getResultSet();
                if (rs.next()) {
                    return false;
                }
            } finally {
                JdbcUtil.close(stmt, rs);
            }
        }

        return true;
    }

    /**
     * Due to a lot of new DB connections during tests /dev/random on CI servers may run low.
     */
    private void checkOS() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0) {
            // the 3 '/' are important to make it an URL
            System.setProperty("java.security.egd", "file:///dev/urandom");
        }
    }

    private void ensureSchemaEmpty(final Connection conn) throws SQLException {
        String[] schemaNames = getSchemaNames();
        if (!getOrCreateSchemaContext().getService(IConnectionTestDialect.class).isEmptySchema(conn)) {
            truncateMainSchema(conn, schemaNames[0]);
        }
        truncateAdditionalSchemas(conn, schemaNames, false);
    }

    protected void executeAdditionalDataRunnables(final FrameworkMethod frameworkMethod) {
        try {
            ISchemaRunnable[] dataRunnables = getDataRunnables(getTestClass().getJavaClass(), null, frameworkMethod);
            executeWithDeferredConstraints(dataRunnables);
        } catch (Exception e) {
            throw RuntimeExceptionUtil.mask(e);
        }
    }

    private void executeWithDeferredConstraints(final ISchemaRunnable... schemaRunnables) {
        if (schemaRunnables.length == 0) {
            return;
        }
        try {
            Connection conn = getConnection();
            boolean success = false;
            try {
                IStateRollback rollback = getOrCreateSchemaContext().getService(IConnectionDialect.class).disableConstraints(conn, getSchemaNames());
                for (ISchemaRunnable schemaRunnable : schemaRunnables) {
                    schemaRunnable.executeSchemaSql(conn);
                }
                rollback.rollback();
                conn.commit();
                success = true;
            } finally {
                if (!success && !conn.getAutoCommit()) {
                    conn.rollback();
                }
            }
        } catch (Exception e) {
            throw RuntimeExceptionUtil.mask(e);
        }
    }

    @SneakyThrows
    @SuppressWarnings("resource")
    @Override
    protected void extendPropertiesInstance(FrameworkMethod frameworkMethod, Properties props) {
        super.extendPropertiesInstance(frameworkMethod, props);

        var testForkSuffix = props.getString(UtilConfigurationConstants.ForkName);
        if (testForkSuffix != null) {
            var databaseUser = props.getString(PersistenceJdbcConfigurationConstants.DatabaseUser);
            var databaseSchemaName = props.getString(PersistenceJdbcConfigurationConstants.DatabaseSchemaName);
            props.putString(PersistenceJdbcConfigurationConstants.DatabaseUser, databaseUser + "_" + testForkSuffix);
            props.putString(PersistenceJdbcConfigurationConstants.DatabaseSchemaName, databaseSchemaName + "_" + testForkSuffix);
        }
        DialectSelectorModule.fillProperties(props);

        var schemaConnection = connection != null && !connection.isClosed() ? connection : null;
        if (schemaConnection == null || !schemaConnection.isWrapperFor(IPreparedConnectionHolder.class)) {
            return;
        }
        schemaConnection.unwrap(IPreparedConnectionHolder.class).setPreparedConnection(true);
        var preparedConnections = new ArrayList<Connection>(1);
        preparedConnections.add(schemaConnection);
        props.put(PersistenceJdbcConfigurationConstants.PreparedConnectionInstances, preparedConnections);
    }

    @Override
    protected void finalize() throws Throwable {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
        if (schemaContext != null) {
            schemaContext.getRoot().dispose();
            schemaContext = null;
        }
        super.finalize();
    }

    private String[] getConfiguredExternalTableNames(final Class<?> type) {
        String[] configuredSynonymNames;
        var annotations = findAnnotations(type, SQLTableSynonyms.class);
        if (annotations.size() == 1) {
            var annoInfo = annotations.get(0);
            SQLTableSynonyms anno = (SQLTableSynonyms) annoInfo.getAnnotation();
            configuredSynonymNames = anno.value();
        } else {
            configuredSynonymNames = new String[0];
        }

        return configuredSynonymNames;
    }

    @SneakyThrows
    private Connection getConnection() {
        if (connection != null && !connection.isClosed()) {
            return connection;
        }
        Connection conn;
        try {
            conn = getOrCreateSchemaContext().getService(IConnectionFactory.class).create();
        } catch (MaskingRuntimeException e) {
            var cause = e.getCause();
            while (cause instanceof MaskingRuntimeException) {
                cause = cause.getCause();
            }
            var testProps = getOrCreateSchemaContext().getService(IProperties.class);
            testUserHasBeenCreated = getOrCreateSchemaContext().getService(IConnectionTestDialect.class)
                                                               .createTestUserIfSupported(cause, testProps.getString(PersistenceJdbcConfigurationConstants.DatabaseUser),
                                                                       testProps.getString(PersistenceJdbcConfigurationConstants.DatabasePass), testProps);
            if (!testUserHasBeenCreated) {
                throw e;
            }
            conn = getOrCreateSchemaContext().getService(IConnectionFactory.class).create();
        }
        connection = conn;
        return connection;
    }

    protected ISchemaRunnable[] getDataRunnables(final Class<?> callingClass, final Class<?> type, final FrameworkMethod frameworkMethod) {
        var schemaRunnables = new ArrayList<ISchemaRunnable>();

        var annotations = findAnnotations(type, frameworkMethod != null ? frameworkMethod.getMethod() : null, SQLDataList.class, SQLData.class);

        var schemaContext = getOrCreateSchemaContext();
        var connectionDialect = schemaContext.getService(IConnectionDialect.class);
        var properties = schemaContext.getService(IProperties.class);

        var sqlCommands = new ArrayList<String>();
        var sqlToSourceMap = new HashMap<String, String>();

        for (IAnnotationInfo<?> schemaItem : annotations) {
            var annotation = schemaItem.getAnnotation();
            if (annotation instanceof SQLDataList) {
                SQLDataList sqlDataList = (SQLDataList) annotation;

                SQLData[] value = sqlDataList.value();
                for (SQLData sqlData : value) {
                    getSchemaRunnable(schemaContext, sqlData.type(), sqlData.valueProvider(), sqlData.value(), schemaRunnables, schemaItem.getAnnotatedElement(), true, sqlCommands, sqlToSourceMap,
                            connectionDialect, properties, getLog(), doExecuteStrict);
                }
            } else {
                SQLData sqlData = (SQLData) annotation;

                getSchemaRunnable(schemaContext, sqlData.type(), sqlData.valueProvider(), sqlData.value(), schemaRunnables, schemaItem.getAnnotatedElement(), true, sqlCommands, sqlToSourceMap,
                        connectionDialect, properties, getLog(), doExecuteStrict);
            }
        }
        if (sqlCommands.size() != 0) {
            schemaRunnables.add(new ISchemaRunnable() {
                @Override
                public void executeSchemaSql(Connection connection) throws Exception {
                    executeScript(sqlCommands, connection, false, sqlToSourceMap, null, connectionDialect, getLog(), doExecuteStrict);
                }
            });
        }
        return schemaRunnables.toArray(new ISchemaRunnable[schemaRunnables.size()]);
    }

    protected ILogger getLog() {
        return schemaContext.getService(ILoggerCache.class).getCachedLogger(schemaContext, AmbethInformationBusWithPersistenceRunner.class);
    }

    /**
     * Get the already existing schema context or call <code>rebuildSchemaContext</code> to create a
     * new one.
     *
     * @return Schema content
     */
    protected IServiceContext getOrCreateSchemaContext() {
        if (schemaContext == null || schemaContext.isDisposed()) {
            rebuildSchemaContext();
        }
        return schemaContext;
    }

    private String[] getSchemaNames() {
        var schemaContext = getOrCreateSchemaContext();
        var properties = schemaContext.getService(IProperties.class);
        var schemaProperty = (String) properties.get(PersistenceJdbcConfigurationConstants.DatabaseSchemaName);
        var schemaNames = schemaContext.getService(IConnectionDialect.class).toDefaultCase(schemaProperty).split("[:;]");
        return schemaNames;
    }

    protected ISchemaRunnable[] getStructureRunnables(final Class<?> callingClass, final Class<?> type, final IList<String> sqlExecutionOrder) {
        var schemaRunnables = new ArrayList<ISchemaRunnable>();

        var annotations = findAnnotations(type, SQLStructureList.class, SQLStructure.class);

        var schemaContext = getOrCreateSchemaContext();
        var connectionDialect = schemaContext.getService(IConnectionDialect.class);
        var properties = schemaContext.getService(IProperties.class);
        var sqlCommands = new ArrayList<String>();

        var sqlToSourceMap = new HashMap<String, String>();
        for (IAnnotationInfo<?> schemaItem : annotations) {
            var annotation = schemaItem.getAnnotation();
            if (annotation instanceof SQLStructureList) {
                SQLStructureList sqlStructureList = (SQLStructureList) annotation;

                SQLStructure[] value = sqlStructureList.value();
                for (SQLStructure sqlStructure : value) {
                    getSchemaRunnable(schemaContext, sqlStructure.type(), sqlStructure.schemaFileProvider(), sqlStructure.value(), schemaRunnables, schemaItem.getAnnotatedElement(), true, sqlCommands,
                            sqlToSourceMap, connectionDialect, properties, getLog(), doExecuteStrict);
                }
            } else {
                SQLStructure sqlStructure = (SQLStructure) annotation;
                getSchemaRunnable(schemaContext, sqlStructure.type(), sqlStructure.schemaFileProvider(), sqlStructure.value(), schemaRunnables, schemaItem.getAnnotatedElement(), true, sqlCommands,
                        sqlToSourceMap, connectionDialect, properties, getLog(), doExecuteStrict);
            }
        }
        if (sqlCommands.size() != 0) {
            schemaRunnables.add(new ISchemaRunnable() {
                @Override
                public void executeSchemaSql(Connection connection) throws Exception {
                    executeScript(sqlCommands, connection, false, sqlToSourceMap, sqlExecutionOrder, connectionDialect, getLog(), doExecuteStrict);
                }
            });
        }
        return schemaRunnables.toArray(new ISchemaRunnable[schemaRunnables.size()]);
    }

    private boolean hasStructureAnnotation() {
        return !findAnnotations(getTestClass().getJavaClass(), SQLStructureList.class, SQLStructure.class).isEmpty();

    }

    /**
     * @return Flag if data rebuild is demanded (checks the test class annotation or returns the
     * default value).
     */
    private boolean isDataRebuildDemanded() {
        var result = true; // default value if no annotation is found
        var sqlDataRebuilds = findAnnotations(getTestClass().getJavaClass(), SQLDataRebuild.class);
        if (!sqlDataRebuilds.isEmpty()) {
            var topDataRebuild = sqlDataRebuilds.get(sqlDataRebuilds.size() - 1);
            result = ((SQLDataRebuild) topDataRebuild.getAnnotation()).value();
        }
        return result;
    }

    /**
     * @return Flag if a truncate of the data tables (on test class level) is demanded (checks the
     * test class annotation or returns the default value).
     */
    private boolean isTruncateOnClassDemanded() {
        var result = true; // default value
        var sqlDataRebuilds = findAnnotations(getTestClass().getJavaClass(), SQLDataRebuild.class);
        if (!sqlDataRebuilds.isEmpty()) {
            var topDataRebuild = sqlDataRebuilds.get(sqlDataRebuilds.size() - 1);
            if (topDataRebuild.getAnnotation() instanceof SQLDataRebuild) {
                result = ((SQLDataRebuild) topDataRebuild.getAnnotation()).truncateOnClass();
            }
        }
        return result;
    }

    protected void logMeasurement(final String name, final Object value) {
        var elementName = name.replaceAll(" ", "_").replaceAll("\\.", "_").replaceAll("\\(", ":").replaceAll("\\)", ":");
        xmlWriter.writeOpenElement(elementName);
        xmlWriter.writeEscapedXml(value.toString());
        xmlWriter.writeCloseElement(elementName);
    }

    @Override
    protected org.junit.runners.model.Statement methodBlock(final FrameworkMethod frameworkMethod) {
        var statement = super.methodBlock(frameworkMethod);
        return new org.junit.runners.model.Statement() {
            @Override
            public void evaluate() throws Throwable {
                var doContextRebuild = false;
                var method = frameworkMethod.getMethod();
                var doStructureRebuild = !isStructureRebuildAlreadyHandled && hasStructureAnnotation();
                var methodTriggersContextRebuild =
                        method.isAnnotationPresent(TestModule.class) || method.isAnnotationPresent(TestProperties.class) || method.isAnnotationPresent(TestPropertiesList.class);
                doContextRebuild = beanContext == null || beanContext.isDisposed() || doStructureRebuild || methodTriggersContextRebuild || lastMethodTriggersContextRebuild;
                lastMethodTriggersContextRebuild = methodTriggersContextRebuild;
                var doDataRebuild = isDataRebuildDemanded();
                if (!doDataRebuild) // handle the special cases for SQLDataRebuild=false
                {
                    // If SQL data on class level -> run data SQL before the first test method
                    if (!isFirstTestMethodAlreadyExecuted) {
                        doDataRebuild = !findAnnotations(getTestClass().getJavaClass(), SQLDataList.class, SQLData.class).isEmpty();
                    }
                }
                var doAddAdditionalMethodData = false; // Flag if SQL method data should be
                // inserted
                // (without deleting
                // existing database entries)
                if (!doDataRebuild) // included in data rebuild -> only check if data rebuild isn't
                // done
                {
                    doAddAdditionalMethodData = method.isAnnotationPresent(SQLData.class) || method.isAnnotationPresent(SQLDataList.class);
                }

                if (doStructureRebuild) {
                    rebuildStructure();
                }
                if (doDataRebuild) {
                    rebuildData(frameworkMethod);
                }
                if (doAddAdditionalMethodData) {
                    executeAdditionalDataRunnables(frameworkMethod);
                }
                // Do context rebuild after the database changes have been made because the beans
                // may access
                // the data e.g.
                // in their afterStarted method
                isRebuildContextForThisTestRecommended = doContextRebuild;
                isFirstTestMethodAlreadyExecuted = true;
                isRebuildDataForThisTestRecommended = doDataRebuild;

                try {
                    statement.evaluate();
                } catch (MaskingRuntimeException e) {
                    throw RuntimeExceptionUtil.mask(e, Throwable.class); // potentially unwraps
                    // redundant
                    // MaskingRuntimeException
                }
            }
        };
    }

    @Override
    protected org.junit.runners.model.Statement methodInvoker(final FrameworkMethod method, Object test) {
        var parentStatement = AmbethInformationBusWithPersistenceRunner.super.methodInvoker(method, test);
        var statement = new org.junit.runners.model.Statement() {
            @Override
            public void evaluate() throws Throwable {
                var dataSetup = beanContext.getParent().getService(IDataSetup.class, false);
                if (dataSetup != null) {
                    dataSetup.refreshEntityReferences();
                }
                parentStatement.evaluate();
            }
        };
        return new org.junit.runners.model.Statement() {
            @Override
            public void evaluate() throws Throwable {
                if (isRebuildDataForThisTestRecommended) {
                    beanContext.getService(DataSetupExecutor.class).rebuildData();
                    isRebuildDataForThisTestRecommended = false;
                }
                var securityActive = Boolean.parseBoolean(beanContext.getService(IProperties.class).getString(MergeConfigurationConstants.SecurityActive, "false"));
                if (!securityActive) {
                    statement.evaluate();
                    return;
                }

                var changeControllerState = method.getAnnotation(ChangeControllerState.class);

                var changeControllerActiveTest = false;
                var changeController = beanContext.getService(IChangeController.class, false);
                if (changeControllerState != null) {
                    if (changeController != null) {
                        var conversionHelper = beanContext.getService(IConversionHelper.class);
                        var active = conversionHelper.convertValueToType(Boolean.class, changeControllerState.active());
                        if (Boolean.TRUE.equals(active)) {
                            changeControllerActiveTest = true;
                        }
                    }

                }
                var changeControllerActive = changeControllerActiveTest;

                var authentication = method.getAnnotation(TestAuthentication.class);
                if (authentication == null) {
                    var testClass = getTestClass().getJavaClass();
                    authentication = testClass.getAnnotation(TestAuthentication.class);
                }
                if (authentication == null) {
                    statement.evaluate();
                    return;
                }
                var scope = new StringSecurityScope(authentication.scope());

                var behaviour = new IMethodLevelBehavior<SecurityMethodMode>() {
                    private final SecurityMethodMode mode = new SecurityMethodMode(SecurityContextType.AUTHENTICATED, -1, -1, null, -1, scope);

                    @Override
                    public SecurityMethodMode getBehaviourOfMethod(Method method) {
                        return mode;
                    }

                    @Override
                    public SecurityMethodMode getDefaultBehaviour() {
                        return mode;
                    }
                };

                var interceptor = beanContext.registerBean(SecurityFilterInterceptor.class)
                                             .propertyValue(SecurityFilterInterceptor.P_METHOD_LEVEL_BEHAVIOUR, behaviour)
                                             .propertyValue("Target", statement)
                                             .finish();
                var stmt = (org.junit.runners.model.Statement) beanContext.getService(IProxyFactory.class).createProxy(new Class<?>[] { org.junit.runners.model.Statement.class }, interceptor);
                var securityContextHolder = beanContext.getService(ISecurityContextHolder.class);
                var fAuthentication = authentication;
                var rollback = StateRollback.chain(chain -> {
                    chain.append(securityContextHolder.pushAuthentication(new DefaultAuthentication(fAuthentication.name(), fAuthentication.password().toCharArray(), PasswordType.PLAIN)));
                    if (changeControllerActive && changeController != null) {
                        chain.append(changeController.pushRunWithoutEDBL());
                    }
                });
                try {
                    stmt.evaluate();
                } finally {
                    rollback.rollback();
                }
            }
        };
    }

    @Override
    protected void rebuildContext(FrameworkMethod frameworkMethod) {
        if (frameworkMethod == null) {
            return;
        }
        if (isRebuildDataForThisTestRecommended) {
            var oldValue = DataSetupExecutor.setAutoRebuildData(Boolean.TRUE);
            try {
                super.rebuildContext(frameworkMethod);
            } finally {
                DataSetupExecutor.setAutoRebuildData(oldValue);
                isRebuildDataForThisTestRecommended = false;
            }
        } else {
            super.rebuildContext(frameworkMethod);
        }
        try {
            if (connection != null && !connection.isClosed()) {
                connection.rollback();
            }
        } catch (Exception e) {
            throw RuntimeExceptionUtil.mask(e);
        }
        beanContext.getService(ILightweightTransaction.class).runInTransaction(() -> {
            // Intended blank
        });
    }

    @Override
    protected void rebuildContextDetails(final IBeanContextFactory childContextFactory) {
        super.rebuildContextDetails(childContextFactory);

        childContextFactory.registerBean(MEASUREMENT_BEAN, Measurement.class).propertyValue("TestClassName", getTestClass().getJavaClass()).autowireable(IMeasurement.class);
    }

    @SneakyThrows
    public void rebuildData() {
        rebuildData(null);
    }

    protected void rebuildData(final FrameworkMethod frameworkMethod) throws SQLException {
        var callingClass = getTestClass().getJavaClass();
        var conn = getConnection();

        truncateAllTablesBySchema(conn, getSchemaNames());
        truncateAllTablesExplicitlyGiven(conn, getConfiguredExternalTableNames(callingClass));

        var dataRunnables = getDataRunnables(callingClass, callingClass, frameworkMethod);
        executeWithDeferredConstraints(dataRunnables);
    }

    public void rebuildSchemaContext() {
        if (schemaContext != null) {
            schemaContext.getRoot().dispose();
            schemaContext = null;
        }
        Properties.resetApplication();

        var rollback = Properties.pushSystemOutStream(NullPrintStream.INSTANCE);
        try {
            Properties.loadBootstrapPropertyFile();
        } finally {
            rollback.rollback();
        }

        Properties baseProps = new Properties(Properties.getApplication());
        // baseProps.putString("ambeth.log.level", "WARN");

        extendPropertiesInstance(null, baseProps);

        IServiceContext schemaBootstrapContext = null;
        boolean success = false;
        try {
            schemaBootstrapContext = BeanContextFactory.createBootstrap(baseProps);
            schemaContext = schemaBootstrapContext.createService(AmbethPersistenceSchemaModule.class);
            success = true;
        } finally {
            if (!success && schemaBootstrapContext != null) {
                schemaBootstrapContext.dispose();
            }
        }
    }

    @SneakyThrows
    public void rebuildStructure() {
        var callingClass = getTestClass().getJavaClass();
        var connection = getConnection();

        var oldAutoCommit = connection.getAutoCommit();
        if (!oldAutoCommit) {
            connection.setAutoCommit(true);
        }
        try {
            ensureSchemaEmpty(connection);

            var schemaContext = getOrCreateSchemaContext();
            schemaContext.getService(IConnectionTestDialect.class).preStructureRebuild(connection);

            var sqlExecutionOrder = new ArrayList<String>();

            var structureRunnables = getStructureRunnables(callingClass, callingClass, sqlExecutionOrder);
            for (var structRunnable : structureRunnables) {
                structRunnable.executeSchemaSql(connection);
            }
            var connectionDialect = schemaContext.getService(IConnectionDialect.class);
            var connectionTestDialect = schemaContext.getService(IConnectionTestDialect.class);

            ensureExistenceOfNeededDatabaseObjects(connection, sqlExecutionOrder, connectionDialect, connectionTestDialect, getLog(), doExecuteStrict);
            AmbethInformationBusWithPersistenceRunner.sqlExecutionOrder = sqlExecutionOrder.toArray(String.class);
        } finally {
            if (!oldAutoCommit) {
                try {
                    connection.setAutoCommit(false);
                } catch (SQLException e) {
                    // Intended blank
                }
            }
        }
        isStructureRebuildAlreadyHandled = true;
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        isRebuildDataForThisTestRecommended = false;
        super.runChild(method, notifier);
    }

    public void setDoExecuteStrict(final boolean doExecuteStrict) {
        this.doExecuteStrict = doExecuteStrict;
    }

    private void truncateAdditionalSchemas(final Connection conn, String[] schemaNames, boolean skipEmptyCheck) throws SQLException {
        truncateAllTablesExplicitlyGiven(conn, getConfiguredExternalTableNames(getTestClass().getJavaClass()));

        if (schemaNames != null) {
            var truncateOnClassDemanded = isTruncateOnClassDemanded();
            for (int i = schemaNames.length; i-- > 1; ) {
                var schemaName = schemaNames[i];
                if (skipEmptyCheck || !checkAdditionalSchemaEmpty(conn, schemaName)) {
                    if (truncateOnClassDemanded) {
                        truncateAllTablesBySchema(conn, schemaName);
                    }
                }
            }
        }
    }

    /**
     * Delete the content from all tables within the given schema.
     *
     * @param conn        SQL connection
     * @param schemaNames Schema names to use
     * @throws SQLException
     */
    protected void truncateAllTablesBySchema(final Connection conn, final String... schemaNames) throws SQLException {
        var connectionDialect = getOrCreateSchemaContext().getService(IConnectionDialect.class);
        var allTableNames = connectionDialect.getAllFullqualifiedTableNames(conn, schemaNames);
        if (allTableNames.isEmpty()) {
            return;
        }
        var sql = new ArrayList<String>();

        for (int i = allTableNames.size(); i-- > 0; ) {
            var tableName = allTableNames.get(i);
            sql.add(connectionDialect.buildClearTableSQL(tableName));
        }
        executeWithDeferredConstraints(new ISchemaRunnable() {

            @Override
            public void executeSchemaSql(final Connection connection) throws Exception {
                executeScript(sql, connection, false, null, null, connectionDialect, getLog(), doExecuteStrict);
                sql.clear();
            }
        });
    }

    /**
     * Delete the content from the given tables.
     *
     * @param conn               SQL connection
     * @param explicitTableNames Table name with schema (or synonym)
     * @throws SQLException
     */
    protected void truncateAllTablesExplicitlyGiven(final Connection conn, final String[] explicitTableNames) throws SQLException {
        if (explicitTableNames == null || explicitTableNames.length == 0) {
            return;
        }
        var connectionDialect = getOrCreateSchemaContext().getService(IConnectionDialect.class);
        var sql = new ArrayList<String>();
        for (int i = explicitTableNames.length; i-- > 0; ) {
            var tableName = explicitTableNames[i];
            sql.add(connectionDialect.buildClearTableSQL(tableName));
        }
        executeWithDeferredConstraints(new ISchemaRunnable() {

            @Override
            public void executeSchemaSql(final Connection connection) throws Exception {
                executeScript(sql, connection, false, null, null, connectionDialect, getLog(), doExecuteStrict);
                sql.clear();
            }
        });
    }

    private void truncateMainSchema(final Connection conn, String mainSchemaName) throws SQLException {
        if (hasStructureAnnotation()) {
            getOrCreateSchemaContext().getService(IConnectionTestDialect.class).dropAllSchemaContent(conn, mainSchemaName);
        } else {
            if (isTruncateOnClassDemanded()) {
                truncateAllTablesBySchema(conn, null, mainSchemaName);
            }
        }
    }

    @Override
    protected org.junit.runners.model.Statement withAfterClasses(final org.junit.runners.model.Statement statement) {
        var resultStatement = super.withAfterClasses(statement);
        return new org.junit.runners.model.Statement() {
            @Override
            public void evaluate() throws Throwable {
                resultStatement.evaluate();
                try {
                    try {
                        // After all test methods of the test class have been executed we probably
                        // have to
                        // delete the
                        // test data
                        var conn = getConnection();

                        if (testUserHasBeenCreated) {
                            JdbcUtil.close(connection);
                            connection = null;
                            var testProps = getOrCreateSchemaContext().getService(IProperties.class);
                            getOrCreateSchemaContext().getService(IConnectionTestDialect.class)
                                                      .dropCreatedTestUser(testProps.getString(PersistenceJdbcConfigurationConstants.DatabaseUser),
                                                              testProps.getString(PersistenceJdbcConfigurationConstants.DatabasePass), testProps);
                            testUserHasBeenCreated = false;
                        } else {
                            var schemaNames = getSchemaNames();
                            truncateMainSchema(conn, schemaNames[0]);
                            truncateAdditionalSchemas(conn, schemaNames, true);
                        }
                    } finally {
                        JdbcUtil.close(connection);
                        connection = null;
                        if (schemaContext != null) {
                            schemaContext.getRoot().dispose();
                            schemaContext = null;
                        }
                    }
                } catch (Exception e) {
                    throw RuntimeExceptionUtil.mask(e);
                }
            }
        };
    }

    @Override
    protected org.junit.runners.model.Statement withBeforeClasses(org.junit.runners.model.Statement statement) {
        checkOS();
        return super.withBeforeClasses(new org.junit.runners.model.Statement() {
            @Override
            public void evaluate() throws Throwable {
                if (System.getProperties().getProperty(PersistenceJdbcConfigurationConstants.DatabaseConnection) == null) {
                    if (jdbcDatabaseContainer == null) {
                        jdbcDatabaseContainer = new PostgreSQLContainer<>("postgres:15-alpine");
                        jdbcDatabaseContainer.start();
                    }
                }
                try {
                    if (jdbcDatabaseContainer != null) {
                        System.getProperties().put(PersistenceJdbcConfigurationConstants.DatabaseConnection, jdbcDatabaseContainer.getJdbcUrl());
                        System.getProperties().put(PersistenceJdbcConfigurationConstants.DatabaseName, jdbcDatabaseContainer.getDatabaseName());
                        System.getProperties().put(PersistenceJdbcConfigurationConstants.DatabaseUser, jdbcDatabaseContainer.getUsername());
                        System.getProperties().put(PersistenceJdbcConfigurationConstants.DatabasePass, jdbcDatabaseContainer.getPassword());
                        DatabaseProtocolResolver.enrichWithDatabaseProtocol(System.getProperties());
                    }
                    statement.evaluate();
                } finally {
                    if (jdbcDatabaseContainer != null) {
                        jdbcDatabaseContainer.close();
                        jdbcDatabaseContainer = null;
                    }
                }
            }
        });
    }
}
