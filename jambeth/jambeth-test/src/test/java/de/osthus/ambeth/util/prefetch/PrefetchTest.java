package de.osthus.ambeth.util.prefetch;

import java.util.Arrays;
import java.util.Comparator;

import org.junit.Test;

import de.osthus.ambeth.cache.ClearAllCachesEvent;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.event.IEventDispatcher;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.merge.config.MergeConfigurationConstants;
import de.osthus.ambeth.persistence.jdbc.connection.IStatementPerformanceReport;
import de.osthus.ambeth.persistence.jdbc.connection.IStatementPerformanceReportItem;
import de.osthus.ambeth.query.IQuery;
import de.osthus.ambeth.query.IQueryBuilder;
import de.osthus.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import de.osthus.ambeth.testutil.SQLStructure;
import de.osthus.ambeth.testutil.TestFrameworkModule;
import de.osthus.ambeth.testutil.TestProperties;
import de.osthus.ambeth.testutil.TestPropertiesList;
import de.osthus.ambeth.util.IPrefetchConfig;
import de.osthus.ambeth.util.IPrefetchHandle;
import de.osthus.ambeth.util.IPrefetchHelper;

@TestFrameworkModule(PrefetchTestModule.class)
@TestPropertiesList({ @TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "PrefetchTest_orm.xml"),
		@TestProperties(name = MergeConfigurationConstants.PrefetchInLazyTransactionActive, value = "false"),
		@TestProperties(name = "ambeth.log.level.de.osthus.ambeth.persistence.jdbc", value = "DEBUG") })
@SQLStructure("PrefetchTest_structure.sql")
public class PrefetchTest extends AbstractInformationBusWithPersistenceTest
{
	@Autowired
	protected IPrefetchHelper prefetchHelper;

	@Autowired
	protected PrefetchTestDataSetup prefetchTestDataSetup;

	@Autowired
	protected IStatementPerformanceReport statementPerformanceReport;

	@Test
	public void optimizingPrefetch() throws Throwable
	{
		beanContext.getService(IEventDispatcher.class).dispatchEvent(ClearAllCachesEvent.getInstance());

		IQuery<EntityA> q_allEntityAs;
		IQuery<EntityD> q_allEntityDs;
		{
			IQueryBuilder<EntityA> qb = queryBuilderFactory.create(EntityA.class);
			q_allEntityAs = qb.build(qb.isEqualTo(qb.property("Id"), qb.valueName("Id")));
		}
		{
			IQueryBuilder<EntityD> qb = queryBuilderFactory.create(EntityD.class);
			q_allEntityDs = qb.build(qb.isEqualTo(qb.property("Id"), qb.valueName("Id")));
		}
		IList<EntityA> allEntityAs = q_allEntityAs.param("Id", prefetchTestDataSetup.rootEntityA.getId()).retrieve();
		IList<EntityD> allEntityDs = q_allEntityDs.param("Id", prefetchTestDataSetup.rootEntityD.getId()).retrieve();

		// IPrefetchHandle prefetch = prefetchHelper.createPrefetch()//
		// .add(EntityA.class, "AsOfA", "BsOfA")//
		// .build();

		IPrefetchHandle prefetch;
		{
			IPrefetchConfig prefetchConfig = prefetchHelper.createPrefetch();
			EntityA entityA = prefetchConfig.plan(EntityA.class);
			entityA.getAsOfA().get(0).getCsOfA();
			entityA.getBsOfA().get(0).getCsOfB();
			// entityA.getCsOfA();
			// EntityB entityB = prefetchConfig.plan(EntityB.class);
			// entityB.getAsOfB();
			// entityB.getBsOfB();
			// EntityC entityC = prefetchConfig.plan(EntityC.class);
			// entityC.getCsOfC();
			EntityD entityD = prefetchConfig.plan(EntityD.class);
			entityD.getDsOfD();
			prefetch = prefetchConfig.build();
		}
		prefetch.prefetch(allEntityAs, allEntityDs);

		beanContext.getService(IEventDispatcher.class).dispatchEvent(ClearAllCachesEvent.getInstance());
		statementPerformanceReport.reset();
		allEntityAs = q_allEntityAs.param("Id", prefetchTestDataSetup.rootEntityA.getId()).retrieve();
		allEntityDs = q_allEntityDs.param("Id", prefetchTestDataSetup.rootEntityD.getId()).retrieve();
		prefetch.prefetch(allEntityAs, allEntityDs);

		IStatementPerformanceReportItem[] reportItems = statementPerformanceReport.createReport(false, false);
		Arrays.sort(reportItems, new Comparator<IStatementPerformanceReportItem>()
		{
			@Override
			public int compare(IStatementPerformanceReportItem o1, IStatementPerformanceReportItem o2)
			{
				return o1.getStatement().compareTo(o2.getStatement());
			}
		});
		System.out.println();
		// 3 1,66 0,00 SELECT "ENTITYA_ID","ENTITYB_ID" FROM "JAMBETH"."L_ENTITYA_ENTITYBS" WHERE "ENTITYA_ID" IN (?) AND "ENTITYB_ID" IS NOT NULL
		// 3 1,00 7,33 SELECT "ENTITYA_ID","ENTITYC_ID" FROM "JAMBETH"."L_ENTITYA_ENTITYCS" WHERE "ENTITYA_ID" IN (?) AND "ENTITYC_ID" IS NOT NULL
		// 3 0,66 0,33 SELECT "ENTITYB_ID","ENTITYC_ID" FROM "JAMBETH"."L_ENTITYB_ENTITYCS" WHERE "ENTITYB_ID" IN (?) AND "ENTITYC_ID" IS NOT NULL
		// 2 1,50 0,50 SELECT "ID","VERSION" FROM "JAMBETH"."ENTITYA" WHERE "ID" IN (?)
		// 3 1,00 0,33 SELECT "ID","VERSION" FROM "JAMBETH"."ENTITYB" WHERE "ID" IN (?)
		// 3 2,00 0,00 SELECT "LEFT_ID","RIGHT_ID" FROM "JAMBETH"."L_ENTITYA_ENTITYAS" WHERE "LEFT_ID" IN (?) AND "RIGHT_ID" IS NOT NULL
		// 3 1,33 0,00 SELECT "LEFT_ID","RIGHT_ID" FROM "JAMBETH"."L_ENTITYB_ENTITYBS" WHERE "LEFT_ID" IN (?) AND "RIGHT_ID" IS NOT NULL
		// Assert.assertEquals(7, reportItems.length);
		// Assert.assertEquals(3, reportItems[0].getExecutionCount());
		// Assert.assertEquals(3, reportItems[1].getExecutionCount());
		// Assert.assertEquals(3, reportItems[2].getExecutionCount());
		// Assert.assertEquals(2, reportItems[3].getExecutionCount());
		// Assert.assertEquals(3, reportItems[4].getExecutionCount());
		// Assert.assertEquals(3, reportItems[5].getExecutionCount());
		// Assert.assertEquals(3, reportItems[6].getExecutionCount());

	}
}
