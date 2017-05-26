package com.koch.ambeth.util.prefetch;

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

import java.util.Arrays;
import java.util.Comparator;

import org.junit.Test;

import com.koch.ambeth.event.IEventDispatcher;
import com.koch.ambeth.informationbus.persistence.setup.SQLStructure;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.config.MergeConfigurationConstants;
import com.koch.ambeth.merge.util.IPrefetchConfig;
import com.koch.ambeth.merge.util.IPrefetchHandle;
import com.koch.ambeth.merge.util.IPrefetchHelper;
import com.koch.ambeth.persistence.jdbc.connection.IStatementPerformanceReport;
import com.koch.ambeth.persistence.jdbc.connection.IStatementPerformanceReportItem;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.service.cache.ClearAllCachesEvent;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.TestFrameworkModule;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.TestPropertiesList;
import com.koch.ambeth.util.collections.IList;

@TestFrameworkModule(PrefetchTestModule.class)
@TestPropertiesList({
		@TestProperties(name = ServiceConfigurationConstants.mappingFile,
				value = "PrefetchTest_orm.xml"),
		@TestProperties(name = MergeConfigurationConstants.PrefetchInLazyTransactionActive,
				value = "false"),
		@TestProperties(name = "ambeth.log.level.com.koch.ambeth.persistence.jdbc", value = "DEBUG")})
@SQLStructure("PrefetchTest_structure.sql")
public class PrefetchTest extends AbstractInformationBusWithPersistenceTest {
	@Autowired
	protected IPrefetchHelper prefetchHelper;

	@Autowired
	protected PrefetchTestDataSetup prefetchTestDataSetup;

	@Autowired
	protected IStatementPerformanceReport statementPerformanceReport;

	@Test
	public void optimizingPrefetch() throws Throwable {
		beanContext.getService(IEventDispatcher.class).dispatchEvent(ClearAllCachesEvent.getInstance());

		IQuery<EntityA> q_allEntityAs = createQuery(EntityA.class);
		IQuery<EntityD> q_allEntityDs = createQuery(EntityD.class);

		IList<EntityA> allEntityAs =
				q_allEntityAs.param("Id", prefetchTestDataSetup.rootEntityA.getId()).retrieve();
		IList<EntityD> allEntityDs =
				q_allEntityDs.param("Id", prefetchTestDataSetup.rootEntityD.getId()).retrieve();

		// IPrefetchHandle prefetch = prefetchHelper.createPrefetch()//
		// .add(EntityA.class, "AsOfA", "BsOfA")//
		// .build();

		IPrefetchHandle prefetch = createPrefetch();
		prefetch.prefetch(allEntityAs, allEntityDs);

		beanContext.getService(IEventDispatcher.class).dispatchEvent(ClearAllCachesEvent.getInstance());
		statementPerformanceReport.reset();
		allEntityAs = q_allEntityAs.param("Id", prefetchTestDataSetup.rootEntityA.getId()).retrieve();
		allEntityDs = q_allEntityDs.param("Id", prefetchTestDataSetup.rootEntityD.getId()).retrieve();
		prefetch.prefetch(allEntityAs, allEntityDs);

		IStatementPerformanceReportItem[] reportItems =
				statementPerformanceReport.createReport(false, false);
		Arrays.sort(reportItems, new Comparator<IStatementPerformanceReportItem>() {
			@Override
			public int compare(IStatementPerformanceReportItem o1, IStatementPerformanceReportItem o2) {
				return o1.getStatement().compareTo(o2.getStatement());
			}
		});
		System.out.println();
		// 3 1,66 0,00 SELECT "ENTITYA_ID","ENTITYB_ID" FROM "JAMBETH"."L_ENTITYA_ENTITYBS" WHERE
		// "ENTITYA_ID" IN (?) AND "ENTITYB_ID" IS NOT NULL
		// 3 1,00 7,33 SELECT "ENTITYA_ID","ENTITYC_ID" FROM "JAMBETH"."L_ENTITYA_ENTITYCS" WHERE
		// "ENTITYA_ID" IN (?) AND "ENTITYC_ID" IS NOT NULL
		// 3 0,66 0,33 SELECT "ENTITYB_ID","ENTITYC_ID" FROM "JAMBETH"."L_ENTITYB_ENTITYCS" WHERE
		// "ENTITYB_ID" IN (?) AND "ENTITYC_ID" IS NOT NULL
		// 2 1,50 0,50 SELECT "ID","VERSION" FROM "JAMBETH"."ENTITYA" WHERE "ID" IN (?)
		// 3 1,00 0,33 SELECT "ID","VERSION" FROM "JAMBETH"."ENTITYB" WHERE "ID" IN (?)
		// 3 2,00 0,00 SELECT "LEFT_ID","RIGHT_ID" FROM "JAMBETH"."L_ENTITYA_ENTITYAS" WHERE "LEFT_ID"
		// IN (?) AND "RIGHT_ID" IS NOT NULL
		// 3 1,33 0,00 SELECT "LEFT_ID","RIGHT_ID" FROM "JAMBETH"."L_ENTITYB_ENTITYBS" WHERE "LEFT_ID"
		// IN (?) AND "RIGHT_ID" IS NOT NULL
		// Assert.assertEquals(7, reportItems.length);
		// Assert.assertEquals(3, reportItems[0].getExecutionCount());
		// Assert.assertEquals(3, reportItems[1].getExecutionCount());
		// Assert.assertEquals(3, reportItems[2].getExecutionCount());
		// Assert.assertEquals(2, reportItems[3].getExecutionCount());
		// Assert.assertEquals(3, reportItems[4].getExecutionCount());
		// Assert.assertEquals(3, reportItems[5].getExecutionCount());
		// Assert.assertEquals(3, reportItems[6].getExecutionCount());

	}

	protected IPrefetchHandle createPrefetch() {
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
		return prefetchConfig.build();
	}

	protected <T> IQuery<T> createQuery(Class<T> clazz) {
		IQueryBuilder<T> qb = queryBuilderFactory.create(clazz);
		return qb.build(qb.isEqualTo(qb.property("Id"), qb.valueName("Id")));
	}
}
