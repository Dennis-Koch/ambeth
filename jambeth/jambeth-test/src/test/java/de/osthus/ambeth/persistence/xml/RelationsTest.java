package de.osthus.ambeth.persistence.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import org.junit.Test;

import de.osthus.ambeth.cache.ClearAllCachesEvent;
import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.cache.ICacheContext;
import de.osthus.ambeth.cache.ICacheProvider;
import de.osthus.ambeth.cache.ISingleCacheRunnable;
import de.osthus.ambeth.cache.config.CacheConfigurationConstants;
import de.osthus.ambeth.cache.config.CacheNamedBeans;
import de.osthus.ambeth.cache.interceptor.CacheInterceptor;
import de.osthus.ambeth.event.IEventDispatcher;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import de.osthus.ambeth.persistence.exception.NullConstraintException;
import de.osthus.ambeth.persistence.xml.model.Address;
import de.osthus.ambeth.persistence.xml.model.Boat;
import de.osthus.ambeth.persistence.xml.model.Employee;
import de.osthus.ambeth.persistence.xml.model.IBusinessService;
import de.osthus.ambeth.persistence.xml.model.IEmployeeService;
import de.osthus.ambeth.persistence.xml.model.Project;
import de.osthus.ambeth.service.config.ConfigurationConstants;
import de.osthus.ambeth.testutil.AbstractPersistenceTest;
import de.osthus.ambeth.testutil.SQLData;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;
import de.osthus.ambeth.util.IParamHolder;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.util.ParamHolder;

@SQLData("/de/osthus/ambeth/persistence/xml/Relations_data.sql")
@SQLStructure("/de/osthus/ambeth/persistence/xml/Relations_structure.sql")
@TestPropertiesList({ @TestProperties(name = ConfigurationConstants.mappingFile, value = "de/osthus/ambeth/persistence/xml/orm.xml"),
		@TestProperties(name = CacheConfigurationConstants.ServiceResultCacheActive, value = "false") })
@TestModule(TestServicesModule.class)
public class RelationsTest extends AbstractPersistenceTest
{
	protected IBusinessService businessService;

	protected ICache cache;

