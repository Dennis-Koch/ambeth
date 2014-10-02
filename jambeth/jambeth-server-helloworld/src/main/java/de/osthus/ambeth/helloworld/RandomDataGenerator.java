package de.osthus.ambeth.helloworld;

import java.util.List;

import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.database.DatabaseCallback;
import de.osthus.ambeth.database.ITransaction;
import de.osthus.ambeth.helloworld.service.IHelloWorldService;
import de.osthus.ambeth.helloworld.transfer.TestEntity;
import de.osthus.ambeth.helloworld.transfer.TestEntity2;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.job.IJob;
import de.osthus.ambeth.job.IJobContext;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityFactory;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.security.SecurityContext;
import de.osthus.ambeth.security.SecurityContext.SecurityContextType;
import de.osthus.ambeth.util.ParamChecker;

@SecurityContext(SecurityContextType.AUTHENTICATED)
public class RandomDataGenerator implements IInitializingBean, IJob
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	public static enum ChangeOperation
	{
		NOTHING, INSERT, UPDATE, DELETE;
	}

	@Autowired
	protected IEntityFactory entityFactory;

	@Autowired
	protected IHelloWorldService helloWorldService;

	@Autowired
	protected ITransaction transaction;

	@Property(name = "random.min", mandatory = false)
	protected int minThreshold = 10;

	@Property(name = "random.max", mandatory = false)
	protected int maxThreadhold = 20;

	protected long transactionCounter;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertTrue(maxThreadhold >= minThreshold, "Thresholds must be valid: " + maxThreadhold + " >= " + minThreshold);
	}

	@Override
	public boolean canBePaused()
	{
		return false;
	}

	@Override
	public boolean canBeStopped()
	{
		return false;
	}

	@Override
	public boolean supportsCompletenessTracking()
	{
		return false;
	}

	@Override
	public boolean supportsStatusTracking()
	{
		return false;
	}

	@Override
	public void execute(IJobContext context) throws Throwable
	{
		long start = System.currentTimeMillis();

		// boolean forbiddenSuccess = false;
		// try
		// {
		// helloWorldService.forbiddenMethod();
		// }
		// catch (ServiceCallForbiddenException e)
		// {
		// forbiddenSuccess = true;
		// }
		// if (!forbiddenSuccess)
		// {
		// throw new IllegalStateException("Service call should have been failed!");
		// }

		long lastLogTime = System.currentTimeMillis();
		long lastCounter = transactionCounter;

		final List<TestEntity> allTestEntities = helloWorldService.getAllTestEntities();
		final List<TestEntity2> allTest2Entities = helloWorldService.getAllTest2Entities();

		// Do something for nearly 60 seconds (job gets invoked every 60 seconds
		while (System.currentTimeMillis() - start < 58000)
		{
			doChange(allTestEntities, allTest2Entities);

			transaction.processAndCommit(new DatabaseCallback()
			{
				@Override
				public void callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap) throws Exception
				{
					doChange(allTestEntities, allTest2Entities);
					doChange(allTestEntities, allTest2Entities);
					doChange(allTestEntities, allTest2Entities);
				}
			});

			long spent = System.currentTimeMillis() - lastLogTime;
			if (spent > 5000)
			{
				log.info("Tx/s: " + (long) ((transactionCounter - lastCounter) / (spent / 1000.0)));
				lastLogTime = System.currentTimeMillis();
				lastCounter = transactionCounter;
			}
			Thread.sleep(1);
		}
	}

	protected void doChange(List<TestEntity> allTestEntities, List<TestEntity2> allTest2Entities)
	{
		// Evaluate entity to change by its index in the result list of existing entities (necessary for UPDATE /
		// DELETE)
		int selectEntityIndex = (int) (Math.random() * allTestEntities.size());

		// Evaluate entity2 to select its index in the result list of existing entities (necessary for INSERT of
		// entity1)
		int selectEntity2Index = (int) (Math.random() * allTest2Entities.size());

		// Evaluate random type of change ( INSERT / UPDATE / DELETE / NOTHING)
		double randomChange = Math.random();

		// Map from randomChange to the enum-based operation to execute
		ChangeOperation changeOperation;
		if (randomChange < 0.10) // 10% probability for INSERTs
		{
			changeOperation = ChangeOperation.INSERT;
		}
		else if (randomChange < 0.20) // 10% probability for DELETEs
		{
			changeOperation = ChangeOperation.DELETE;
		}
		else if (randomChange < 0.40) // 20% probability for doing NOTHING
		{
			changeOperation = ChangeOperation.NOTHING;
		}
		else
		// 60% probablity for doing an ordinary UPDATE on an entity
		{
			changeOperation = ChangeOperation.UPDATE;
		}

		boolean changeTestEntity = allTest2Entities.size() > 0 && Math.random() > 0.1;

		doChange(allTestEntities, allTest2Entities, selectEntityIndex, selectEntity2Index, changeOperation, changeTestEntity);
		transactionCounter++;
	}

	protected void doChange(List<TestEntity> allTestEntities, List<TestEntity2> allTest2Entities, int selectEntityIndex, int selectEntity2Index,
			ChangeOperation changeOperation, boolean changeTestEntity)
	{
		// Evaluate new value to change on chosen entity (necessary for INSERT / UPDATE)
		int randomNewValue = (int) (Math.random() * Integer.MAX_VALUE / 2);

		if (changeTestEntity)
		{
			// If there are less than minThreshold entities, force to insertion of one
			if (allTestEntities.size() < minThreshold || ChangeOperation.INSERT.equals(changeOperation))
			{
				TestEntity2 testEntity2 = allTest2Entities.get(selectEntity2Index);
				TestEntity newEntity = entityFactory.createEntity(TestEntity.class);
				newEntity.setMyValue(randomNewValue);
				newEntity.setRelation(testEntity2);
				helloWorldService.saveTestEntities(newEntity);
				allTestEntities.add(newEntity);
				log.debug("New " + TestEntity.class.getSimpleName() + " " + newEntity.getId() + " with " + TestEntity2.class.getName() + " "
						+ testEntity2.getId());
			}
			// If there are more than maxThreadhold entities, force to deletion of one
			else if (allTestEntities.size() > maxThreadhold || ChangeOperation.DELETE.equals(changeOperation))
			{
				TestEntity deleteEntity = allTestEntities.remove(selectEntityIndex);
				log.debug("Del " + TestEntity.class.getSimpleName() + " " + deleteEntity.getId());
				helloWorldService.deleteTestEntities(deleteEntity.getId());
			}
			else if (ChangeOperation.UPDATE.equals(changeOperation))
			{
				TestEntity updateEntity = allTestEntities.get(selectEntityIndex);
				updateEntity.setMyValue(randomNewValue);
				updateEntity.getEmbeddedObject().setName("Name_" + randomNewValue);
				updateEntity.getEmbeddedObject().setValue(randomNewValue);
				helloWorldService.saveTestEntities(updateEntity);
				log.debug("Upd " + TestEntity.class.getSimpleName() + " " + updateEntity.getId());
			}
			else
			{
				TestEntity noOpEntity = allTestEntities.get(selectEntityIndex);
				// Change nothing, but try to save entity (results in NO-OP)
				helloWorldService.saveTestEntities(noOpEntity);
			}
		}
		else
		{
			// If there are less than minThreshold entities, force to insertion of one
			if (allTest2Entities.size() < minThreshold || ChangeOperation.INSERT.equals(changeOperation))
			{
				TestEntity2 newEntity2 = entityFactory.createEntity(TestEntity2.class);
				newEntity2.setMyValue2(randomNewValue);
				helloWorldService.saveTest2Entities(newEntity2);
				allTest2Entities.add(newEntity2);
				log.debug("New " + TestEntity2.class.getSimpleName() + " " + newEntity2.getId());
			}
			// If there are more than maxThreadhold entities, force to deletion of one
			else if (allTest2Entities.size() > maxThreadhold || ChangeOperation.DELETE.equals(changeOperation))
			{
				TestEntity2 deleteEntity = allTest2Entities.remove(selectEntity2Index);
				log.debug("Del " + TestEntity2.class.getSimpleName() + " " + deleteEntity.getId());
				helloWorldService.deleteTest2Entities(deleteEntity);
			}
			else if (ChangeOperation.UPDATE.equals(changeOperation))
			{
				TestEntity2 updateEntity2 = allTest2Entities.get(selectEntity2Index);
				updateEntity2.setMyValue2(randomNewValue);
				helloWorldService.saveTest2Entities(updateEntity2);
				log.debug("Upd " + TestEntity2.class.getSimpleName() + " " + updateEntity2.getId());
			}
			else
			{
				TestEntity2 noOpEntity2 = allTest2Entities.get(selectEntity2Index);
				// Change nothing, but try to save entity (results in NO-OP)
				helloWorldService.saveTest2Entities(noOpEntity2);
			}
		}
	}
}
