package com.koch.ambeth.persistence.pg;

import com.koch.ambeth.ioc.IocModule;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.factory.BeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.persistence.PermissionGroup;
import com.koch.ambeth.persistence.api.sql.ISqlBuilder;
import com.koch.ambeth.persistence.jdbc.AbstractConnectionTestDialect;
import com.koch.ambeth.persistence.jdbc.JdbcUtil;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.persistence.pg.RandomUserScript.RandomUserModule;
import com.koch.ambeth.util.appendable.AppendableStringBuilder;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.config.IProperties;
import lombok.SneakyThrows;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Pattern;

public class PostgresTestDialect extends AbstractConnectionTestDialect {
    public static final String ROOT_DATABASE_USER = "ambeth.root.database.user";

    public static final String ROOT_DATABASE_PASS = "ambeth.root.database.pass";
    protected final HashSet<String> ignoredTables = new HashSet<>();
    @Autowired
    protected ISqlBuilder sqlBuilder;

    @Property(name = ROOT_DATABASE_USER, defaultValue = "postgres")
    protected String rootDatabaseUser;

    @Property(name = ROOT_DATABASE_PASS, defaultValue = "developer")
    protected String rootDatabasePass;

    @Property(name = PersistenceJdbcConfigurationConstants.DatabaseName, mandatory = false)
    protected String databaseName;

    @Property(name = PersistenceJdbcConfigurationConstants.DatabaseSchemaName)
    protected String schemaName;

    protected String[] schemaNames;
    @LogInstance
    private ILogger log;

    @Override
    public void afterPropertiesSet() throws Throwable {
        super.afterPropertiesSet();
        schemaNames = connectionDialect.toDefaultCase(schemaName).split("[:;]");
    }

    @Override
    public boolean createTestUserIfSupported(Throwable reason, String userName, String userPassword, IProperties testProps) {
        if (!(reason instanceof SQLException)) {
            return false;
        }
        if (!"28P01".equals(((SQLException) reason).getSQLState()) // INVALID PASSWORD, FATAL: password
                // authentication failed for user
                // "xxx"
                && !"3D000".equals(((SQLException) reason).getSQLState()) // FATAL: database "xxx" does not
                // exist
                && !"28000".equals(((SQLException) reason).getSQLState()) // INVALID AUTHORIZATION
            // SPECIFICATION undefined role
            // tried to access the server
        ) {
            return false;
        }
        // try to recover by trying to create the necessary user with the default credentials of sys
        var createUserProps = new Properties(testProps);
        createUserProps.put(RandomUserScript.SCRIPT_IS_CREATE, "true");
        createUserProps.put(RandomUserScript.SCRIPT_DATABASE_NAME, databaseName);
        createUserProps.put(RandomUserScript.SCRIPT_USER_NAME, userName);
        createUserProps.put(RandomUserScript.SCRIPT_USER_PASS, userPassword);

        createUserProps.put(PersistenceJdbcConfigurationConstants.DatabaseUser, rootDatabaseUser);
        createUserProps.put(PersistenceJdbcConfigurationConstants.DatabasePass, rootDatabasePass);
        var bootstrapContext = BeanContextFactory.createBootstrap(createUserProps);
        try {
            bootstrapContext.createService("randomUser", RandomUserModule.class, IocModule.class);
        } finally {
            bootstrapContext.dispose();
        }
        return true;
    }

    @Override
    public void dropCreatedTestUser(String userName, String userPassword, IProperties testProps) {
        var createUserProps = new Properties(testProps);
        createUserProps.put(RandomUserScript.SCRIPT_IS_CREATE, "false");
        createUserProps.put(RandomUserScript.SCRIPT_USER_NAME, userName);
        createUserProps.put(RandomUserScript.SCRIPT_USER_PASS, userPassword);

        createUserProps.put(PersistenceJdbcConfigurationConstants.DatabaseUser, rootDatabaseUser);
        createUserProps.put(PersistenceJdbcConfigurationConstants.DatabasePass, rootDatabasePass);
        var bootstrapContext = BeanContextFactory.createBootstrap(createUserProps);
        try {
            bootstrapContext.createService("randomUser", RandomUserModule.class, IocModule.class);
        } finally {
            bootstrapContext.dispose();
        }
    }

