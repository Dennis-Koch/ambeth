package de.osthus.ambeth.persistence.xml;

import java.sql.Connection;
import java.util.Collection;

import de.osthus.ambeth.audit.Password;
import de.osthus.ambeth.audit.User;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.security.IPasswordUtil;
import de.osthus.ambeth.security.SecurityTest;
import de.osthus.ambeth.sql.ISqlBuilder;
import de.osthus.ambeth.util.setup.AbstractDatasetBuilder;
import de.osthus.ambeth.util.setup.IDatasetBuilder;

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

		Password password = createEntity(Password.class);
		passwordUtil.assignNewPassword(SecurityTest.userPass1.toCharArray(), password, user);
		user.setPassword(password);
	}

	@Override
	public Collection<Class<? extends IDatasetBuilder>> getDependsOn()
	{
		return null;
	}
}
