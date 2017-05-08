package com.koch.ambeth.query.squery.service;

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

import java.util.Date;
import java.util.List;

import com.koch.ambeth.filter.IPagingRequest;
import com.koch.ambeth.filter.IPagingResponse;
import com.koch.ambeth.filter.ISortDescriptor;
import com.koch.ambeth.query.squery.model.Person;

public interface IPersonService {
	List<Person> findAll();

	Person findById(Integer id);

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

	IPagingResponse<Person> findByNameStartWith(String name, IPagingRequest request,
			ISortDescriptor... sorts);

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
	String findByConcreteMethod(String anyValue);

	List<Person> findByNameStartWithSortByNameDesc(String persionNameStart);

	List<Person> findByAgeGeSortByNameDesc(Integer minAge);
}