    @SneakyThrows
    @Override
    public void preStructureRebuild(Connection connection) {
        super.preStructureRebuild(connection);

        try (var stm = connection.createStatement()) {
            for (var schemaName : schemaNames) {
                stm.execute("CREATE SCHEMA IF NOT EXISTS \"" + schemaName + "\"");
                // stm.execute("CREATE DOMAIN \"" + schemaName + "\".lo AS oid");
                try {
                    stm.execute("CREATE EXTENSION IF NOT EXISTS lo SCHEMA \"" + schemaName + "\"");
                } catch (Exception e) {
                    if (log.isWarnEnabled()) {
                        log.warn("LOB extension could not be initialized. Later DDLs containing BLOB or CLOB columns will fail", e);
                    }
                }
            }
            stm.execute("SET SCHEMA '" + schemaNames[0] + "'");
        }
    }

    @Override
    public void preProcessConnectionForTest(Connection connection, String[] schemaNames, boolean forcePreProcessing) {
        // intended blank
    }

    @SneakyThrows
    @Override
    public boolean isEmptySchema(Connection connection) {
        try (var stmt = connection.createStatement();
             var rs = stmt.executeQuery("SELECT count(*) FROM pg_class c INNER JOIN pg_namespace n ON c.relnamespace=n.oid WHERE n.nspname='" + schemaNames[0] + "'")) {
            rs.next();
            return rs.getInt(1) == 0;
        }
    }

    @Override
    public String[] createAdditionalTriggers(Connection connection, String fqTableName) {
        var allFieldsOfTable = connectionDialect.getAllFieldsOfTable(connection, fqTableName);
        var sql = new ArrayList<String>();
        var schemaAndTableName = sqlBuilder.getSchemaAndTableName(fqTableName);
        for (var columnEntry : allFieldsOfTable) {
            if (!PostgresDialect.isBLobColumnName(columnEntry.getTypeName())) {
                continue;
            }
            var triggerName = schemaAndTableName[1] + "_lob_" + columnEntry.getFieldName();
            {
                var sb = new AppendableStringBuilder();

                sb.append("CREATE TRIGGER ").append(triggerName);
                sb.append(" BEFORE UPDATE OF ");
                connectionDialect.escapeName(columnEntry.getFieldName(), sb);
                sb.append(" OR DELETE ");
                sb.append(" ON \"").append(schemaAndTableName[1]).append("\" FOR EACH ROW EXECUTE PROCEDURE lo_manage(");
                connectionDialect.escapeName(columnEntry.getFieldName(), sb);
                sb.append(")");
                sql.add(sb.toString());
            }
            {
                var sb = new AppendableStringBuilder();

                sb.append("CREATE TRIGGER ").append(triggerName).append("_t");
                sb.append(" BEFORE TRUNCATE");
                sb.append(" ON \"").append(schemaAndTableName[1]).append("\" EXECUTE PROCEDURE lo_manage(");
                connectionDialect.escapeName(columnEntry.getFieldName(), sb);
                sb.append(")");
                sql.add(sb.toString());
            }
        }
        return sql.toArray(String.class);
    }

