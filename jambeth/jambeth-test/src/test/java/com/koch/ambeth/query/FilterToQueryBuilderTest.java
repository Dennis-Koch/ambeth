package com.koch.ambeth.query;

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

import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.koch.ambeth.filter.CompositeFilterDescriptor;
import com.koch.ambeth.filter.FilterDescriptor;
import com.koch.ambeth.filter.FilterOperator;
import com.koch.ambeth.filter.IFilterDescriptor;
import com.koch.ambeth.filter.IPagingResponse;
import com.koch.ambeth.filter.LogicalOperator;
import com.koch.ambeth.informationbus.persistence.setup.SQLData;
import com.koch.ambeth.informationbus.persistence.setup.SQLStructure;
import com.koch.ambeth.query.filter.IFilterToQueryBuilder;
import com.koch.ambeth.query.filter.IPagingQuery;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.testutil.AbstractInformationBusWithPersistenceTest;
import com.koch.ambeth.testutil.TestProperties;
import com.koch.ambeth.testutil.category.SlowTests;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.ArrayList;

@Category(SlowTests.class)
@SQLData("FilterDescriptor_data.sql")
@SQLStructure("FilterDescriptor_structure.sql")
@TestProperties(name = ServiceConfigurationConstants.mappingFile, value = "com/koch/ambeth/query/FilterDescriptor_orm.xml")
public class FilterToQueryBuilderTest extends AbstractInformationBusWithPersistenceTest {
	private IFilterToQueryBuilder filterToQueryBuilder;

	@Override
	public void afterPropertiesSet() throws Throwable {
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(filterToQueryBuilder, "FilterToQueryBuilder");
	}

	public void setFilterToQueryBuilder(IFilterToQueryBuilder filterToQueryBuilder) {
		this.filterToQueryBuilder = filterToQueryBuilder;
	}

	@Test
	public void testBuildQuery_ManyChildFilters() {
		CompositeFilterDescriptor<QueryEntity> filterDescriptor = new CompositeFilterDescriptor<>(
				QueryEntity.class);
		filterDescriptor.setLogicalOperator(LogicalOperator.OR);
		List<IFilterDescriptor<QueryEntity>> childFilterDescriptors = new ArrayList<>();

		for (int i = 0; i < 5000; i++) {
			FilterDescriptor<QueryEntity> filter1 = new FilterDescriptor<>(QueryEntity.class);
			filter1.setMember("Id");
			filter1.setOperator(FilterOperator.IS_EQUAL_TO);
			filter1.setValue(Arrays.asList(Integer.toString(i)));

			FilterDescriptor<QueryEntity> filter2 = new FilterDescriptor<>(QueryEntity.class);
			filter2.setMember("Version");
			filter2.setOperator(FilterOperator.IS_EQUAL_TO);
			filter2.setValue(Arrays.asList(Integer.toString(2)));

			CompositeFilterDescriptor<QueryEntity> childFilter = new CompositeFilterDescriptor<>(
					QueryEntity.class);
			childFilter.setLogicalOperator(LogicalOperator.AND);
			List<IFilterDescriptor<QueryEntity>> subFilters = Arrays
					.<IFilterDescriptor<QueryEntity>>asList(filter1, filter2);
			childFilter.setChildFilterDescriptors(subFilters);
			childFilterDescriptors.add(childFilter);
		}

		filterDescriptor.setChildFilterDescriptors(childFilterDescriptors);
		IPagingQuery<QueryEntity> query = filterToQueryBuilder.buildQuery(filterDescriptor, null);
		assertNotNull(query);

		IPagingResponse<QueryEntity> result = query.retrieve(null);
		assertNotNull(result);
	}
}
