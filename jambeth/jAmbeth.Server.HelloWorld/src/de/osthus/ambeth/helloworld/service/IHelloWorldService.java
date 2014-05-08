package de.osthus.ambeth.helloworld.service;

import java.util.Collection;
import java.util.List;

import de.osthus.ambeth.annotation.Find;
import de.osthus.ambeth.annotation.Merge;
import de.osthus.ambeth.annotation.Process;
import de.osthus.ambeth.annotation.Remove;
import de.osthus.ambeth.annotation.XmlType;
import de.osthus.ambeth.filter.model.IFilterDescriptor;
import de.osthus.ambeth.filter.model.IPagingRequest;
import de.osthus.ambeth.filter.model.IPagingResponse;
import de.osthus.ambeth.filter.model.ISortDescriptor;
import de.osthus.ambeth.helloworld.transfer.TestEntity;
import de.osthus.ambeth.helloworld.transfer.TestEntity2;

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
