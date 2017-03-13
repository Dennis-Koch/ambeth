package com.koch.ambeth.server.helloworld.service;

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
public interface IHelloWorldService
{
	@Find(entityType = TestEntity.class)
	IPagingResponse<TestEntity> findTestEntities(IFilterDescriptor<TestEntity> filterDescriptor, ISortDescriptor[] sortDescriptors, IPagingRequest pagingRequest);

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