	protected IEmployeeService employeeService;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(businessService, "BusinessService");
		ParamChecker.assertNotNull(cache, "Cache");
		ParamChecker.assertNotNull(employeeService, "EmployeeService");
	}

	public void setBusinessService(IBusinessService businessService)
	{
		this.businessService = businessService;
	}

	public void setCache(ICache cache)
	{
		this.cache = cache;
	}

	public void setEmployeeService(IEmployeeService employeeService)
	{
		this.employeeService = employeeService;
	}

	@Test
	public void testNullableToOne() throws Throwable
	{
		Employee employee1 = employeeService.getByName("Oscar Meyer");
		Employee employee2 = employeeService.getByName("Steve Smith");

		assertNull(employee1.getSupervisor());
		assertNotNull(employee2.getSupervisor());
		assertEquals(employee1.getId(), employee2.getSupervisor().getId());

		employee2.setSupervisor(null);
		employee1.setSupervisor(employee2);
		employeeService.save(employee1);
		employeeService.save(employee2);

		Employee loadedEmployee1 = employeeService.getByName("Oscar Meyer");
		Employee loadedEmployee2 = employeeService.getByName("Steve Smith");

		assertNull(loadedEmployee2.getSupervisor());
		assertNotNull(loadedEmployee1.getSupervisor());
		assertEquals(loadedEmployee2.getId(), loadedEmployee1.getSupervisor().getId());
	}

	@Test
	public void testNotNullableToOne() throws Throwable
	{
		Employee employee = employeeService.getByName("Oscar Meyer");

		assertNotNull(employee.getPrimaryAddress());
		assertEquals(11, employee.getPrimaryAddress().getId());

		Address address = cache.getObject(Address.class, 16);
		assertNotNull(address);
		employee.setPrimaryAddress(address);

		employeeService.save(employee);

		Employee actual = cache.getObject(Employee.class, employee.getId());
		assertEquals(address.getId(), actual.getPrimaryAddress().getId());

		Address moved = entityFactory.createEntity(Address.class);
		moved.setStreet("First Street");
		moved.setCity("New Town");
		employee.setPrimaryAddress(moved);

		employeeService.save(employee);
	}

	@Test(expected = PersistenceException.class)
	public void testNotNullableToOne_setToNull() throws Throwable
	{
		Employee employee = employeeService.getByName("Oscar Meyer");

		employee.setPrimaryAddress(null);

		employeeService.save(employee);
	}

	@Test
	public void testToMany() throws Throwable
	{
		Employee employee = employeeService.getByName("Oscar Meyer");

		assertNotNull(employee.getOtherAddresses());
		assertEquals(2, employee.getOtherAddresses().size());

		Address third = entityFactory.createEntity(Address.class);
		third.setStreet("First Street");
		third.setCity("New Town");
		employee.getOtherAddresses().add(third);

		employeeService.save(employee);

		Employee actual = cache.getObject(Employee.class, employee.getId());
		assertEquals(3, actual.getOtherAddresses().size());
	}

	@Test
	public void testNewToMany() throws Throwable
	{
		Employee employee = entityFactory.createEntity(Employee.class);
		employee.setName("Jane Doe");
		employee.setPrimaryProject(cache.getObject(Project.class, 21));

		Address addr1 = entityFactory.createEntity(Address.class);
		addr1.setStreet("First Street");
		addr1.setCity("New Town");
		employee.setPrimaryAddress(addr1);

		Address addr2 = entityFactory.createEntity(Address.class);
		addr2.setStreet("First Street");
		addr2.setCity("New Town");
		employee.getOtherAddresses().add(addr2);

		Address addr3 = entityFactory.createEntity(Address.class);
		addr3.setStreet("Second Street");
		addr3.setCity("New new Town");
		employee.getOtherAddresses().add(addr3);

		assertEquals(2, employee.getOtherAddresses().size());

		employeeService.save(employee);

		Employee actual = employeeService.getByName(employee.getName());
		assertEquals(2, actual.getOtherAddresses().size());
	}

	@Test
	public void testCascadDelete() throws Throwable
	{
		Employee employee = cache.getObject(Employee.class, 1);
		assertFalse(employee.getOtherAddresses().isEmpty());
		Address actual = employee.getOtherAddresses().iterator().next();
		assertNotNull(cache.getObject(Address.class, actual.getId()));

		employeeService.delete(employee);

		assertNull(cache.getObject(Address.class, actual.getId()));
	}

	@Test
	public void testCascadDeleteAfterUnlink() throws Throwable
	{
		Employee employee = cache.getObject(Employee.class, 1);
		int addressCount = employee.getOtherAddresses().size();
		assertTrue(0 < addressCount);
		Address firstAddress = employee.getOtherAddresses().iterator().next();
		assertNotNull(cache.getObject(Address.class, firstAddress.getId()));
		employee.getOtherAddresses().remove(firstAddress);

		employeeService.save(employee);

		assertNull(cache.getObject(Address.class, firstAddress.getId()));
	}

	@Test
	public void testListDelete() throws Throwable
	{
		List<Employee> employees = cache.getObjects(Employee.class, 1, 2, 3);
		employeeService.delete(employees);

		assertTrue(cache.getObjects(Employee.class, 1, 2, 3).isEmpty());
	}

	@Test
	public void testAlternateIdDelete() throws Throwable
	{
		Employee employee = cache.getObject(Employee.class, 1);
		employeeService.delete(employee.getName());

		assertNull(cache.getObject(Employee.class, 1));
	}

	@Test
	public void testSetDelete() throws Throwable
	{
		List<Employee> employees = cache.getObjects(Employee.class, 1, 2, 3);
		Set<Employee> employeeSet = new HashSet<Employee>(employees);
		employeeService.delete(employeeSet);

		assertTrue(cache.getObjects(Employee.class, 1, 2, 3).isEmpty());
	}

	@Test
	public void testArrayDelete() throws Throwable
	{
		List<Employee> employees = cache.getObjects(Employee.class, 1, 2, 3);
		employeeService.delete(employees.toArray(new Employee[employees.size()]));

		assertTrue(cache.getObjects(Employee.class, 1, 2, 3).isEmpty());
	}

	@Test
	public void testCascadedRetrieve() throws Throwable
	{
		List<String> names = Arrays.asList(new String[] { "Steve Smith", "Oscar Meyer" });

		CacheInterceptor.pauseCache.set(Boolean.TRUE);
		try
		{
			List<Employee> actual = businessService.retrieve(names);

			assertEquals(2, actual.size());
		}
		finally
		{
			CacheInterceptor.pauseCache.remove();
		}
	}

	@Test
	public void testMultipleChanges() throws Throwable
	{
		Employee employee1 = cache.getObject(Employee.class, 1);
		employee1.setName(employee1.getName() + " jun.");

		Employee employee2 = entityFactory.createEntity(Employee.class);
		employee2.setName("Jane Doe");
		employee2.setPrimaryProject(cache.getObject(Project.class, 21));

		Address addr1 = entityFactory.createEntity(Address.class);
		addr1.setStreet("First Street");
		addr1.setCity("New Town");
		employee2.setPrimaryAddress(addr1);

		Employee employee3 = cache.getObject(Employee.class, 2);
		employee3.setName(employee3.getName() + " jun.");

		Employee employee4 = cache.getObject(Employee.class, 3);
		employee4.setName(employee4.getName() + " jun.");

		List<Employee> employees = Arrays.asList(new Employee[] { employee1, employee2, employee3, employee4 });

		employeeService.save(employees);
	}

	@Test
	public void testRelationUnlinkSameTable()
	{
		List<Employee> allEmployees = employeeService.getAll();
		Employee withSupervisor = null;
		for (Employee employee : allEmployees)
		{
			if (employee.getSupervisor() != null)
			{
				withSupervisor = employee;
				break;
			}
		}
		assertNotNull("An employee with a supervisor is needed", withSupervisor);

		withSupervisor.setSupervisor(null);
		employeeService.save(withSupervisor);

		Employee actual = employeeService.getByName(withSupervisor.getName());
		assertNull(actual.getSupervisor());
	}

	@Test
	public void testRelationUnlinkOtherTable()
	{
		List<Employee> allEmployees = employeeService.getAll();
		Employee withOtherAddress = null;
		for (Employee employee : allEmployees)
		{
			if (!employee.getOtherAddresses().isEmpty())
			{
				withOtherAddress = employee;
				break;
			}
		}
		assertNotNull("An employee with at least one other address is needed", withOtherAddress);

		withOtherAddress.getOtherAddresses().clear();
		employeeService.save(withOtherAddress);

		Employee actual = employeeService.getByName(withOtherAddress.getName());
		assertTrue(actual.getOtherAddresses().isEmpty());
	}

	@Test
	public void testBidirectionalToOneRelation()
	{
		Employee employee = cache.getObject(Employee.class, 1);
		assertNotNull("An employee with a primary address is needed", employee.getPrimaryAddress());
		Address primaryAddress = cache.getObject(Address.class, employee.getPrimaryAddress().getId());

		assertNotNull(primaryAddress.getResident());
		assertProxyEquals(employee, primaryAddress.getResident());
	}

	@Test
	public void testBidirectionalToManyRelation()
	{
		Employee employee = cache.getObject(Employee.class, 1);
		Project primaryProject = cache.getObject(Project.class, employee.getPrimaryProject().getId());
		assertNotNull("An employee with a primary project is needed", primaryProject);

		assertTrue(primaryProject.getEmployees().contains(employee));
	}

	@Test
	public void testVersionUpdateOnFKRelation()
	{
		int employeeId = 1;
		int oldProjectId = 21;
		int nextProjectId = 22;

		Employee employee = cache.getObject(Employee.class, employeeId);
		Project oldProject = employee.getPrimaryProject();
		assertNotNull("An employee with a primary project is needed", oldProject);
		assertEquals(oldProjectId, oldProject.getId());
		Project nextProject = cache.getObject(Project.class, nextProjectId);
		assertNotNull(nextProject);

		short versionEmployee = employee.getVersion();
		short versionOldProject = oldProject.getVersion();
		short versionNextProject = nextProject.getVersion();

		employee.setPrimaryProject(nextProject);
		employeeService.save(employee);

		Employee loadedEmployee = cache.getObject(Employee.class, employeeId);
		Project loadedOldProject = cache.getObject(Project.class, oldProjectId);
		Project loadedNextProject = cache.getObject(Project.class, nextProjectId);

		assertTrue(versionEmployee < loadedEmployee.getVersion());
		assertTrue(versionOldProject < loadedOldProject.getVersion());
		assertTrue(versionNextProject < loadedNextProject.getVersion());
	}

	@Test
	public void testVersionUpdateOnLTRelation()
	{
		int employeeId = 1;
		int oldProjectId = 21;
		int nextProjectId = 23;

		Employee employee = cache.getObject(Employee.class, employeeId);
		assertEquals(2, employee.getAllProjects().size());
		Project oldProject = cache.getObject(Project.class, oldProjectId);
		Project nextProject = cache.getObject(Project.class, nextProjectId);
		assertTrue(employee.getAllProjects().contains(oldProject));
		assertFalse(employee.getAllProjects().contains(nextProject));

		short versionEmployee = employee.getVersion();
		short versionOldProject = oldProject.getVersion();
		short versionNextProject = nextProject.getVersion();

		employee.getAllProjects().remove(oldProject);
		employee.getAllProjects().add(nextProject);
		employeeService.save(employee);

		assertFalse(employee.getAllProjects().contains(oldProject));
		assertTrue(employee.getAllProjects().contains(nextProject));
		Employee loadedEmployee = cache.getObject(Employee.class, employeeId);
		Project loadedOldProject = cache.getObject(Project.class, oldProjectId);
		Project loadedNextProject = cache.getObject(Project.class, nextProjectId);

		assertTrue(versionEmployee < loadedEmployee.getVersion());
		assertTrue(versionOldProject < loadedOldProject.getVersion());
		assertTrue(versionNextProject < loadedNextProject.getVersion());
	}

	@Test
	public void testVersionUpdateWithoutBackRelation()
	{
		int employeeId = 1;

		Employee employee = cache.getObject(Employee.class, employeeId);
		Boat boat = employee.getBoat();
		assertNotNull("An employee with a boat is needed", boat);

		short versionEmployee = employee.getVersion();
		short versionBoat = boat.getVersion();

		employee.setName(employee.getName() + " II");
		employeeService.save(employee);

		Employee loadedEmployee = cache.getObject(Employee.class, employeeId);
		Boat loadedBoat = cache.getObject(Boat.class, boat.getId());

		assertTrue(versionEmployee < loadedEmployee.getVersion());
		assertEquals(versionBoat, loadedBoat.getVersion());
	}

	@Test
	public void testOptimisticLockWithDelete()
	{
		List<Employee> employees = employeeService.getAll();
		employeeService.delete(employees);

		for (Employee employee : employees)
		{
			assertEquals(0, employee.getId());
			assertEquals(0, employee.getVersion());
		}
	}

	// TODO Test ist durch die Threads "unstabil". Manchmal schlaegt er ohne Aenderung fehl.
	@Test(expected = OptimisticLockException.class)
	public void testOptimisticLockWithDelete_ThreadLocalFLC()
	{
		ICacheProvider cacheProvider = beanContext.getService(CacheNamedBeans.CacheProviderThreadLocal, ICacheProvider.class);
		testOptimisticLockWithDelete_lock_Intern(cacheProvider);
	}

	@Test
	public void testOptimisticLockWithDelete_SingletonFLC_Save_First()
	{
		ICacheProvider cacheProvider = beanContext.getService(CacheNamedBeans.CacheProviderSingleton, ICacheProvider.class);
		testOptimisticLockWithDelete_lock_Intern(Boolean.TRUE, cacheProvider);
	}

	@Test(expected = NullConstraintException.class)
	public void testOptimisticLockWithDelete_SingletonFLC_Save_Last()
	{
		ICacheProvider cacheProvider = beanContext.getService(CacheNamedBeans.CacheProviderSingleton, ICacheProvider.class);
		testOptimisticLockWithDelete_lock_Intern(Boolean.FALSE, cacheProvider);
	}

	@Test(expected = OptimisticLockException.class)
	public void testOptimisticLockWithDelete_PrototypeFLC()
	{
		ICacheProvider cacheProvider = beanContext.getService(CacheNamedBeans.CacheProviderPrototype, ICacheProvider.class);
		testOptimisticLockWithDelete_lock_Intern(cacheProvider);
	}

	protected void testOptimisticLockWithDelete_lock_Intern(ICacheProvider cacheProvider)
	{
		testOptimisticLockWithDelete_lock_Intern(Boolean.TRUE, cacheProvider);
		testOptimisticLockWithDelete_lock_Intern(Boolean.FALSE, cacheProvider);
		testOptimisticLockWithDelete_lock_Intern(null, cacheProvider);
		System.gc();
		System.gc();
	}

	protected void testOptimisticLockWithDelete_lock_Intern(final Boolean saveFirst, final ICacheProvider cacheProvider)
	{
		final CyclicBarrier barrier1 = new CyclicBarrier(2);

		final CyclicBarrier preCleanupBarrier = new CyclicBarrier(2);

		final CountDownLatch firstLatch = new CountDownLatch(1);

		final IParamHolder<Throwable> exceptionHolder = new ParamHolder<Throwable>();
		final CountDownLatch latch = new CountDownLatch(2);
		final IParamHolder<Thread> savingThreadHolder = new ParamHolder<Thread>();
		final IParamHolder<Thread> deletingThreadHolder = new ParamHolder<Thread>();

		final ICacheContext cacheContext = beanContext.getService(ICacheContext.class);

		Thread savingThread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					cacheContext.executeWithCache(cacheProvider, new ISingleCacheRunnable<Object>()
					{
						@Override
						public Object run() throws Throwable
						{
							List<Employee> employees = employeeService.getAll();

							barrier1.await();

							if (Boolean.FALSE.equals(saveFirst))
							{
								firstLatch.await();
							}
							Employee firstEmployee = employees.get(0);
							firstEmployee.setName(firstEmployee.getName() + " jun.");
							employeeService.save(firstEmployee);
							for (Employee employee : employees)
							{
								assertTrue(0 < employee.getId());
								assertTrue(0 < employee.getVersion());
								assertTrue(0 < employee.getPrimaryAddress().getId());
							}
							return null;
						}
					});
				}
				catch (Throwable e)
				{
					if (exceptionHolder.getValue() == null)
					{
						exceptionHolder.setValue(e);
					}
					barrier1.reset();
				}
				finally
				{
					if (Boolean.TRUE.equals(saveFirst))
					{
						firstLatch.countDown();
					}
					latch.countDown();
					try
					{
						preCleanupBarrier.await();
					}
					catch (Throwable e)
					{
						throw RuntimeExceptionUtil.mask(e);
					}
					beanContext.getService(IEventDispatcher.class).dispatchEvent(ClearAllCachesEvent.getInstance());
					beanContext.getService(IThreadLocalCleanupController.class).cleanupThreadLocal();
				}
			}
		});
		Thread deletingThread = new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				try
				{
					cacheContext.executeWithCache(cacheProvider, new ISingleCacheRunnable<Object>()
					{
						@Override
						public Object run() throws Throwable
						{
							List<Employee> employees = employeeService.getAll();

							barrier1.await();

							if (Boolean.TRUE.equals(saveFirst))
							{
								firstLatch.await();
							}
							employeeService.delete(employees);

							for (Employee employee : employees)
							{
								assertTrue(0 == employee.getId());
								assertTrue(0 == employee.getVersion());
								Address primaryAddress = employee.getPrimaryAddress();
								if (primaryAddress != null)
								{
									assertTrue(0 == primaryAddress.getId());
								}
							}
							return null;
						}
					});
				}
				catch (Throwable e)
				{
					if (exceptionHolder.getValue() == null)
					{
						exceptionHolder.setValue(e);
					}
					barrier1.reset();
				}
				finally
				{
					if (Boolean.FALSE.equals(saveFirst))
					{
						firstLatch.countDown();
					}
					latch.countDown();
					try
					{
						preCleanupBarrier.await();
					}
					catch (Throwable e)
					{
						throw RuntimeExceptionUtil.mask(e);
					}
					beanContext.getService(IEventDispatcher.class).dispatchEvent(ClearAllCachesEvent.getInstance());
					beanContext.getService(IThreadLocalCleanupController.class).cleanupThreadLocal();

				}
			}
		});
		savingThread.setContextClassLoader(Thread.currentThread().getContextClassLoader());
		savingThread.setDaemon(true);
		savingThread.setName("Save  ");
		deletingThread.setContextClassLoader(Thread.currentThread().getContextClassLoader());
		deletingThread.setDaemon(true);
		deletingThread.setName("Delete");
		savingThreadHolder.setValue(savingThread);
		deletingThreadHolder.setValue(deletingThread);
		savingThread.start();
		deletingThread.start();
		try
		{
			if (!latch.await(30000, TimeUnit.SECONDS))
			{
				exceptionHolder.setValue(new TimeoutException("No response after 30 seconds"));
			}
			latch.await();
			savingThreadHolder.getValue().interrupt();
			deletingThreadHolder.getValue().interrupt();
			if (exceptionHolder.getValue() != null)
			{
				throw exceptionHolder.getValue();
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Test
	public void testOptimisticLockWithUpdatedCascadeDelete()
	{
		Employee employee = cache.getObject(Employee.class, 1);
		Boat boat = employee.getBoat();

		boat.setName(boat.getName() + " II");
		employeeService.saveBoat(boat);

		employee.setBoat(null);
		employeeService.save(employee);
	}

	@Test
	public void testListOfStrings()
	{
		Employee employee = cache.getObject(Employee.class, 1);
		employee.setNicknames(Arrays.asList("nick1", "nick2"));
		employeeService.save(employee);
	}
}