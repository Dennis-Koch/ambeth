package com.koch.ambeth.persistence.xml;

import java.sql.Connection;
import java.util.Collection;

import com.koch.ambeth.audit.User;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.util.setup.AbstractDatasetBuilder;
import com.koch.ambeth.merge.util.setup.IDatasetBuilder;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.sql.ISqlBuilder;
import com.koch.ambeth.security.SecurityTest;
import com.koch.ambeth.security.server.IPasswordUtil;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;

public class Relations20WithSecurityTestDataSetBuilder extends AbstractDatasetBuilder
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

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
	protected void buildDatasetInternal()
	{
		User user = createEntity(User.class);
		user.setSID(SecurityTest.userName1);
		user.setName(SecurityTest.userName1);
		user.setActive(true);

		passwordUtil.assignNewPassword(SecurityTest.userPass1.toCharArray(), user, null);
	}

	@Override
	public Collection<Class<? extends IDatasetBuilder>> getDependsOn()
	{
		return null;
	}
}
