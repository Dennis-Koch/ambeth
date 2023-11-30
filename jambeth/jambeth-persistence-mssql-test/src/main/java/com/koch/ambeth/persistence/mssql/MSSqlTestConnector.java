package com.koch.ambeth.persistence.mssql;

/*-
 * #%L
 * jambeth-persistence-mssql-test
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

import io.toolisticon.spiap.api.SpiService;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.persistence.IConnectionDialect;
import com.koch.ambeth.persistence.jdbc.IConnectionTestDialect;
import com.koch.ambeth.persistence.jdbc.connection.IDatabaseConnectionUrlProvider;
import com.koch.ambeth.persistence.jdbc.testconnector.ITestConnector;

@SpiService(ITestConnector.class)
public class MSSqlTestConnector implements ITestConnector {
    @Override
    public boolean supports(String databaseProtocol) {
        return "jdbc:sqlserver".equals(databaseProtocol);
    }

    @Override
    public void handleTestSetup(IBeanContextFactory beanContextFactory, String databaseProtocol) {
        beanContextFactory.registerBean(MSSqlConnectionUrlProvider.class).autowireable(IDatabaseConnectionUrlProvider.class);
        beanContextFactory.registerBean(MSSqlDialect.class).autowireable(IConnectionDialect.class);
        beanContextFactory.registerBean(MSSqlTestDialect.class).autowireable(IConnectionTestDialect.class);
    }

    @Override
    public void handleTest(IBeanContextFactory beanContextFactory, String databaseProtocol) {
        beanContextFactory.registerBean(MSSqlTestModule.class);
    }
}
