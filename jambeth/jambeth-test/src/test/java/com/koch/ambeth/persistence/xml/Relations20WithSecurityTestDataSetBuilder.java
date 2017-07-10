package com.koch.ambeth.persistence.xml;

/*-
 * #%L
 * jambeth-test
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

import java.sql.Connection;
import java.util.Collection;

import com.koch.ambeth.audit.User;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.util.setup.AbstractDatasetBuilder;
import com.koch.ambeth.merge.util.setup.IDatasetBuilder;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.sql.ISqlBuilder;
import com.koch.ambeth.security.SecurityTest;
import com.koch.ambeth.security.server.IPasswordUtil;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;

public class Relations20WithSecurityTestDataSetBuilder extends AbstractDatasetBuilder {
	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected Connection connection;

	@Autowired
	protected IDatabase database;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected IPasswordUtil passwordUtil;

	@Autowired
	protected ISqlBuilder sqlBuilder;

	@Override
	protected void buildDatasetInternal() {
		User user = createEntity(User.class);
		user.setSID(SecurityTest.userName1);
		user.setName(SecurityTest.userName1);
		user.setActive(true);

		passwordUtil.assignNewPassword(SecurityTest.userPass1.toCharArray(), user, null);
	}

	@Override
	public Collection<Class<? extends IDatasetBuilder>> getDependsOn() {
		return null;
	}
}
