package com.koch.ambeth.persistence.oracle;

/*-
 * #%L
 * jambeth-persistence-oracle11-test
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.PersistenceException;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.ioc.IocModule;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.factory.BeanContextFactory;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.persistence.IConnectionDialect;
import com.koch.ambeth.persistence.IPersistenceHelper;
import com.koch.ambeth.persistence.PersistenceHelper;
import com.koch.ambeth.persistence.api.sql.ISqlBuilder;
import com.koch.ambeth.persistence.jdbc.IConnectionFactory;
import com.koch.ambeth.persistence.jdbc.JdbcUtil;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.persistence.jdbc.connection.ConnectionFactory;
import com.koch.ambeth.persistence.jdbc.connection.IDatabaseConnectionUrlProvider;
import com.koch.ambeth.persistence.sql.SqlBuilder;
import com.koch.ambeth.persistence.util.IPersistenceExceptionUtil;
import com.koch.ambeth.persistence.util.PersistenceExceptionUtil;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

/**
 * This class can be used to create or delete one or more database users (schemas). If a property
 * file is demanded all user names are saved to this file after the successful creation of the users
 * (plus the summary of all created users as Ambeth needs it for the property replacement).
 * <p>
 * Creating a user requires at least the create flag set to true and a password for the new user (or
 * several passwords for several users - delimited by a comma). Deleting a user requires the flag
 * set to false and the user names either explicitly (several user names have to be delimited by a
 * comma) or by providing the property file where the user names are stored.
 * <p>
 * <i>Arguments to create a single user:</i><br>
 * script.create=true script.user.pass=mypass
 * script.user.propertyfile=c:/temp/myproject_xyz.properties<br>
 * <br>
 * <i>Arguments to delete a user by name:</i><br>
 * script.create=false script.user.name=CI_TMP_123456<br>
 * <br>
 * <i>Arguments to delete a user by property file:</i><br>
 * script.create=false script.user.propertyfile=c:/temp/myproject_xyz.properties<br>
 * <br>
 * <i>Arguments to create multiple users:</i><br>
 * script.create=true script.user.pass=mypass1,mypass2,mypass3
 * script.user.propertyfile=c:/temp/myproject_xyz.properties<br>
 * <br>
 * <i>Arguments to delete multiple users (by name):</i><br>
 * script.create=false script.user.name=CI_TMP_123456,CI_TMP_123458,CI_TMP_123467<br>
 */
public class RandomUserScript implements IInitializingBean, IStartingBean {
    public static final String SCRIPT_USER_NAME = "script.user.name";

    public static final String SCRIPT_USER_PASS = "script.user.pass";

    public static final String SCRIPT_IS_CREATE = "script.create";

    public static final String SCRIPT_USER_QUOTA = "script.user.quota";

    public static final String SCRIPT_USER_PROPERTYFILE = "script.user.propertyfile";

    /**
     * Used as prefix for the schema names in the property file. Also used for one schema only.
     */
    public static final String PROPERTY_PREFIX = "database.schema.name";

    private static final String ARGUMENT_DELIMITER = ",";

    private static final String SCHEMA_DELIMITER = ":";

    /**
     * @param args
     */
    public static void main(final String[] args) throws Exception {
        Properties.getApplication().fillWithCommandLineArgs(args);
        Properties.loadBootstrapPropertyFile();

        Properties props = Properties.getApplication();

        try (IServiceContext bootstrapContext = BeanContextFactory.createBootstrap(props)) {
            bootstrapContext.createService("randomUser", RandomUserModule.class, IocModule.class);
        }
    }

    private static String[] getUserNames(String userName, String propertyFileName) {
        String[] userNames = null;
        if (userName != null) {
            userNames = userName.split(ARGUMENT_DELIMITER);
        } else {
            Properties props = Properties.getApplication();
            props.load(propertyFileName);
            List<String> foundUserNames = new ArrayList<>();
            int index = 0;
            String usrName = props.getString(PROPERTY_PREFIX + "." + index);
            while (usrName != null) {
                foundUserNames.add(usrName);
                index++;
                usrName = props.getString(PROPERTY_PREFIX + "." + index);
            }
            userNames = foundUserNames.toArray(new String[0]);
        }
        return userNames;
    }

