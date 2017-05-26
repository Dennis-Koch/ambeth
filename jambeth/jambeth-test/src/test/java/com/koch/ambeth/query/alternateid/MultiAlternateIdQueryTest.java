package com.koch.ambeth.query.alternateid;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.koch.ambeth.filter.FilterDescriptor;
import com.koch.ambeth.filter.IPagingRequest;
import com.koch.ambeth.filter.IPagingResponse;
import com.koch.ambeth.filter.ISortDescriptor;
import com.koch.ambeth.informationbus.persistence.setup.SQLData;
import com.koch.ambeth.informationbus.persistence.setup.SQLStructure;
import com.koch.ambeth.query.filter.IFilterToQueryBuilder;
import com.koch.ambeth.query.filter.IPagingQuery;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.util.ParamChecker;

@TestProperties(name = ServiceConfigurationConstants.mappingFile,
		value = "com/koch/ambeth/query/alternateid/MultiAlternateIdQuery_orm.xml")
@SQLStructure("MultiAlternateIdQuery_structure.sql")
@SQLData("MultiAlternateIdQuery_data.sql")
public class MultiAlternateIdQueryTest extends AbstractInformationBusWithPersistenceTest {
	protected IFilterToQueryBuilder filterToQueryBuilder;

	@Override
	public void afterPropertiesSet() throws Throwable {
		super.afterPropertiesSet();
		ParamChecker.assertNotNull(filterToQueryBuilder, "filterToQueryBuilder");
	}

	public void setFilterToQueryBuilder(IFilterToQueryBuilder filterToQueryBuilder) {
		this.filterToQueryBuilder = filterToQueryBuilder;
	}

	@Test
	public void testRetrieveRefsFD() throws Exception {
		IPagingRequest pagingRequest = null;
		FilterDescriptor<MultiAlternateIdEntity> filterDescriptor =
				new FilterDescriptor<>(MultiAlternateIdEntity.class);
		ISortDescriptor[] sortDescriptors = null;
		IPagingQuery<MultiAlternateIdEntity> query =
				filterToQueryBuilder.buildQuery(filterDescriptor, sortDescriptors);
		IPagingResponse<MultiAlternateIdEntity> actual =
				query.retrieveRefs(pagingRequest, "AlternateId1");
		assertNotNull(actual);
		List<IObjRef> actualList = actual.getRefResult();
		assertEquals(3, actualList.size());
		assertEquals(0, actualList.get(0).getIdNameIndex());
		String aid = (String) actualList.get(0).getId();
		assertTrue(aid.endsWith(".1"));
	}

	@Test
	public void testRetrieveRefsQBF() throws Exception {
		IPagingRequest pagingRequest = null;
		IPagingQuery<MultiAlternateIdEntity> query =
				queryBuilderFactory.create(MultiAlternateIdEntity.class).buildPaging();
		IPagingResponse<MultiAlternateIdEntity> actual =
				query.retrieveRefs(pagingRequest, "AlternateId1");
		assertNotNull(actual);
		List<IObjRef> actualList = actual.getRefResult();
		assertEquals(3, actualList.size());
		assertEquals(0, actualList.get(0).getIdNameIndex());
		String aid = (String) actualList.get(0).getId();
		assertTrue(aid.endsWith(".1"));

		IPagingResponse<MultiAlternateIdEntity> actual2 =
				query.param("abc", "abcValue").retrieveRefs(pagingRequest, "AlternateId1");
		assertNotNull(actual2);
		List<IObjRef> actualList2 = actual2.getRefResult();
		assertEquals(3, actualList2.size());
		assertEquals(0, actualList2.get(0).getIdNameIndex());
		String aid2 = (String) actualList2.get(0).getId();
		assertTrue(aid2.endsWith(".1"));
	}

}