    @SneakyThrows
    @Override
    public String[] createOptimisticLockTrigger(Connection connection, String fqTableName) {
        if (PostgresDialect.BIN_TABLE_NAME.matcher(fqTableName).matches() || PostgresDialect.IDX_TABLE_NAME.matcher(fqTableName).matches()) {
            return new String[0];
        }
        var names = sqlBuilder.getSchemaAndTableName(fqTableName);
        var tableColumns = new ArrayList<String>();

        var allFieldsOfTable = connectionDialect.getAllFieldsOfTable(connection, fqTableName);
        String columnNameOfVersion = null;

        for (var columnEntry : allFieldsOfTable) {
            var columnName = columnEntry.getFieldName();
            if (columnName.equalsIgnoreCase(PermissionGroup.permGroupIdNameOfData)) {
                continue;
            }
            if (!columnEntry.expectsMapping()) {
                continue;
            }
            if (columnEntry.getJavaType().equals(Clob.class) || columnEntry.getJavaType().equals(Blob.class)) {
                // ORA-25006: cannot specify this column in UPDATE OF clause
                // lobs have a lob locator as a pointer to the internal technical lob storage. the lob
                // locator is never changed when a lob is initialized or
                // updated
                continue;
            }
            tableColumns.add(columnName);
            if (columnNameOfVersion == null) {
                String fieldNameLower = columnName.toLowerCase();
                if ("version".equals(fieldNameLower) || "\"version\"".equals(fieldNameLower) || "'version'".equals(fieldNameLower)) {
                    columnNameOfVersion = columnName;
                }
            }
        }
        var maxProcedureNameLength = connection.getMetaData().getMaxProcedureNameLength();
        String triggerName = ormPatternMatcher.buildOptimisticLockTriggerFromTableName(fqTableName, maxProcedureNameLength);

        if (columnNameOfVersion == null) {
            return new String[0];
        }
        if (!columnNameOfVersion.equals(connectionDialect.toDefaultCase(columnNameOfVersion))) {
            columnNameOfVersion = connectionDialect.escapeName(columnNameOfVersion);
        }
        var functionName = "f_" + triggerName;
        var sql = new String[2];
        {
            var sb = new AppendableStringBuilder();
            sb.append("CREATE OR REPLACE FUNCTION ").append(functionName).append("() RETURNS TRIGGER AS $").append(functionName).append("$\n");
            sb.append(" BEGIN\n");
            sb.append("  IF NEW.").append(columnNameOfVersion).append(" <= OLD.").append(columnNameOfVersion).append(" THEN\n");
            sb.append("  RAISE EXCEPTION '").append(Integer.toString(PostgresDialect.getOptimisticLockErrorCode())).append(" Optimistic Lock Exception';\n");
            sb.append("  END IF;\n");
            sb.append("  RETURN NEW;");
            sb.append(" END;\n");
            sb.append("$").append(functionName).append("$ LANGUAGE plpgsql COST 1");
            sql[0] = sb.toString();
        }
        {
            var sb = new AppendableStringBuilder();

            sb.append("CREATE TRIGGER \"").append(triggerName);
            sb.append("\" BEFORE UPDATE");
            if (!tableColumns.isEmpty()) {
                sb.append(" OF ");
                for (int a = 0, size = tableColumns.size(); a < size; a++) {
                    if (a > 0) {
                        sb.append(',');
                    }
                    sqlBuilder.escapeName(tableColumns.get(a), sb);
                }
            }
            sb.append(" ON \"").append(names[1]).append("\" FOR EACH ROW EXECUTE PROCEDURE ").append(functionName).append("()");
            sql[1] = sb.toString();
        }
        return sql;
    }

    @Override
    protected boolean isTableNameToIgnore(String tableName) {
        if (PostgresDialect.BIN_TABLE_NAME.matcher(tableName).matches()) {
            return true;
        }
        return false;
    }

    @Override
    protected IList<String> queryForAllTables(Connection connection) {
        return connectionDialect.queryDefault(connection, "FULL_NAME",
                "SELECT DISTINCT n.nspname || '.' || c.relname AS FULL_NAME FROM pg_trigger t JOIN pg_class c ON t.tgrelid=c.oid JOIN pg_namespace n ON c.relnamespace=n.oid WHERE n.nspname='" +
                        schemaNames[0] + "'");
    }

    @Override
    protected IList<String> queryForAllTriggers(Connection connection) {
        return connectionDialect.queryDefault(connection, "TRIGGER_NAME", "SELECT t.tgname AS TRIGGER_NAME FROM pg_trigger t");
    }