    /**
     * Write the given user name to the given property file. Creates the property file if it doesn't
     * exist.
     *
     * @param propertyFileName Property file name
     * @param createdUserNames User names
     */
    private static void writeToPropertyFile(final String propertyFileName, final List<String> createdUserNames, String[] passwords) {
        if (propertyFileName == null || createdUserNames == null) {
            throw new IllegalArgumentException("Mandatory values not set!");
        }
        File propertyFile = new File(propertyFileName);
        OutputStreamWriter fileWriter = null;
        try {
            fileWriter = new OutputStreamWriter(new FileOutputStream(propertyFile), Charset.forName("UTF-8"));
            String content = createPropertyFileContent(createdUserNames, passwords);
            fileWriter.append(content);
        } catch (IOException e) {
            throw RuntimeExceptionUtil.mask(e);
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    // ignore
                }
                fileWriter = null;
            }
        }
    }

    private static String createPropertyFileContent(final List<String> createdUserNames, String[] passwords) {
        StringBuilder summaryBuilder = new StringBuilder();
        StringBuilder singleSchemaBuilder = new StringBuilder();

        summaryBuilder.append(PROPERTY_PREFIX);
        summaryBuilder.append('=');
        for (int i = 0; i < createdUserNames.size(); i++) {
            String userName = createdUserNames.get(i);

            if (i > 0) {
                summaryBuilder.append(SCHEMA_DELIMITER);
            }
            summaryBuilder.append(userName);

            singleSchemaBuilder.append(PROPERTY_PREFIX);
            singleSchemaBuilder.append('.');
            singleSchemaBuilder.append(i);
            singleSchemaBuilder.append('=');
            singleSchemaBuilder.append(userName);
            singleSchemaBuilder.append('\n');
        }
        summaryBuilder.append('\n');

        String connectionUser =
                PersistenceJdbcConfigurationConstants.DatabaseUser + "=" + createdUserNames.get(0) + "\n" + PersistenceJdbcConfigurationConstants.DatabasePass + "=" + passwords[0] + "\n";
        String content = connectionUser + summaryBuilder.toString() + singleSchemaBuilder.toString();
        return content;
    }

    private static void deleteUsers(final Connection connection, final String[] userNames) throws SQLException {
        Statement stm = connection.createStatement();
        try {
            for (String userName : userNames) {
                deleteUser(stm, userName);
            }
        } finally {
            JdbcUtil.close(stm);
        }
    }

    private static void deleteUser(final Statement statement, final String userName) throws SQLException {
        statement.execute("DROP USER " + userName + " CASCADE");
    }
    @Autowired
    protected IConnectionFactory connectionFactory;
    @Property(name = SCRIPT_IS_CREATE)
    protected boolean createUser;
    @Property(name = SCRIPT_USER_NAME, mandatory = false)
    protected String userName;
    @Property(name = SCRIPT_USER_PASS, mandatory = false)
    protected String userPass;
    @Property(name = SCRIPT_USER_QUOTA, defaultValue = "100M")
    protected String userQuota;
    @Property(name = SCRIPT_USER_PROPERTYFILE, mandatory = false)
    protected String userPropertyFile;
    @LogInstance
    private ILogger log;

    @Override
    public void afterPropertiesSet() throws Throwable {
        ParamChecker.assertNotNull(connectionFactory, "connectionFactory");

        if (createUser) {
            if (userPass == null) {
                throw new IllegalArgumentException("Property '" + SCRIPT_USER_PASS + "' has to be specified if '" + SCRIPT_IS_CREATE + "' is true");
            }
        } else {
            String[] userNames = getUserNames(userName, userPropertyFile);
            if (userNames == null) {
                throw new IllegalArgumentException("Property '" + SCRIPT_USER_NAME + "' or '" + SCRIPT_USER_PROPERTYFILE + "' has to be specified if '" + SCRIPT_IS_CREATE + "' is false");
            }
        }
    }

    @Override
    public void afterStarted() throws Throwable {
        Connection connection = connectionFactory.create();
        try {
            if (createUser) {
                String[] passwords = userPass.split(ARGUMENT_DELIMITER);
                List<String> createdUserNames = new ArrayList<>();
                for (String password : passwords) {
                    String createdUserName = createUser(connection, userName, password, userQuota);
                    if (createdUserName != null) {
                        System.out.println("[[CREATED_USERNAME]] " + createdUserName);
                        createdUserNames.add(createdUserName);
                    }
                }
                if (userPropertyFile != null) {
                    writeToPropertyFile(userPropertyFile, createdUserNames, passwords);
                }
            } else {
                String[] userNames = getUserNames(userName, userPropertyFile);
                deleteUsers(connection, userNames);
            }
        } finally {
            JdbcUtil.close(connection);
            connection = null;
        }
    }

    private String createUser(final Connection connection, final String username, final String password, final String quota) throws SQLException {
        String[] privileges = {
                "RESOURCE",
                "CONNECT",
                "CTXAPP",
                "SELECT_CATALOG_ROLE",
                "create procedure",
                "create sequence",
                "create session",
                "create table",
                "create trigger",
                "create type",
                "create view",
                "create user",
                "drop user",
                "CHANGE NOTIFICATION",
                "EXECUTE ON CTXSYS.CTX_CLS",
                "EXECUTE ON CTXSYS.CTX_DDL",
                "EXECUTE ON CTXSYS.CTX_DOC",
                "EXECUTE ON CTXSYS.CTX_OUTPUT",
                "EXECUTE ON CTXSYS.CTX_QUERY",
                "EXECUTE ON CTXSYS.CTX_REPORT",
                "EXECUTE ON CTXSYS.CTX_THES",
                "EXECUTE ON CTXSYS.CTX_ULEXER",
                "SELECT ON V_$SQLAREA",
                "SELECT ON SYS.CON$",
                "SELECT ON SYS.CDEF$",
                "SELECT ON SYS.CCOL$",
                "SELECT ON SYS.COL$",
                "SELECT ON SYS.USER$",
                "SELECT ON SYS.\"_CURRENT_EDITION_OBJ\"",
                "SELECT ON SYS.ATTRCOL$"
        };

        String createdUserName = null;
        Statement stm = connection.createStatement();
        try {
            String userTablespace = "USERS", tempTablespace = "TEMP";
            int tries = 10, tryCount = 0;

            SQLException firstEx = null;
            while (tryCount++ < tries) {
                // Ensure that we have maximum 28 characters: prefix has 7, long has maximum 19 + 2 random
                // digits
                String randomName = username != null ? username : "CI_TMP_" + System.nanoTime() + String.format("%02d", (int) (Math.random() * 99));
                try {
                    stm.execute(
                            "CREATE USER " + randomName + " IDENTIFIED BY \"" + password + "\" DEFAULT TABLESPACE \"" + userTablespace + "\" TEMPORARY TABLESPACE \"" + tempTablespace + "\" ACCOUNT " +
                                    "LOCK");
                    createdUserName = randomName;
                    break;
                } catch (SQLException e) {
                    if (firstEx == null) {
                        firstEx = e;
                        if (username != null) {
                            // no sense to retry because of the fixed username
                            break;
                        }
                    }
                }
            }
            if (createdUserName == null) {
                log.error("It was not possible to create a random user after " + tries + " + tries. The exception on the first try was:", firstEx);
            } else {
                for (int a = privileges.length; a-- > 0; ) {
                    try {
                        stm.execute("GRANT " + privileges[a] + " to " + createdUserName);
                    } catch (PersistenceException e) {
                        log.error(e);
                    }
                }
                stm.execute("ALTER USER " + createdUserName + " QUOTA " + quota + " ON \"" + userTablespace + "\"");
                stm.execute("ALTER USER " + createdUserName + " ACCOUNT UNLOCK");
            }
        } catch (SQLException e) {
            if (createdUserName != null) {
                deleteUser(stm, createdUserName);
            }
        } finally {
            JdbcUtil.close(stm);
        }
        return createdUserName;
    }

    @FrameworkModule
    public static class RandomUserModule implements IInitializingModule {
        @Override
        public void afterPropertiesSet(final IBeanContextFactory beanContextFactory) throws Throwable {
            beanContextFactory.registerBean(OracleConnectionUrlProvider.class).autowireable(IDatabaseConnectionUrlProvider.class);
            beanContextFactory.registerBean(Oracle10gThinDialect.class).autowireable(IConnectionDialect.class);
            beanContextFactory.registerBean(PersistenceExceptionUtil.class).autowireable(IPersistenceExceptionUtil.class);
            beanContextFactory.registerBean(ConnectionFactory.class).autowireable(IConnectionFactory.class);
            beanContextFactory.registerBean(SqlBuilder.class).autowireable(ISqlBuilder.class);
            beanContextFactory.registerBean(PersistenceHelper.class).autowireable(IPersistenceHelper.class);
            beanContextFactory.registerBean(RandomUserScript.class);
        }
    }
}
