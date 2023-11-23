package com.koch.ambeth.informationbus.persistence.setup;

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
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.factory.BeanContextFactory;
import com.koch.ambeth.ioc.log.ILoggerCache;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.log.io.FileUtil;
import com.koch.ambeth.merge.util.setup.SetupModule;
import com.koch.ambeth.persistence.IConnectionDialect;
import com.koch.ambeth.persistence.connection.IPreparedConnectionHolder;
import com.koch.ambeth.persistence.jdbc.IConnectionFactory;
import com.koch.ambeth.persistence.jdbc.IConnectionTestDialect;
import com.koch.ambeth.persistence.jdbc.JdbcUtil;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.persistence.jdbc.connector.DialectSelectorModule;
import com.koch.ambeth.util.annotation.AnnotationInfo;
import com.koch.ambeth.util.annotation.IAnnotationInfo;
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
import com.koch.ambeth.util.state.IStateRollback;
import com.koch.ambeth.util.state.StateRollback;
import jakarta.persistence.PersistenceException;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class AmbethPersistenceSetup implements Closeable {
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

    private static void ensureExistanceOfNeededDatabaseObjects(final Connection conn, List<String> sqlExecutionOrder, IConnectionDialect connectionDialect,
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
                        // for the same command depending on the state of the schema - but we want the FIRST
                        // exception)
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
            for (var schemaFile : schemaFiles) {
                if (schemaFile == null || schemaFile.length() == 0) {
                    continue;
                }
                var sql = readSqlFile(schemaFile, callingClass, properties, log);
                allSQL.addAll(sql);
                for (var oneSql : sql) {
                    sqlToSourceMap.put(oneSql, schemaFile);
                }
            }
        } catch (Exception e) {
            throw RuntimeExceptionUtil.mask(e);
        }
    }

    private static void handleSqlCommand(String command, final Statement stmt, final Map<String, Object> defaultOptions, IConnectionDialect connectionDialect) throws SQLException {
        Map<String, Object> options = defaultOptions;
        Matcher optionLine = AmbethPersistenceSetup.optionLine.matcher(command.trim());
        if (optionLine.find()) {
            options = new HashMap<>(defaultOptions);
            String optionString = optionLine.group(1).replace(" ", "");
            String[] preSqls = optionSeparator.split(optionString);
            command = lineSeparator.split(command, 2)[1];
            Object value;
            for (int i = preSqls.length; i-- > 0; ) {
                Matcher keyValue = optionPattern.matcher(preSqls[i]);
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
        File tempFile = new File(fileName);
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
            String relativePath = fileName.startsWith("/") ? "." + fileName : callingNamespace.replace(".", File.separator) + File.separator + fileName;
            String forwardPath = relativePath.replace('\\', '/');
            String[] classPaths = pathSeparator.split(System.getProperty("java.class.path"));
            for (int i = 0; i < classPaths.length; i++) {
                File classpathEntry = new File(classPaths[i]);
                if (classpathEntry.isDirectory()) {
                    tempFile = new File(classPaths[i], relativePath);
                    if (tempFile.canRead()) {
                        sqlFile = tempFile;
                        break;
                    }
                } else if (classpathEntry.isFile()) {
                    boolean keepOpen = false;
                    ZipInputStream jis = new ZipInputStream(new FileInputStream(classpathEntry));
                    try {
                        ZipEntry jarEntry;
                        while ((jarEntry = jis.getNextEntry()) != null) {
                            String jarEntryName = jarEntry.getName();
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
                Pattern fileSuffixPattern = Pattern.compile(".+\\.(?:[^\\.]*)");
                Matcher matcher = fileSuffixPattern.matcher(relativePath);
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
                    String error = "Cannot find '" + relativePath + "' in class path:" + nl;
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
            var ignoreThisCommand = false;
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
                        var toCut = 1; // Trailing space
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

    @SuppressWarnings("unchecked")
    protected static List<IAnnotationInfo<?>> findAnnotations(Class<?> type, Method method, Class<?>... annotationTypes) {
        var targetList = new ArrayList<IAnnotationInfo<?>>();
        findAnnotations(type, targetList, true, annotationTypes);

        if (method != null) {
            for (var annotationType : annotationTypes) {
                var annotation = method.getAnnotation((Class<? extends Annotation>) annotationType);
                if (annotation != null) {
                    targetList.add(new AnnotationInfo<>(annotation, method));
                }
            }
        }
        return targetList;
    }

    @SuppressWarnings("unchecked")
    protected static void findAnnotations(Class<?> type, List<IAnnotationInfo<?>> targetList, boolean isFirst, Class<?>... annotationTypes) {
        if (type == null || Object.class.equals(type)) {
            return;
        }
        if (!type.isInterface()) {
            findAnnotations(type.getSuperclass(), targetList, false, annotationTypes);
        }
        for (Class<?> annotationType : annotationTypes) {
            Annotation annotation = type.getAnnotation((Class<? extends Annotation>) annotationType);
            if (annotation != null) {
                targetList.add(new AnnotationInfo<>(annotation, type));
            }
        }
        if (isFirst) {
            Class<?>[] interfaces = type.getInterfaces();
            for (Class<?> currInterface : interfaces) {
                for (Class<?> annotationType : annotationTypes) {
                    Annotation annotationOfInterface = currInterface.getAnnotation((Class<? extends Annotation>) annotationType);
                    if (annotationOfInterface != null) {
                        targetList.add(new AnnotationInfo<>(annotationOfInterface, currInterface));
                    }
                }
            }
        }
    }

    protected boolean doExecuteStrict = false;
    protected boolean testUserHasBeenCreated;
    protected Class<?> testClass;
    private Connection connection;
    private IServiceContext schemaContext;
    private Properties props = new Properties();

    public AmbethPersistenceSetup(final Class<?> testClass) {
        this.testClass = testClass;
    }

    public Class<?> getTestClass() {
        return testClass;
    }

    protected List<Class<? extends IInitializingModule>> buildFrameworkTestModuleList() {
        List<Class<? extends IInitializingModule>> frameworkTestModuleList = new ArrayList<>();
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

    private void ensureSchemaEmpty(final Connection conn) throws SQLException {
        String[] schemaNames = getSchemaNames();
        if (!getOrCreateSchemaContext().getService(IConnectionTestDialect.class).isEmptySchema(conn)) {
            truncateMainSchema(conn, schemaNames[0]);
        }
        truncateAdditionalSchemas(conn, schemaNames, false);
    }

    protected void executeAdditionalDataRunnables(final Method frameworkMethod) {
        try {
            ISchemaRunnable[] dataRunnables = getDataRunnables(getTestClass(), null, frameworkMethod);
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
                if (!success) {
                    if (!conn.getAutoCommit()) {
                        conn.rollback();
                    }
                }
            }
        } catch (Exception e) {
            throw RuntimeExceptionUtil.mask(e);
        }
    }

    @SneakyThrows
    @SuppressWarnings("resource")
    protected void extendPropertiesInstance(Method frameworkMethod, Properties props) {
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

    @SneakyThrows
    @Override
    public void close() throws IOException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
        if (schemaContext != null) {
            schemaContext.close();
            schemaContext = null;
        }
    }

    private String[] getConfiguredExternalTableNames(final Class<?> type) {
        String[] configuredSynonymNames;
        List<IAnnotationInfo<?>> annotations = findAnnotations(type, SQLTableSynonyms.class);
        if (annotations.size() == 1) {
            IAnnotationInfo<?> annoInfo = annotations.get(0);
            SQLTableSynonyms anno = (SQLTableSynonyms) annoInfo.getAnnotation();
            configuredSynonymNames = anno.value();
        } else {
            configuredSynonymNames = new String[0];
        }

        return configuredSynonymNames;
    }

    private Connection getConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return connection;
        }
        Connection conn;
        try {
            conn = getOrCreateSchemaContext().getService(IConnectionFactory.class).create();
        } catch (Throwable e) {
            Throwable cause = e;
            while (cause instanceof MaskingRuntimeException) {
                cause = cause.getCause();
            }
            IProperties testProps = getOrCreateSchemaContext().getService(IProperties.class);
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

    protected ISchemaRunnable[] getDataRunnables(final Class<?> callingClass, final Class<?> type, final Method frameworkMethod) {
        List<ISchemaRunnable> schemaRunnables = new ArrayList<>();

        List<IAnnotationInfo<?>> annotations = findAnnotations(type, frameworkMethod, SQLDataList.class, SQLData.class);

        IServiceContext schemaContext = getOrCreateSchemaContext();
        final IConnectionDialect connectionDialect = schemaContext.getService(IConnectionDialect.class);
        IProperties properties = schemaContext.getService(IProperties.class);

        final IList<String> sqlCommands = new ArrayList<>();
        final IMap<String, String> sqlToSourceMap = new HashMap<>();

        for (IAnnotationInfo<?> schemaItem : annotations) {
            Annotation annotation = schemaItem.getAnnotation();
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
        return schemaContext.getService(ILoggerCache.class).getCachedLogger(schemaContext, AmbethPersistenceSetup.class);
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

    public void rebuildSchemaContext() {
        if (schemaContext != null) {
            schemaContext.getRoot().dispose();
            schemaContext = null;
        }
        Properties baseProps = new Properties(props);
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

    private String[] getSchemaNames() {
        IServiceContext schemaContext = getOrCreateSchemaContext();
        IProperties properties = schemaContext.getService(IProperties.class);
        String schemaProperty = (String) properties.get(PersistenceJdbcConfigurationConstants.DatabaseSchemaName);
        String[] schemaNames = schemaContext.getService(IConnectionDialect.class).toDefaultCase(schemaProperty).split("[:;]");
        return schemaNames;
    }

    // protected org.junit.runners.model.Statement methodInvoker(final FrameworkMethod method,
    // Object test) {
    // final org.junit.runners.model.Statement parentStatement =
    // AmbethPersistenceSetup.super.methodInvoker(method, test);
    // final org.junit.runners.model.Statement statement = new org.junit.runners.model.Statement() {
    // @Override
    // public void evaluate() throws Throwable {
    // IDataSetup dataSetup = beanContext.getParent().getService(IDataSetup.class, false);
    // if (dataSetup != null) {
    // dataSetup.refreshEntityReferences();
    // }
    // parentStatement.evaluate();
    // }
    // };
    // return new org.junit.runners.model.Statement() {
    // @Override
    // public void evaluate() throws Throwable {
    // beanContext.getService(DataSetupExecutor.class).rebuildData();
    // boolean securityActive = Boolean.parseBoolean(beanContext.getService(IProperties.class)
    // .getString(MergeConfigurationConstants.SecurityActive, "false"));
    // if (!securityActive) {
    // statement.evaluate();
    // return;
    // }
    //
    // ChangeControllerState changeControllerState =
    // method.getAnnotation(ChangeControllerState.class);
    //
    // boolean changeControllerActiveTest = false;
    // final IChangeController changeController =
    // beanContext.getService(IChangeController.class, false);
    // if (changeControllerState != null) {
    // if (changeController != null) {
    // IConversionHelper conversionHelper = beanContext.getService(IConversionHelper.class);
    // Boolean active =
    // conversionHelper.convertValueToType(Boolean.class, changeControllerState.active());
    // if (Boolean.TRUE.equals(active)) {
    // changeControllerActiveTest = true;
    // }
    // }
    //
    // }
    // final boolean changeControllerActive = changeControllerActiveTest;
    //
    // TestAuthentication authentication = method.getAnnotation(TestAuthentication.class);
    // if (authentication == null) {
    // Class<?> testClass = getTestClass().getJavaClass();
    // authentication = testClass.getAnnotation(TestAuthentication.class);
    // }
    // if (authentication == null) {
    // statement.evaluate();
    // return;
    // }
    // final ISecurityScope scope = new StringSecurityScope(authentication.scope());
    //
    // IMethodLevelBehavior<SecurityMethodMode> behaviour =
    // new IMethodLevelBehavior<SecurityMethodMode>() {
    // private final SecurityMethodMode mode = new SecurityMethodMode(
    // SecurityContextType.AUTHENTICATED, -1, -1, null, -1, scope);
    //
    // @Override
    // public SecurityMethodMode getBehaviourOfMethod(Method method) {
    // return mode;
    // }
    //
    // @Override
    // public SecurityMethodMode getDefaultBehaviour() {
    // return mode;
    // }
    // };
    //
    // SecurityFilterInterceptor interceptor =
    // beanContext.registerBean(SecurityFilterInterceptor.class)
    // .propertyValue(SecurityFilterInterceptor.P_METHOD_LEVEL_BEHAVIOUR,
    // behaviour).propertyValue("Target", statement)
    // .finish();
    // org.junit.runners.model.Statement stmt =
    // (org.junit.runners.model.Statement) beanContext.getService(IProxyFactory.class)
    // .createProxy(new Class<?>[] {org.junit.runners.model.Statement.class}, interceptor);
    // final org.junit.runners.model.Statement fStatement = stmt;
    // ISecurityContextHolder securityContextHolder =
    // beanContext.getService(ISecurityContextHolder.class);
    // securityContextHolder.setScopedAuthentication(
    // new DefaultAuthentication(authentication.name(),
    // authentication.password().toCharArray(), PasswordType.PLAIN),
    // new CheckedSupplier<Object>() {
    // @Override
    // public Object invoke() throws Exception {
    // IStateRollback rollback = NoOpStateRollback.instance;
    // if (changeControllerActive && changeController != null) {
    // rollback = changeController.pushRunWithoutEDBL();
    // }
    // try {
    // fStatement.evaluate();
    // }
    // finally {
    // rollback.rollback();
    // }
    // return null;
    //
    // }
    // });
    // }
    // };
    // }

    protected ISchemaRunnable[] getStructureRunnables(final Class<?> callingClass, final Class<?> type, final IList<String> sqlExecutionOrder) {
        var schemaRunnables = new ArrayList<ISchemaRunnable>();

        var annotations = findAnnotations(type, SQLStructureList.class, SQLStructure.class);

        var schemaContext = getOrCreateSchemaContext();
        var connectionDialect = schemaContext.getService(IConnectionDialect.class);
        var properties = schemaContext.getService(IProperties.class);
        var sqlCommands = new ArrayList<String>();

        var sqlToSourceMap = new HashMap<String, String>();
        for (var schemaItem : annotations) {
            var annotation = schemaItem.getAnnotation();
            if (annotation instanceof SQLStructureList) {
                var sqlStructureList = (SQLStructureList) annotation;

                var value = sqlStructureList.value();
                for (var sqlStructure : value) {
                    getSchemaRunnable(schemaContext, sqlStructure.type(), sqlStructure.schemaFileProvider(), sqlStructure.value(), schemaRunnables, schemaItem.getAnnotatedElement(), true, sqlCommands,
                            sqlToSourceMap, connectionDialect, properties, getLog(), doExecuteStrict);
                }
            } else {
                var sqlStructure = (SQLStructure) annotation;
                getSchemaRunnable(schemaContext, sqlStructure.type(), sqlStructure.schemaFileProvider(), sqlStructure.value(), schemaRunnables, schemaItem.getAnnotatedElement(), true, sqlCommands,
                        sqlToSourceMap, connectionDialect, properties, getLog(), doExecuteStrict);
            }
        }
        if (sqlCommands.size() != 0) {
            schemaRunnables.add(connection -> executeScript(sqlCommands, connection, false, sqlToSourceMap, sqlExecutionOrder, connectionDialect, getLog(), doExecuteStrict));
        }
        return schemaRunnables.toArray(new ISchemaRunnable[schemaRunnables.size()]);
    }

    private boolean hasStructureAnnotation() {
        return !findAnnotations(getTestClass(), SQLStructureList.class, SQLStructure.class).isEmpty();

    }

    // protected void rebuildContext(Method method) {
    // DataSetupExecutor.setAutoRebuildData(Boolean.TRUE);
    // try {
    // super.rebuildContext(method);
    // }
    // finally {
    // DataSetupExecutor.setAutoRebuildData(null);
    // }
    // try {
    // if (connection != null && !connection.isClosed()) {
    // connection.rollback();
    // }
    // }
    // catch (Throwable e) {
    // throw RuntimeExceptionUtil.mask(e);
    // }
    // }

    /**
     * @return Flag if a truncate of the data tables (on test class level) is demanded (checks the
     * test class annotation or returns the default value).
     */
    private boolean isTruncateOnClassDemanded() {
        return true;
    }

    // public void rebuildSchemaContext() {
    // if (schemaContext != null) {
    // schemaContext.getRoot().dispose();
    // schemaContext = null;
    // }
    // Properties.resetApplication();
    //
    // PrintStream oldPrintStream = System.out;
    // System.setOut(NullPrintStream.INSTANCE);
    // try {
    // Properties.loadBootstrapPropertyFile();
    // }
    // finally {
    // System.setOut(oldPrintStream);
    // }
    //
    // Properties baseProps = new Properties(Properties.getApplication());
    // // baseProps.putString("ambeth.log.level", "WARN");
    //
    // extendPropertiesInstance(null, baseProps);
    //
    // IServiceContext schemaBootstrapContext = null;
    // boolean success = false;
    // try {
    // schemaBootstrapContext = BeanContextFactory.createBootstrap(baseProps);
    // schemaContext = schemaBootstrapContext.createService(AmbethPersistenceSchemaModule.class);
    // success = true;
    // }
    // finally {
    // if (!success && schemaBootstrapContext != null) {
    // schemaBootstrapContext.dispose();
    // }
    // }
    // }

    public void executeSetup(final Method method) {
        boolean doStructureRebuild = hasStructureAnnotation();
        boolean doAddAdditionalMethodData = false; // Flag if SQL method data should be
        // inserted (without deleting existing database entries)

        if (doStructureRebuild) {
            rebuildStructure();
        }
        rebuildData(method);
        if (doAddAdditionalMethodData) {
            executeAdditionalDataRunnables(method);
        }
    }

    @SneakyThrows
    protected void rebuildData(final Method frameworkMethod) {
        var callingClass = getTestClass();
        var conn = getConnection();

        truncateAllTablesBySchema(conn, getSchemaNames());
        truncateAllTablesExplicitlyGiven(conn, getConfiguredExternalTableNames(callingClass));

        var dataRunnables = getDataRunnables(callingClass, callingClass, frameworkMethod);
        executeWithDeferredConstraints(dataRunnables);
    }

    @SneakyThrows
    protected IStateRollback pushAutoCommit(Connection conn, boolean autoCommit) {
        var oldAutoCommit = conn.getAutoCommit();
        if (oldAutoCommit == autoCommit) {
            return StateRollback.empty();
        }
        setAutoCommit(conn, autoCommit);
        return () -> setAutoCommit(conn, oldAutoCommit);
    }

    protected void setAutoCommit(Connection conn, boolean autoCommit) {
        try {
            conn.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            // Intended blank
        }
    }

    @SneakyThrows
    public void rebuildStructure() {
        var callingClass = getTestClass();
        var rollback = FileUtil.pushCurrentTypeScope(callingClass);
        try {
            var connection = getConnection();

            rollback = StateRollback.prepend(pushAutoCommit(connection, true), rollback);
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

            ensureExistanceOfNeededDatabaseObjects(connection, sqlExecutionOrder, connectionDialect, connectionTestDialect, getLog(), doExecuteStrict);
            AmbethPersistenceSetup.sqlExecutionOrder = sqlExecutionOrder.toArray(String.class);
        } finally {
            rollback.rollback();
        }
    }

    public void setDoExecuteStrict(final boolean doExecuteStrict) {
        this.doExecuteStrict = doExecuteStrict;
    }

    private void truncateAdditionalSchemas(final Connection conn, String[] schemaNames, boolean skipEmptyCheck) throws SQLException {
        truncateAllTablesExplicitlyGiven(conn, getConfiguredExternalTableNames(getTestClass()));

        if (schemaNames != null) {
            boolean truncateOnClassDemanded = isTruncateOnClassDemanded();
            for (int i = schemaNames.length; i-- > 1; ) {
                String schemaName = schemaNames[i];
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
        final IConnectionDialect connectionDialect = getOrCreateSchemaContext().getService(IConnectionDialect.class);
        List<String> allTableNames = connectionDialect.getAllFullqualifiedTableNames(conn, schemaNames);
        if (allTableNames.isEmpty()) {
            return;
        }
        final List<String> sql = new ArrayList<>();

        for (int i = allTableNames.size(); i-- > 0; ) {
            String tableName = allTableNames.get(i);
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
        final IConnectionDialect connectionDialect = getOrCreateSchemaContext().getService(IConnectionDialect.class);
        final List<String> sql = new ArrayList<>();
        for (int i = explicitTableNames.length; i-- > 0; ) {
            String tableName = explicitTableNames[i];
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

    protected List<IAnnotationInfo<?>> findAnnotations(Class<?> type, Class<?>... annotationTypes) {
        return findAnnotations(type, null, annotationTypes);
    }

    public AmbethPersistenceSetup withProperties(IProperties props) {
        this.props.load(props);
        return this;
    }
}
