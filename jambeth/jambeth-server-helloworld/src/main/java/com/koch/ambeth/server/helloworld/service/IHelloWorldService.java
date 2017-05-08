package com.koch.ambeth.server.helloworld.service;

/*-
 * #%L
 * jambeth-server-helloworld
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

import java.util.Collection;
import java.util.List;

import com.koch.ambeth.filter.IFilterDescriptor;
import com.koch.ambeth.filter.IPagingRequest;
import com.koch.ambeth.filter.IPagingResponse;
import com.koch.ambeth.filter.ISortDescriptor;
import com.koch.ambeth.server.helloworld.transfer.TestEntity;
import com.koch.ambeth.server.helloworld.transfer.TestEntity2;
import com.koch.ambeth.util.annotation.Find;
import com.koch.ambeth.util.annotation.Merge;
import com.koch.ambeth.util.annotation.Process;
import com.koch.ambeth.util.annotation.Remove;
import com.koch.ambeth.util.annotation.XmlType;

@XmlType
public interface IHelloWorldService {
	@Find(entityType = TestEntity.class)
	IPagingResponse<TestEntity> findTestEntities(IFilterDescriptor<TestEntity> filterDescriptor,
			ISortDescriptor[] sortDescriptors, IPagingRequest pagingRequest);

	@Process
	void forbiddenMethod();

	@Find
	List<TestEntity> getAllTestEntities();

	@Find
	List<TestEntity2> getAllTest2Entities();

	@Process
	double doFunnyThings(int value, String text);

	@Merge
	void saveTestEntities(TestEntity... testEntities);

	@Merge
	void saveTestEntities(Collection<TestEntity> testEntities);

	@Merge
	void saveTest2Entities(TestEntity2... test2Entities);

	@Remove
	void deleteTestEntities(TestEntity... testEntities);

	@Remove
	void deleteTestEntities(Collection<TestEntity> testEntities);

	@Remove(entityType = TestEntity.class, idName = "Id")
	void deleteTestEntities(long... ids);

	@Remove
	void deleteTest2Entities(TestEntity2... test2Entities);
}
