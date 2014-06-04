package de.osthus.ambeth.testutil.persistencerunner;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ PersistenceRunnerWithoutAnythingTest.class, PersistenceRunnerWithRebuildDataContextAndAdditionallyDataOnMethodLevelTest.class,
		PersistenceRunnerWithRebuildDataContextAndOnlyDataOnClassLevelTest.class, PersistenceRunnerWithRebuildDataContextAndOnlyDataOnMethodLevelTest.class,
		PersistenceRunnerWithRebuildDataOnlyTest.class, PersistenceRunnerWithRebuildDataStructureAndAdditionallyContextAndDataOnMethodLevelTest.class,
		PersistenceRunnerWithRebuildDataStructureContextAndAdditionallyDataOnMethodLevelTest.class,
		PersistenceRunnerWithRebuildDataStructureContextAndNoDataOnMethodLevelTest.class, PersistenceRunnerWithRebuildDataStructureContextAndNoDataTest.class,
		PersistenceRunnerWithRebuildDataStructureContextAndOnlyDataOnMethodLevelTest.class,
		PersistenceRunnerWithRebuildDataStructureContextAndSomeAdditionallyDataOnMethodLevelTest.class,
		PersistenceRunnerWithContextAndAdditionallyDataOnMethodLevelTest.class, PersistenceRunnerWithContextAndOnlyDataOnClassLevelTest.class,
		PersistenceRunnerWithContextAndOnlyDataOnMethodLevelTest.class, PersistenceRunnerWithStructureAndAdditionallyContextAndDataOnMethodLevelTest.class,
		PersistenceRunnerWithStructureContextAndAdditionallyDataOnMethodLevelTest.class, PersistenceRunnerWithStructureContextAndNoDataOnMethodLevelTest.class,
		PersistenceRunnerWithStructureContextAndNoDataTest.class, PersistenceRunnerWithStructureContextAndOnlyDataOnMethodLevelTest.class,
		PersistenceRunnerWithStructureContextAndSomeAdditionallyDataOnMethodLevelTest.class })
public class AllPersistenceRunnerTests
{
}