    @Override
    protected IList<String> queryForAllPermissionGroupNeedingTables(Connection connection) {
        return connectionDialect.queryDefault(connection, "TNAME",
                "SELECT c.table_name AS TNAME FROM information_schema.columns c WHERE LOWER(c.column_name)=LOWER('" + PermissionGroup.permGroupIdNameOfData + "') AND LOWER(table_schema)=LOWER('" +
                        schemaNames[0] + "')");
    }

    @Override
    protected IList<String> queryForAllPotentialPermissionGroups(Connection connection) {
        return connectionDialect.queryDefault(connection, "PERM_GROUP_NAME", "SELECT t.table_name AS PERM_GROUP_NAME FROM information_schema.tables t");
    }

    @SneakyThrows
    @Override
    public String[] createPermissionGroup(Connection connection, String tableName) {
        var maxProcedureNameLength = connection.getMetaData().getMaxProcedureNameLength();
        var permissionGroupName = ormPatternMatcher.buildPermissionGroupFromTableName(tableName, maxProcedureNameLength);
        String pkName;
        var matcher = Pattern.compile("(?:.*\\.)?([^\\.]+)").matcher(permissionGroupName);
        if (matcher.matches()) {
            pkName = matcher.group(1) + "_PK";
        } else {
            pkName = tableName + "_PK";
        }
        var sql = new ArrayList<String>();

        sql.add("CREATE TABLE \"" + permissionGroupName + "\" "//
                + "(\"" + PermissionGroup.userIdName + "\" VARCHAR2(64 CHAR) NOT NULL,"//
                + "\"" + PermissionGroup.permGroupIdName + "\" NUMBER(18,0) NOT NULL,"//
                + "\"" + PermissionGroup.readPermColumName + "\" NUMBER(1,0),"//
                + "\"" + PermissionGroup.updatePermColumName + "\" NUMBER(1,0),"//
                + "\"" + PermissionGroup.deletePermColumName + "\" NUMBER(1,0),"//
                + "CONSTRAINT \"" + pkName + "\" PRIMARY KEY ("//
                + "\"" + PermissionGroup.userIdName + "\",\"" + PermissionGroup.permGroupIdName + "\""//
                + ") USING INDEX )");

        sql.add("CREATE INDEX \"" + permissionGroupName + "_IDX\"" + " ON \"" + permissionGroupName + "\" (\"" + PermissionGroup.permGroupIdName + "\")");

        sql.add("CREATE SEQUENCE \"" + permissionGroupName + "_SEQ\" START 10000");

        // PreparedStatement pstm = null;
        // ResultSet rs = null;
        // try
        // {
        // pstm = conn
        // .prepareStatement("SELECT T.TNAME as TNAME FROM TAB T LEFT OUTER JOIN COLS C ON T.TNAME =
        // C.TABLE_NAME WHERE C.COLUMN_NAME=? AND T.TNAME IN (?)");
        // pstm.setString(1, PermissionGroup.permGroupIdNameOfData);
        // pstm.setString(2, tableName);
        // rs = pstm.executeQuery();
        // if (!rs.next())
        // {
        // sql.add("ALTER TABLE " + tableName + " ADD \"" + PermissionGroup.permGroupIdNameOfData + "\"
        // NUMBER(18,0)");
        // }
        // }
        // finally
        // {
        // JdbcUtil.close(pstm, rs);
        // }

        return sql.toArray(String.class);
    }

    @SneakyThrows
    @Override
    public void dropAllSchemaContent(Connection connection, String schemaName) {
        Statement stmt = null, stmt2 = null;
        ResultSet rs = null;
        try {
            stmt = connection.createStatement();
            stmt.execute("DROP SCHEMA IF EXISTS \"" + connectionDialect.toDefaultCase(schemaName) + "\" CASCADE");
        } finally {
            JdbcUtil.close(stmt, rs);
            JdbcUtil.close(stmt2);
        }
    }
}
