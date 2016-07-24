package de.osthus.ambeth.query.squery.service;

import java.util.Date;
import java.util.List;

import de.osthus.ambeth.filter.model.IPagingRequest;
import de.osthus.ambeth.filter.model.IPagingResponse;
import de.osthus.ambeth.filter.model.ISortDescriptor;
import de.osthus.ambeth.query.squery.ISquery;
import de.osthus.ambeth.query.squery.model.Person;

public interface IPersonService extends ISquery<Person>
{
	Person findById(Integer id);

	List<Person> findAll();

	List<Person> findByName(String name);

	List<Person> findByNameIsNull();

	List<Person> findByNameContains(String name);

	List<Person> findByNameStartWith(String name);

	List<Person> findByVersionLtOrAgeGeAndNameIn(Integer version, Integer age, String... name);

	List<Person> findByHaveAndriod(Boolean haveAndriod);

	List<Person> findByHaveOrange(Boolean haveOrange);

	List<Person> findByHaveOrangeOrAgeGe(Boolean haveOrange, Integer minAge);

	List<Person> findByHomeAddressName(String name);

	List<Person> findByHomeAddressStreat(String streat);

	int countByNameStartWith(String name);

	Long countByNameContains(String name);

	List<Person> findAllSortByAgeDesc();

	List<Person> findByNameStartWithOrderByAgeAscNameDesc(String name);

	List<Person> findAllSortByVersion(ISortDescriptor sort);

	IPagingResponse<Person> findByNameStartWith(String name, IPagingRequest request, ISortDescriptor... sorts);

	List<Person> findByAgeGe(Integer minAge, IPagingRequest request, ISortDescriptor sort);

	List<Person> findByModifyTimeDateAt(Date date);

	List<Person> findByModifyTimeDateGt(Date date);

	List<Person> findByModifyTimeDateGe(Date date);

	List<Person> findByModifyTimeDateLt(Date date);

	List<Person> findByModifyTimeDateLe(Date date);

	/**
	 * this method is implemented in PersonService, so it will not have Squery feature
	 * 
	 * @param anyValue
	 * @return
	 */
	String someConcreteMethod(String anyValue);

	List<Person> findByNameStartWithSortByNameDesc(String persionNameStart);

	List<Person> findByAgeGeSortByNameDesc(Integer minAge);
}
