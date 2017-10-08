package com.koch.ambeth.persistence.jdbc.transaction;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.datachange.model.IDataChange;
import com.koch.ambeth.event.IEventListener;
import com.koch.ambeth.event.IEventListenerExtendable;
import com.koch.ambeth.informationbus.persistence.setup.SQLStructure;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.merge.IMergeProcess;
import com.koch.ambeth.model.Material;
import com.koch.ambeth.persistence.api.database.ITransaction;
import com.koch.ambeth.persistence.config.PersistenceConfigurationConstants;
import com.koch.ambeth.persistence.jdbc.transaction.LazyTransactionTest.LazyTransactionTestModule;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.TestFrameworkModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.util.threading.IBackgroundWorkerDelegate;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;

@TestFrameworkModule(LazyTransactionTestModule.class)
@TestPropertiesList({
		@TestProperties(name = PersistenceConfigurationConstants.DatabaseTablePrefix, value = "D_"),
		@TestProperties(name = PersistenceConfigurationConstants.DatabaseFieldPrefix, value = "F_"),
		@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "orm.xml")})
@SQLStructure("../JDBCDatabase_structure.sql")
public class LazyTransactionTest extends AbstractInformationBusWithPersistenceTest {
	public static class LazyTransactionTestModule implements IInitializingModule {
		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
			beanContextFactory.registerBean(MyUserTransaction.class)
					.autowireable(MyUserTransaction.class, UserTransaction.class);
		}
	}

	public static class MyUserTransaction implements UserTransaction {
		private int openTransactions;

		@Override
		public void begin() throws NotSupportedException, SystemException {
			openTransactions++;
		}

		@Override
		public void commit() throws RollbackException, HeuristicMixedException,
				HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
			openTransactions--;
		}

		@Override
		public void rollback() throws IllegalStateException, SecurityException, SystemException {
			openTransactions--;
		}

		@Override
		public void setRollbackOnly() throws IllegalStateException, SystemException {
		}

		@Override
		public int getStatus() throws SystemException {
			return 0;
		}

		@Override
		public void setTransactionTimeout(int seconds) throws SystemException {
		}

		public int getOpenTransactions() {
			return openTransactions;
		}
	}

	@Autowired
	protected IEventListenerExtendable eventListenerExtendable;

	@Autowired
	protected IMergeProcess mergeProcess;

	@Autowired
	protected MyUserTransaction userTransaction;

	@Autowired
	protected ITransaction transaction;

	/**
	 * Test that the each layer of lazy or non-lazy transaction correctly handles the
	 * begin/commit/rollback counter
	 */
	@Test
	public void testLazyWithCascadedTransactionOnDataChange() {
		final Material material = entityFactory.createEntity(Material.class);
		material.setName("Updated");

		class OnceListener implements IEventListener {
			boolean once = false;

			@Override
			public void handleEvent(Object eventObject, long dispatchTime, long sequenceId)
					throws Exception {
				Assert.assertFalse(once);
				once = true;
				eventListenerExtendable.unregisterEventListener(this, IDataChange.class);
				transaction.runInLazyTransaction(new IResultingBackgroundWorkerDelegate<Object>() {
					@Override
					public Object invoke() throws Exception {
						transaction.runInTransaction(new IBackgroundWorkerDelegate() {
							@Override
							public void invoke() throws Exception {
							}
						});
						return null;
					}
				});
			}
		}

		eventListenerExtendable.registerEventListener(new OnceListener(), IDataChange.class);
		mergeProcess.process(material);
		Assert.assertEquals(0, userTransaction.getOpenTransactions());

		material.setName("Updated2");

		eventListenerExtendable.registerEventListener(new OnceListener(), IDataChange.class);
		transaction.runInLazyTransaction(new IResultingBackgroundWorkerDelegate<Object>() {
			@Override
			public Object invoke() throws Exception {
				mergeProcess.process(material);
				return null;
			}
		});
		Assert.assertEquals(0, userTransaction.getOpenTransactions());
	}
}
